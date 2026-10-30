package org.example.infrashtructure.queue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FfprobeService implements AutoCloseable {

    private static final int MAX_STDOUT_BYTES = 8 * 1024 * 1024; // 8 MB cho JSON metadata
    private static final int MAX_STDERR_BYTES = 64 * 1024;       // 64 KB cho log lỗi
    private static final Set<String> BLOCKED_FORMATS = Set.of(
            "tty", "image2", "image2pipe", "jpeg_pipe", "png_pipe", "webp_pipe", "bmp_pipe", "svg_pipe"
    );

    private final String ffprobePath;
    private final URI filerBaseUri; // Có thể null nếu không giới hạn về 1 Filer cố định
    private final long timeoutSeconds;
    private final long rwTimeoutMicros;
    private final long probeSizeBytes;
    private final long analyzeDurationMicros;
    private final ObjectMapper mapper;
    private final ExecutorService ioExecutor;

    public FfprobeService(String ffprobePath, String filerBaseUrl, long timeoutSeconds) {
        this(
                ffprobePath,
                filerBaseUrl != null ? URI.create(filerBaseUrl) : null,
                timeoutSeconds,
                Math.min(timeoutSeconds, 5) * 1_000_000L, // Socket I/O timeout mặc định tối đa 5s
                10_000_000L,                              // 10 MB an toàn hơn cho video 4K/bitrate cao
                5_000_000L,                               // 5 giây
                new ObjectMapper()
        );
    }

    public FfprobeService(
            String ffprobePath,
            URI filerBaseUri,
            long timeoutSeconds,
            long rwTimeoutMicros,
            long probeSizeBytes,
            long analyzeDurationMicros,
            ObjectMapper mapper) {
        this.ffprobePath = Objects.requireNonNull(ffprobePath, "ffprobePath không được null");
        this.filerBaseUri = filerBaseUri;
        this.timeoutSeconds = timeoutSeconds;
        this.rwTimeoutMicros = rwTimeoutMicros;
        this.probeSizeBytes = probeSizeBytes;
        this.analyzeDurationMicros = analyzeDurationMicros;
        this.mapper = Objects.requireNonNull(mapper, "mapper không được null");
        this.ioExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "ffprobe-stream-gobbler");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Đọc metadata từ đường dẫn tương đối trên SeaweedFS Filer (vd: "/buckets/videos/clip #1 tiếng Việt.mp4").
     * Tự động mã hóa an toàn khoảng trắng, tiếng Việt, ký tự '#', '?', '%'.
     */
    public VideoMetadata probeFilerPath(String remotePath) throws FfprobeException {
        return probeFilerPath(remotePath, Collections.emptyMap());
    }

    public VideoMetadata probeFilerPath(String remotePath, Map<String, String> headers) throws FfprobeException {
        if (filerBaseUri == null) {
            throw new FfprobeException(ErrorType.INVALID_URL, "Chưa cấu hình filerBaseUri để ghép với remotePath");
        }
        URI targetUri = buildSafeFilerUri(filerBaseUri, remotePath);
        return probeUri(targetUri, headers);
    }

    /**
     * Đọc metadata từ URL đầy đủ của SeaweedFS Filer.
     */
    public VideoMetadata probeUrl(String fileUrl) throws FfprobeException {
        return probeUrl(fileUrl, Collections.emptyMap());
    }

    public VideoMetadata probeUrl(String fileUrl, Map<String, String> headers) throws FfprobeException {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new FfprobeException(ErrorType.INVALID_URL, "URL không được rỗng");
        }
        try {
            return probeUri(new URI(fileUrl.trim()), headers);
        } catch (URISyntaxException e) {
            throw new FfprobeException(
                    ErrorType.INVALID_URL,
                    "URL không hợp lệ (hãy dùng probeFilerPath nếu tên file chứa khoảng trắng/ký tự đặc biệt)",
                    e
            );
        }
    }

    public VideoMetadata probeUri(URI videoUri, Map<String, String> headers) throws FfprobeException {
        validateTargetUri(videoUri);
        List<String> cmd = buildCommand(videoUri, headers);

        Process p;
        try {
            p = new ProcessBuilder(cmd).start();
        } catch (IOException e) {
            throw new FfprobeException(ErrorType.EXECUTION_ERROR, "Không thể khởi chạy tiến trình ffprobe", e);
        }

        try {
            // Đóng stdin ngay để tiến trình con không bao giờ chờ input
            closeQuietly(p.getOutputStream());

            CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(
                    () -> readStreamBounded(p, p.getInputStream(), MAX_STDOUT_BYTES, true), ioExecutor);
            CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(
                    () -> readStreamBounded(p, p.getErrorStream(), MAX_STDERR_BYTES, false), ioExecutor);

            boolean finished = p.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                String partialErr = sanitizeOutput(getQuietly(stderrFuture), videoUri);
                throw new FfprobeException(
                        ErrorType.TIMEOUT,
                        "ffprobe timeout sau " + timeoutSeconds + "s khi đọc " + maskUri(videoUri)
                                + (partialErr.isEmpty() ? "" : " - " + partialErr)
                );
            }

            String stdout = stdoutFuture.get(2, TimeUnit.SECONDS);
            String stderr = sanitizeOutput(stderrFuture.get(2, TimeUnit.SECONDS), videoUri);

            if (p.exitValue() != 0) {
                throw classifyProcessError(p.exitValue(), stderr, videoUri);
            }

            JsonNode root = mapper.readTree(stdout);
            validateProbeOutput(root, videoUri);
            return VideoMetadata.from(root);

        } catch (FfprobeException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FfprobeException(ErrorType.INTERRUPTED, "Luồng xử lý bị ngắt khi đang chạy ffprobe", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new FfprobeException(ErrorType.IO_ERROR, "Lỗi đọc dữ liệu từ ffprobe: " + cause.getMessage(), cause);
        } catch (TimeoutException | IOException e) {
            throw new FfprobeException(ErrorType.IO_ERROR, "Lỗi phân tích kết quả JSON từ ffprobe", e);
        } finally {
            if (p.isAlive()) {
                p.destroyForcibly();
            }
        }
    }

    private List<String> buildCommand(URI videoUri, Map<String, String> headers) throws FfprobeException {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffprobePath);
        cmd.add("-v");
        cmd.add("error");
        cmd.add("-print_format");
        cmd.add("json");
        cmd.add("-show_format");
        cmd.add("-show_streams");

        // Chặn SSRF / đọc file nội bộ qua HTTP redirect hoặc file m3u8 giả mạo
        cmd.add("-protocol_whitelist");
        cmd.add("http,https,tcp,tls,crypto");

        // Timeout cho từng thao tác socket I/O để thoát sớm khi mạng treo ngầm
        cmd.add("-rw_timeout");
        cmd.add(String.valueOf(rwTimeoutMicros));

        // Tự kết nối lại nếu đứt TCP tạm thời khi nhảy HTTP Range tới moov atom cuối file
        cmd.add("-reconnect");
        cmd.add("1");
        cmd.add("-reconnect_delay_max");
        cmd.add("2");

        cmd.add("-probesize");
        cmd.add(String.valueOf(probeSizeBytes));
        cmd.add("-analyzeduration");
        cmd.add(String.valueOf(analyzeDurationMicros));

        if (headers != null && !headers.isEmpty()) {
            StringBuilder headerBlock = new StringBuilder();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if (containsCrlf(k) || containsCrlf(v)) {
                    throw new FfprobeException(ErrorType.INVALID_URL, "HTTP header chứa ký tự xuống dòng không hợp lệ");
                }
                headerBlock.append(k).append(": ").append(v).append("\r\n");
            }
            cmd.add("-headers");
            cmd.add(headerBlock.toString());
        }

        cmd.add("-i");
        // toASCIIString() đảm bảo ký tự Unicode được percent-encode chuẩn RFC 3986
        cmd.add(videoUri.toASCIIString());
        return cmd;
    }

    private void validateTargetUri(URI uri) throws FfprobeException {
        if (uri == null) {
            throw new FfprobeException(ErrorType.INVALID_URL, "URI không được null");
        }
        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new FfprobeException(ErrorType.INVALID_URL, "Chỉ cho phép giao thức HTTP/HTTPS: " + scheme);
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new FfprobeException(ErrorType.INVALID_URL, "URI thiếu thông tin host");
        }
        // Chống SSRF: nếu đã cấu hình filerBaseUri thì bắt buộc đúng host và port của Filer
        if (filerBaseUri != null) {
            if (!uri.getHost().equalsIgnoreCase(filerBaseUri.getHost())
                    || effectivePort(uri) != effectivePort(filerBaseUri)) {
                throw new FfprobeException(
                        ErrorType.INVALID_URL,
                        "Host đích không khớp với SeaweedFS Filer đã cấu hình: " + maskUri(uri)
                );
            }
        }
    }

    private static URI buildSafeFilerUri(URI baseUri, String remotePath) throws FfprobeException {
        if (remotePath == null || remotePath.isBlank()) {
            throw new FfprobeException(ErrorType.INVALID_URL, "remotePath không được rỗng");
        }
        String normalizedRel = remotePath.startsWith("/") ? remotePath : "/" + remotePath;
        if (Arrays.asList(normalizedRel.split("/")).contains("..")) {
            throw new FfprobeException(ErrorType.INVALID_URL, "Phát hiện path traversal trong remotePath");
        }

        String basePath = baseUri.getPath() == null ? "" : baseUri.getPath();
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }

        try {
            // Constructor nhiều tham số tự động escape '#', '?', '%', khoảng trắng trong tên file
            return new URI(
                    baseUri.getScheme(),
                    baseUri.getUserInfo(),
                    baseUri.getHost(),
                    baseUri.getPort(),
                    basePath + normalizedRel,
                    null,
                    null
            ).normalize();
        } catch (URISyntaxException e) {
            throw new FfprobeException(ErrorType.INVALID_URL, "Không thể tạo URI từ remotePath", e);
        }
    }

    private void validateProbeOutput(JsonNode root, URI uri) throws FfprobeException {
        if (root == null || !root.isObject() || !root.has("format")) {
            throw new FfprobeException(ErrorType.INVALID_MEDIA, "ffprobe trả về JSON rỗng hoặc thiếu format: " + maskUri(uri));
        }

        String formatName = root.path("format").path("format_name").asText("").toLowerCase(Locale.ROOT);
        for (String token : formatName.split(",")) {
            if (BLOCKED_FORMATS.contains(token.trim())) {
                throw new FfprobeException(
                        ErrorType.INVALID_MEDIA,
                        "File không phải video hợp lệ (format=" + formatName + ", có thể là ảnh tĩnh hoặc trang lỗi text/HTML)"
                );
            }
        }

        JsonNode streams = root.path("streams");
        if (!streams.isArray() || streams.isEmpty()) {
            throw new FfprobeException(ErrorType.INVALID_MEDIA, "Không tìm thấy stream nào trong file: " + maskUri(uri));
        }

        JsonNode realVideoStream = null;
        for (JsonNode s : streams) {
            boolean isVideo = "video".equals(s.path("codec_type").asText());
            boolean isAttachedPic = s.path("disposition").path("attached_pic").asInt(0) == 1;
            if (isVideo && !isAttachedPic) {
                realVideoStream = s;
                break;
            }
        }

        if (realVideoStream == null) {
            throw new FfprobeException(
                    ErrorType.INVALID_MEDIA,
                    "File không chứa luồng video thực (có thể chỉ là file âm thanh kèm ảnh bìa)"
            );
        }

        if (realVideoStream.path("width").asInt(0) <= 0 || realVideoStream.path("height").asInt(0) <= 0) {
            throw new FfprobeException(
                    ErrorType.PROBE_INCOMPLETE,
                    "Không đọc được độ phân giải video (cần tăng probeSizeBytes cho video bitrate cao)"
            );
        }
    }

    private static FfprobeException classifyProcessError(int exitCode, String stderr, URI uri) {
        String lower = stderr.toLowerCase(Locale.ROOT);
        String msg = "ffprobe thất bại (exit=" + exitCode + ") tại " + maskUri(uri) + ": " + stderr.trim();

        if (lower.contains("404 not found")) {
            return new FfprobeException(ErrorType.NOT_FOUND, msg);
        }
        if (lower.contains("401 unauthorized") || lower.contains("403 forbidden")) {
            return new FfprobeException(ErrorType.UNAUTHORIZED, msg);
        }
        if (lower.contains("moov atom not found")
                || lower.contains("invalid data found when processing input")
                || lower.contains("end of file")) {
            return new FfprobeException(ErrorType.INVALID_MEDIA, msg);
        }
        if (lower.contains("500 internal server error")
                || lower.contains("502 bad gateway")
                || lower.contains("503 service unavailable")
                || lower.contains("504 gateway timeout")
                || lower.contains("connection refused")
                || lower.contains("connection timed out")
                || lower.contains("connection reset")
                || lower.contains("i/o error")) {
            return new FfprobeException(ErrorType.NETWORK_OR_SERVER_ERROR, msg);
        }
        return new FfprobeException(ErrorType.EXECUTION_ERROR, msg);
    }

    private static String readStreamBounded(Process p, InputStream stream, int maxBytes, boolean failOnOverflow) {
        try (InputStream in = stream;
             ByteArrayOutputStream buffer = new ByteArrayOutputStream(Math.min(maxBytes, 8192))) {
            byte[] chunk = new byte[4096];
            int total = 0;
            int n;
            boolean truncated = false;

            while ((n = in.read(chunk)) != -1) {
                if (total + n <= maxBytes) {
                    buffer.write(chunk, 0, n);
                    total += n;
                } else if (failOnOverflow) {
                    p.destroyForcibly();
                    throw new UncheckedIOException(
                            new IOException("Output của ffprobe vượt quá giới hạn " + maxBytes + " bytes"));
                } else {
                    int remaining = maxBytes - total;
                    if (remaining > 0) {
                        buffer.write(chunk, 0, remaining);
                        total = maxBytes;
                    }
                    truncated = true;
                    // Tiếp tục xả phần dư của stderr để không làm đầy OS pipe gây deadlock
                }
            }

            String text = buffer.toString(StandardCharsets.UTF_8);
            return truncated ? text + "\n...[truncated]" : text;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static boolean containsCrlf(String value) {
        return value != null && (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0);
    }

    private static String maskUri(URI uri) {
        if (uri == null) return "null";
        String base = uri.getScheme() + "://" + uri.getHost()
                + (uri.getPort() != -1 ? ":" + uri.getPort() : "")
                + (uri.getRawPath() != null ? uri.getRawPath() : "");
        return uri.getRawQuery() != null ? base + "?[REDACTED]" : base;
    }

    private static String sanitizeOutput(String text, URI uri) {
        if (text == null || text.isEmpty() || uri == null || uri.getRawQuery() == null) {
            return text == null ? "" : text;
        }
        return text.replace(uri.toASCIIString(), maskUri(uri))
                .replace(uri.toString(), maskUri(uri));
    }

    private static String getQuietly(CompletableFuture<String> future) {
        try {
            return future.get(500, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            return "";
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void close() {
        ioExecutor.shutdownNow();
    }

    public enum ErrorType {
        INVALID_URL(false),
        NOT_FOUND(false),
        UNAUTHORIZED(false),
        INVALID_MEDIA(false),
        PROBE_INCOMPLETE(false),
        TIMEOUT(true),
        NETWORK_OR_SERVER_ERROR(true),
        INTERRUPTED(true),
        IO_ERROR(true),
        EXECUTION_ERROR(false);

        private final boolean retryable;

        ErrorType(boolean retryable) {
            this.retryable = retryable;
        }

        public boolean isRetryable() {
            return retryable;
        }
    }

    public static class FfprobeException extends Exception {
        private final ErrorType errorType;

        public FfprobeException(ErrorType errorType, String message) {
            super(message);
            this.errorType = errorType;
        }

        public FfprobeException(ErrorType errorType, String message, Throwable cause) {
            super(message, cause);
            this.errorType = errorType;
        }

        public ErrorType getErrorType() {
            return errorType;
        }

        public boolean isRetryable() {
            return errorType.isRetryable();
        }
    }
}