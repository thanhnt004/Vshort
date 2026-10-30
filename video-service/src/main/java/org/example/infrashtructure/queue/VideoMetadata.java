package org.example.infrashtructure.queue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaInfo {
    private FormatInfo format;
    private List<VideoStream> video;
    private List<AudioStream> audio;

    // Getters and Setters...
    public FormatInfo getFormat() { return format; }
    public void setFormat(FormatInfo format) { this.format = format; }
    public List<VideoStream> getVideo() { return video; }
    public void setVideo(List<VideoStream> video) { this.video = video; }
    public List<AudioStream> getAudio() { return audio; }
    public void setAudio(List<AudioStream> audio) { this.audio = audio; }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class FormatInfo {
    @JsonProperty("format_name")
    private String formatName;
    private String duration;
    private String size;
    @JsonProperty("bit_rate")
    private String bitRate;
    // Getters and setters...
}

@JsonIgnoreProperties(ignoreUnknown = true)
class VideoStream {
    @JsonProperty("codec_name")
    private String codec;
    private String profile;
    private Integer width;
    private Integer height;
    private String pixFmt;
    private String rFrameRate;
    private String avgFrameRate;
    private String bitRate;
    private String fieldOrder;
    private String colorSpace;
    private String colorTransfer;
    private String colorPrimaries;
    private List<String> sideDataList;
    // Getters and setters...
}

@JsonIgnoreProperties(ignoreUnknown = true)
class AudioStream {
    @JsonProperty("codec_name")
    private String codec;
    private String sampleRate;
    private Integer channels;
    private String channelLayout;
    private String bitRate;
    // Getters and setters...
}