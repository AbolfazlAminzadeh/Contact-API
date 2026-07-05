# Netty Contact Service

A high-performance contact management service built directly on Netty, designed for low latency and minimal overhead.

---

# 👏 How to run
### For First, Clone Repository
```cmd
git clone https://github.com/AbolfazlAminzadeh/Contact-API.git
cd Contact-API
```
### now build the container and let it finish the work
```cmd
docker build -t capi:latest .
```

### Then you can run container by this command
```cmd
docker run -d -p 10203:10203 --name contact-api --ulimit nofile=1048576:1048576 --network host capi:latest
```

# 📡 API Documentation

## Base URL

```
http://localhost:10203
```

---

## Create Contact

**POST /contacts**

### Request Body

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "phone": "09123456789"
}
```

### Responses

* `201 CREATED` → returns created contact
* `422 UNPROCESSABLE ENTITY` → validation error

---

## Get All Contacts

**GET /contacts**

### Responses

* `200 OK`

```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "phone": "09123456789"
  }
]
```

---

## Delete Contact

**DELETE /contacts/{id}**

### Responses

* `200 OK`

```json
{ "message": "Deleted Successfully" }
```

* `404 NOT FOUND`
* `422 UNPROCESSABLE ENTITY`

---

## Shutdown Server

**GET /shutdown**

### Response

```json
{ "status": "shutting down" }
```

---

## Error Format

All errors return:

```json
{
  "error": "message"
}
```

---

## Validation Rules

* `id >= 0`
* `firstName`, `lastName` must not be empty
* `phone`:

  * must be numeric
  * must be 11 digits
  * normalized to start with `09`

---

# 🛠️ For Developers

## Requirements

* **Java 25**
* Gradle

---

## Build Runnable JAR

```bash
./gradlew shadowJar
```

Output:

```
build/libs/<project>-all.jar
```

Run:

```bash
java -jar build/libs/<project>-all.jar
```

---

## Native Image (Best Performance)

For maximum performance and zero JVM dependency:

### Requirements

* **GraalVM 25**
* Set `JAVA_HOME` to GraalVM

### Build Native Binary

```bash
./gradlew nativeCompile
```

Output:

```
build/native/nativeCompile/<binary>
```

Run:

```bash
./<binary>
```

---

## Performance Notes

* Uses Netty with:

  * Epoll (Linux)
  * KQueue (macOS)
  * NIO fallback
* Optimized TCP settings:

  * `TCP_NODELAY`
  * `SO_KEEPALIVE`
  * `TCP_FASTOPEN`
  * `TCP_QUICKBACK` (JUST ON LINUX)
* Thread model:

  * Boss group (accept connections)
  * Worker group (handle requests)

---

## Architecture Overview

Pipeline:

```
HttpServerCodec
→ HttpObjectAggregator
→ JsonDecoder
→ MainHandler
```

Storage:

* In-memory `ConcurrentHashMap` (thread-safe)  (save on disk, soon)

---

## Notes

* No persistence layer (in-memory only)
* Designed for performance experimentation / lightweight services
* Graceful shutdown supported via `/shutdown`

---

### (Most parts of README file is AI-Generated, find any issue? report it)