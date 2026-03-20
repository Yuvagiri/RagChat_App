
# 🧠 RAG Chat - API Documentation

This project provides RESTful APIs for managing chat sessions and messages for a Retrieval-Augmented Generation (RAG) based chatbot system. It includes endpoints for health checks, session management, message creation, and message retrieval.

---

## 🚀 Base URL

```

http://localhost:8080

```

---

## 🔐 Authentication

Some endpoints expect a header:

```

x-api-key: admin_123

````

If authentication is not required (as with the Health Check), this header can be omitted.

---

## 📂 Endpoints Overview

### 1. ✅ Health Status

**Check service health status.**

- **Method:** `GET`
- **URL:** `/actuator/health`
- **Authentication:** ❌ Not required

**Sample Request:**
```bash
curl http://localhost:8080/actuator/health
````

**Sample Response:**

```json
{
    "status": "UP",
    "components": {
        "db": {
            "status": "UP",
            "details": {
                "database": "PostgreSQL",
                "validationQuery": "isValid()"
            }
        },
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 354010206208,
                "free": 291079995392,
                "threshold": 10485760,
                "path": "/app/.",
                "exists": true
            }
        },
        "ping": {
            "status": "UP"
        }
    }
}
```

---

### 2. 📘 Create Session

**Create a new chat session.**

* **Method:** `POST`
* **URL:** `/rag_chat_storage/sessions/create`
* **Authentication:** ✅ Required

**Request Body:**

```json
{
  "name": "Travel Plan Chat",
  "userName": "yuva"
}
```

**Sample Response:**

```json
{
  "id": 952,
  "name": "Travel Plan Chat",
  "userName": "yuva",
  "createdAt": "..."
}
```

---

### 3. ❌ Delete Session

**Delete an existing session by ID.**

* **Method:** `DELETE`
* **URL:** `/rag_chat_storage/sessions/delete/{sessionId}`
* **Authentication:** ✅ Required

**Example:**

```bash
DELETE /rag_chat_storage/sessions/delete/952
```

**Response:**

* `200 OK` or `204 No Content`

---

### 4. ⭐ Mark Session as Favourite

**Mark/unmark a session as favorite.**

* **Method:** `PATCH`
* **URL:** `/rag_chat_storage/sessions/{sessionId}/favorite?isFavorite=true`
* **Authentication:** ✅ Required

**Example:**

```bash
PATCH /rag_chat_storage/sessions/105/favorite?isFavorite=true
```

**Query Param:**

* `isFavorite=true | false`

---

### 5. 📄 Get All Sessions

**Retrieve all stored chat sessions.**

* **Method:** `GET`
* **URL:** `/rag_chat_storage/sessions/getAll`
* **Authentication:** ✅ Required

**Sample Response:**

```json
[
  {
    "id": 952,
    "name": "Travel Plan Chat",
    "userName": "yuva",
    ...
  }
]
```

---

### 6. ✏️ Rename Session

**Rename an existing session.**

* **Method:** `PATCH`
* **URL:** `/rag_chat_storage/sessions/{sessionId}/rename`
* **Authentication:** ✅ Required

**Request Body:**

```json
{
  "name": "Renamed Session Title"
}
```

**Example:**

```bash
PATCH /rag_chat_storage/sessions/954/rename
```

---

### 7. 📝 Create Message

**Add a user or bot message to a session.**

* **Method:** `POST`
* **URL:** `/rag_chat_storage/messages`
* **Authentication:** ✅ Required

**Request Body (User Message):**

```json
{
  "sessionId": 952,
  "isBot": false,
  "content": "Hello!"
}
```

**Request Body (Bot Message with Context):**

```json
{
  "sessionId": 952,
  "isBot": true,
  "content": "Here's a summary of the page.",
  "context": "https://simple.wikipedia.org/wiki/Speedrun"
}
```

---

### 8. 📬 Get Messages for a Session

**Fetch all messages for a given session.**

* **Method:** `GET`
* **URL:** `/rag_chat_storage/messages/{sessionId}`
* **Authentication:** ✅ Required

**Example:**

```bash
GET /rag_chat_storage/messages/103
```

**Sample Response:**

```json
[
  {
    "content": "Hello!",
    "isBot": false,
    ...
  },
  {
    "content": "Hi there!",
    "isBot": true,
    "context": "..."
  }
]
```

---

## 📌 Notes

* All endpoints return JSON responses.
* Response times are optimized to be under 1s in most cases.
* Sessions are uniquely identified using their `sessionId`.
* Messages can be attributed to a user or bot using the `isBot` field.

---

## 🧪 Postman Collection

To import and test all endpoints quickly, you can use the provided Postman collection in JSON format.

---

## 🛠️ Tech Stack

* Spring Boot
* REST APIs
* Postgres
* Redis (optional backend storage)

---

