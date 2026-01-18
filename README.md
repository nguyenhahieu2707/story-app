## 🛠️ Backend Setup (Spring Boot)

Phần này hướng dẫn **cài đặt và chạy Backend Service** sử dụng **Java Spring Boot**.

---

## 1️⃣ Yêu cầu hệ thống (Prerequisites)

Đảm bảo máy bạn đã cài đặt các công cụ sau:

* **Java Development Kit (JDK)**: Phiên bản **17 trở lên**
* **Maven**: Dùng để quản lý dependency và build project
* **Docker & Docker Compose**: Dùng để chạy Database và Object Storage

---

## 2️⃣ Cài đặt hạ tầng (Infrastructure)

Dự án sử dụng:

* **PostgreSQL**: Lưu trữ dữ liệu
* **MinIO**: Lưu trữ file (ảnh, âm thanh, model AI)

Toàn bộ hạ tầng được khởi tạo nhanh chóng bằng **Docker Compose**.

---

### 🔹 Bước 1: Tạo file `docker-compose.yml`

Tạo file `docker-compose.yml` tại **thư mục gốc của dự án**
(ngang hàng với thư mục `backend` và `ai-service`), với nội dung sau:

```yaml
version: '3.8'

services:
  # --- PostgreSQL Database ---
  postgres:
    image: postgres:15-alpine
    container_name: story_speaker_db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: appdb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 12345678
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - story-net

  # --- MinIO Object Storage ---
  minio:
    image: minio/minio
    container_name: story_speaker_minio
    ports:
      - "9000:9000"   # API Port
      - "9001:9001"   # Console Port
    environment:
      MINIO_ROOT_USER: root
      MINIO_ROOT_PASSWORD: 12345678
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data
    networks:
      - story-net

volumes:
  postgres_data:
  minio_data:

networks:
  story-net:
    driver: bridge
```

---

### 🔹 Bước 2: Khởi động Docker Compose

Mở terminal tại thư mục chứa file `docker-compose.yml` và chạy:

```bash
docker-compose up -d
```

---

### 🔹 Bước 3: Cấu hình MinIO Buckets

1. Truy cập **MinIO Console**
   👉 [http://localhost:9001](http://localhost:9001)

2. Đăng nhập:

   * **Username:** `root`
   * **Password:** `12345678`

3. Vào mục **Buckets** và tạo **3 bucket bắt buộc**:

   * `story-speaker`
     👉 Lưu ảnh bìa, ảnh minh họa truyện

   * `voice-models`
     👉 Lưu model AI training

   * `generated-audio`
     👉 Lưu file âm thanh sau khi generate

4. ⚠️ **Lưu ý quan trọng**

   * Set **Access Policy** của các bucket là **Public**
   * Hoặc cấu hình policy **read-only** để Client có thể tải ảnh và âm thanh

---

## 3️⃣ Cấu hình ứng dụng (Configuration)

Kiểm tra file:

```
src/main/resources/application.properties
```

Đảm bảo cấu hình khớp với Docker:

```properties
# ===============================
# Database Configuration
# ===============================
spring.datasource.url=jdbc:postgresql://localhost:5432/appdb
spring.datasource.username=postgres
spring.datasource.password=12345678

# ===============================
# MinIO Configuration
# ===============================
minio.url=http://localhost:9000
minio.access-key=root
minio.secret-key=12345678
minio.bucket-name=story-speaker

# ===============================
# Google OAuth & JWT
# (Tự điền key hoặc set biến môi trường)
# ===============================
google.client-id=${GOOGLE_CLIENT_ID}
jwt.secret=${JWT_SECRET}
spring.ai.google.genai.api-key=${GEMINI_API_KEY}
```

---

## 4️⃣ Chạy ứng dụng (Run Application)

Mở terminal tại thư mục **backend** (nơi chứa file `pom.xml`).

### ▶️ Cách 1: Chạy trực tiếp bằng Maven

```bash
mvn spring-boot:run
```

### ▶️ Cách 2: Build file JAR và chạy

```bash
mvn clean package -DskipTests
java -jar target/story-speaker-0.0.1-SNAPSHOT.jar
```

---

## 5️⃣ Kiểm tra hoạt động (Verification)

Sau khi ứng dụng khởi động thành công:

* **Base URL:**
  👉 [http://localhost:8080/story-speaker](http://localhost:8080/story-speaker)

* **Swagger UI (API Documentation):**
  👉 [http://localhost:8080/story-speaker/swagger-ui/index.html](http://localhost:8080/story-speaker/swagger-ui/index.html)
