param($name)
# Các lệnh docker tiếp theo giữ nguyên
docker-compose build $name
docker-compose up -d $name