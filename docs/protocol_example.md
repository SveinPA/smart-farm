# Smart-TV — Protocol (v1)

**Transport:** TCP (stream)  
**Encoding:** UTF-8 text  
**Framing:** One message per line, terminated by `\r\n` (CRLF)  
**Roles:** client (remote control) ↔ server (TV)

Goal: be simple enough to test with `telnet` / `nc`, but strict enough to unit-test.

---

## 1) Message model

### 1.1 Requests (client → server)
`<COMMAND> [<ARG> ...]\r\n`  
Rules:
- Commands are **case-insensitive** (`on`, `On`, `ON` are equal).
- Arguments are separated by one or more spaces.
- Empty lines are ignored.

### 1.2 Responses (server → client)
`OK[ <payload>]\r\n`  
`ERR <code> <reason>\r\n`  
Notes:
- `OK` may include a small payload (see each command).
- `ERR` carries a numeric code and a stable reason token (see §5).

### 1.3 Events (server → client, async)
`EVT <type>[ <payload>]\r\n`  
Notes:
- Sent **only** to connections that sent `SUB`.
- Delivered **in addition to** the `OK` reply that triggered the change.

---

## 2) TV state & allowed commands

State machine:
- `OFF  --ON-->  ON`
- `ON   --OFF--> OFF`

Allowed commands by state:
- **OFF:** `ON`, `STATUS`, `SUB`, `UNSUB`, `PING`
- **ON:**  `OFF`, `STATUS`, `CHANNELS`, `GET`, `SET <n>`, `UP`, `DOWN`, `SUB`, `UNSUB`, `PING`

When turning ON for the first time after server start, the TV selects channel **1**.  
After an `OFF`/`ON` cycle (without restarting the server), it remembers the **last** channel.

---

## 3) Commands (v1)

| Command    | When | Effect                      | Success reply (`OK …`)           | Errors (`ERR code reason`)                  |
|------------|------|-----------------------------|----------------------------------|--------------------------------------------|
| `ON`       | OFF  | Power on TV                 | `OK`                             | `500 SERVER_ERROR`                          |
| `OFF`      | ON   | Power off TV                | `OK`                             | `500 SERVER_ERROR`                          |
| `STATUS`   | Any  | Report power state          | `OK ON` or `OK OFF`              | —                                          |
| `CHANNELS` | ON   | Get channel count `C`       | `OK C=<int>`                     | `401 TV_OFF`                               |
| `GET`      | ON   | Get current channel         | `OK CH=<int>`                    | `401 TV_OFF`                               |
| `SET <n>`  | ON   | Set channel to `n`          | `OK CH=<n>` (+ `EVT CHANNEL <n>`)  | `401 TV_OFF`, `404 OUT_OF_RANGE`, `400 BAD_COMMAND` |
| `UP`       | ON   | Increase channel by 1       | `OK CH=<n>` (+ `EVT CHANNEL <n>`)  | `401 TV_OFF`, `409 INVALID_STATE`*         |
| `DOWN`     | ON   | Decrease channel by 1       | `OK CH=<n>` (+ `EVT CHANNEL <n>`)  | `401 TV_OFF`, `409 INVALID_STATE`*         |
| `SUB`      | Any  | Subscribe to events         | `OK`                             | —                                          |
| `UNSUB`    | Any  | Stop receiving events       | `OK`                             | —                                          |
| `PING`     | Any  | Health check / keep-alive   | `OK PONG`                        | —                                          |

\* **Wrap behavior:** Default **no wrap**. `UP` on max or `DOWN` on min yields `409 INVALID_STATE`.  
If you later enable wrap, document it here (e.g., for `C=10`: `UP` from 10 → 1).

---

## 4) Events

Sent to **all subscribed** connections after relevant changes.

| Event line          | Meaning                       | When sent                         |
|---------------------|-------------------------------|-----------------------------------|
| `EVT CHANNEL <n>`   | Current channel became `<n>`  | After `SET/UP/DOWN` succeeds      |
| `EVT POWER ON`      | Power turned on               | After `ON` (optional but recommended)  |
| `EVT POWER OFF`     | Power turned off              | After `OFF` (optional but recommended) |

Notes:
- Events are **best-effort** per connection; if delivery fails, the server may drop that subscriber.
- Events do **not** replace the `OK` reply to the caller; they are additional notifications.

---

## 5) Error codes

| Code | Reason          | When                                                                 |
|-----:|-----------------|----------------------------------------------------------------------|
| 400  | `BAD_COMMAND`   | Unknown command, wrong argument count/type, too long line            |
| 401  | `TV_OFF`        | Operation requires TV to be ON                                       |
| 404  | `OUT_OF_RANGE`  | Channel outside `[1..C]`                                             |
| 409  | `INVALID_STATE` | Command not allowed in current state (e.g., `UP` on max with no wrap)|
| 500  | `SERVER_ERROR`  | Unexpected server error                                              |

Format: `ERR <code> <reason>` (reason token is stable; human text is for logs).

---

## 6) Robustness & parsing rules

- Max line length: **256** characters. Longer lines → `ERR 400 BAD_COMMAND`.
- Trim leading/trailing whitespace; collapse multiple spaces between tokens.
- Commands are **case-insensitive**; reasons in `ERR` are **UPPERCASE** tokens.
- Numeric arguments are base-10 integers without signs (e.g., `SET 5`).
- Unknown `EVT` types should be **ignored** by clients (log and continue).
- Idle connections/timeouts are implementation details; closing the socket is allowed.

---

## 7) Examples (normative)

### Turn on and read channel
```
C: STATUS
S: OK OFF
C: ON
S: OK
C: STATUS
S: OK ON
C: GET
S: OK CH=1
```

### Subscribe and set a channel
```
C: SUB
S: OK
C: SET 5
S: OK CH=5
S: EVT CHANNEL 5
```

### Errors
```
C: GET
S: ERR 401 TV_OFF

C: SET 999
S: ERR 404 OUT_OF_RANGE

C: SET abc
S: ERR 400 BAD_COMMAND
```

---

## 8) Versioning & compatibility

- This document is **v1** of the protocol.  
- Adding new commands or events should be **backwards compatible**.  
- Breaking changes should bump the protocol version (e.g., expose `PROTOVERSION`, or negotiate via a banner).

---

## 9) Security (minimal)

- No authentication in v1; intended for lab/local use only.  
- Treat all input as untrusted: apply the parsing rules above and never echo raw user input.  
- Do not expose the TV server to the public internet.

---

## 10) Conformance checklist

**Client MUST**
- send CRLF-terminated lines in UTF-8,
- compose commands case-insensitively,
- ignore unknown `EVT` types without crashing.

**Server MUST**
- accept case-insensitive commands, trim whitespace,
- reply with exactly one `OK …` or `ERR …` per request,
- broadcast `EVT CHANNEL <n>` to subscribers after channel changes,
- enforce `[1..C]` bounds and OFF/ON state rules,
- never send stack traces or internal details to clients.
