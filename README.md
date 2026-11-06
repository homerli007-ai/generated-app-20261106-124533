# demo

A small full-stack application for safely deleting files on the server. The backend exposes a single REST endpoint that only deletes regular files located inside a configured `allowed_root` directory, refusing symlinks, non-regular files, and any path that escapes the root. The React frontend provides a simple UI for submitting paths and viewing per-path results.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

Backend:

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

Frontend:

```bash
cd frontend
npm install
npm run dev
```

The dev server starts on `http://localhost:5173` and proxies `/api` requests to the backend.

## API

### `POST /api/delete`

Deletes the given files, subject to the safety rules described below.

Request body:

```json
{
  "paths": ["/absolute/path/to/file1.txt", "/absolute/path/to/file2.txt"]
}
```

Response body:

```json
{
  "allowedRoot": "/absolute/path/to/allowed_root",
  "deleted": ["/absolute/path/to/file1.txt"],
  "refused": [
    { "path": "/absolute/path/to/file2.txt", "reason": "outside allowed_root" }
  ]
}
```

Each path is processed independently. Possible refusal reasons include `outside allowed_root`, `symlink refused`, `does not exist`, `not a regular file`, and the name of any `IOException` / `InvalidPathException` encountered.

## Notes

- The allowed root is configured via `app.allowed-root` in `backend/src/main/resources/application.properties` (default `./allowed_root`). It is resolved to an absolute, normalized path at request time.
- Only regular files are deleted. Directories, symlinks, and special files are always refused.
- Paths are normalized before the containment check, so `..` traversal outside the root is rejected.
- There is no authentication or authorization; the endpoint is intended for local/trusted use only.
- Deletion is permanent — there is no trash or undo.
- The frontend sends one request per submission; there is no batching, retry, or progress reporting beyond the final result.
- CORS is not configured on the backend; the Vite dev proxy is expected to be used during development.