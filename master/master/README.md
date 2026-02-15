# 🛡️ Distributed Scan Dashboard — Master Node

A **distributed network port scanner** built with **Spring Boot 4.0.2** and **Java 21**. This application serves as the **master node** in a master-worker architecture, orchestrating multiple worker agents to perform collaborative port scanning across IP ranges derived from a CIDR block.

---

## 📑 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Data Model](#data-model)
- [API Reference](#api-reference)
- [Service Layer](#service-layer)
- [Utility Classes](#utility-classes)
- [Configuration](#configuration)
- [Getting Started](#getting-started)
- [Workflow](#workflow)
---
## Overview

The Distributed Scan Dashboard is the central control plane for a distributed port scanning system. It:

1. **Registers workers** that send periodic heartbeats.
2. **Assigns IP ranges** to selected workers based on CIDR chunking.
3. **Collects scan results** submitted by workers.
4. **Provides a dashboard API** to view active workers and aggregated scan results.

The system divides a configurable CIDR range into equally-sized chunks and distributes them among selected worker nodes, enabling parallelized network reconnaissance.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        DASHBOARD (UI)                            │
│  GET /api/dashboard/workers     → View active workers            │
│  POST /api/dashboard/start      → Start a scan with workers      │
│  GET /api/dashboard/results     → View all scan results           │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│                     MASTER NODE (this app)                        │
│                                                                  │
│  ┌──────────────┐  ┌────────────────────┐  ┌──────────────────┐  │
│  │ WorkerService│  │ AssignmentService   │  │ScanResultsService│  │
│  │              │  │                    │  │                  │  │
│  │ • Register   │  │ • CIDR chunking    │  │ • Save results   │  │
│  │ • Heartbeat  │  │ • Create assign.   │  │ • Query results  │  │
│  │ • Active chk │  │ • Status updates   │  │                  │  │
│  └──────┬───────┘  └────────┬───────────┘  └────────┬─────────┘  │
│         │                   │                       │            │
│         └───────────────────┼───────────────────────┘            │
│                             ▼                                    │
│                    ┌─────────────────┐                           │
│                    │   MySQL Database │                           │
│                    │  (distributedtask)│                           │
│                    └─────────────────┘                           │
└──────────────────────────────────────────────────────────────────┘
                         ▲
                         │  POST /api/worker/heartbeat
                         │  POST /api/worker/results
                         │
          ┌──────────┐ ┌──────────┐ ┌──────────┐
          │ Worker 1 │ │ Worker 2 │ │ Worker N │
          └──────────┘ └──────────┘ └──────────┘
```

---

## Technology Stack

| Component        | Technology                       |
|------------------|----------------------------------|
| **Framework**    | Spring Boot 4.0.2                |
| **Language**     | Java 21                          |
| **Database**     | MySQL 8.x                        |
| **ORM**          | Spring Data JPA / Hibernate      |
| **Boilerplate**  | Lombok                           |
| **Build Tool**   | Maven                            |
| **DB Connector** | MySQL Connector/J                |
| **Dev Tools**    | Spring Boot DevTools (hot reload)|

---

## Project Structure

```
src/main/java/org/workshop/master/
├── MasterApplication.java          # Spring Boot entry point
├── ScanConfig.java                 # Hardcoded scan configuration (CIDR, ports, interval)
│
├── Entity/                         # JPA entity classes
│   ├── Worker.java                 # Worker node entity
│   ├── WorkerStatus.java           # Enum: IDLE, ACTIVE, BUSY
│   ├── Assignment.java             # Scan assignment entity
│   ├── AssignmentStatus.java       # Enum: PENDING, RUNNING, FINISHED, NOT_EXIST
│   ├── ScanResults.java            # Individual scan result entity
│   └── ScanStatus.java             # Enum: OPEN, CLOSED
│
├── Controllers/                    # REST API controllers
│   ├── DashboardController.java    # Dashboard-facing endpoints
│   └── WorkerController.java       # Worker-facing endpoints
│
├── services/                       # Business logic layer
│   ├── WorkerService.java          # Interface
│   ├── WorkerServiceImp.java       # Implementation
│   ├── AssignmentService.java      # Interface
│   ├── AssignmentServiceImp.java   # Implementation
│   ├── ScanResultsService.java     # Interface
│   └── ScanResultsImp.java         # Implementation
│
├── repository/                     # Spring Data JPA repositories
│   ├── WorkerRepository.java
│   ├── AssignmentRepository.java
│   └── ScanResultsRepository.java
│
├── dto/                            # Data Transfer Objects
│   ├── HeartbeatRequest.java       # Worker heartbeat payload
│   ├── AssignmentResponse.java     # Assignment details sent to worker
│   ├── SelectedWorkers.java        # Dashboard's worker selection
│   ├── ResultsRequest.java         # Scan results submitted by worker
│   ├── ResultItem.java             # Single scan result item
│   ├── ResultsResponse.java        # Scan results for dashboard
│   └── WorkerResponse.java         # Worker info for dashboard
│
└── Utility/
    └── IpUtility.java              # IP address conversion utilities
```

---

## Data Model

### Entity Relationship Diagram

```
┌─────────────────────┐       ┌─────────────────────────┐       ┌─────────────────────────┐
│       Worker        │       │       Assignment         │       │      ScanResults        │
├─────────────────────┤       ├─────────────────────────┤       ├─────────────────────────┤
│ id        : Long PK │──┐    │ id        : Long PK      │──┐    │ id          : Long PK   │
│ name      : String  │  │    │ worker_id : Long FK      │  │    │ ip          : Long      │
│ lastSeen  : Instant │  │    │ startIP   : String       │  │    │ port        : Integer   │
│ workerStatus: Enum  │  └───→│ endIP     : String       │  │    │ worker_id   : Long FK   │
│                     │       │ status    : Enum         │  └───→│ assignment_id: Long FK  │
│                     │       │                         │       │ scannedAt   : Instant   │
│                     │       │                         │       │ status      : Enum      │
└─────────────────────┘       └─────────────────────────┘       └─────────────────────────┘
     1 ──────── * (assignments)       1 ──────── * (scanResults)
```

### Entities

#### `Worker`
Represents a distributed worker node that performs port scanning.

| Field          | Type           | Description                                 |
|----------------|----------------|---------------------------------------------|
| `id`           | `Long` (PK)    | Auto-generated unique identifier            |
| `name`         | `String`       | Unique worker name (sent during heartbeat)  |
| `lastSeen`     | `Instant`      | Timestamp of last heartbeat                 |
| `workerStatus` | `WorkerStatus` | Current status of the worker                |
| `assignments`  | `List<Assignment>` | All assignments linked to this worker   |

#### `Assignment`
Represents a chunk of IP addresses assigned to a worker for scanning.

| Field         | Type               | Description                              |
|---------------|---------------------|------------------------------------------|
| `id`          | `Long` (PK)         | Auto-generated unique identifier         |
| `worker`      | `Worker` (FK)       | The worker assigned to this chunk        |
| `startIP`     | `String`            | Start of the IP range (e.g. `192.168.1.0`) |
| `endIP`       | `String`            | End of the IP range (e.g. `192.168.1.85`)  |
| `status`      | `AssignmentStatus`  | Current status of the assignment         |
| `scanResults` | `List<ScanResults>` | Results collected for this assignment    |

#### `ScanResults`
Stores individual port scan results.

| Field        | Type              | Description                             |
|--------------|-------------------|-----------------------------------------|
| `id`         | `Long` (PK)       | Auto-generated unique identifier        |
| `ip`         | `Long`            | Target IP address (stored as long)      |
| `port`       | `Integer`         | Scanned port number                     |
| `worker`     | `Worker` (FK)     | Worker that performed the scan          |
| `assignment` | `Assignment` (FK) | The assignment this result belongs to   |
| `scannedAt`  | `Instant`         | Timestamp of when the scan was performed|
| `status`     | `ScanStatus`      | Result: `OPEN` or `CLOSED`              |

### Enumerations

| Enum               | Values                                  | Description                     |
|--------------------|------------------------------------------|---------------------------------|
| `WorkerStatus`     | `IDLE`, `ACTIVE`, `BUSY`                 | Worker's current operational state |
| `AssignmentStatus` | `PENDING`, `RUNNING`, `FINISHED`, `NOT_EXIST` | Lifecycle state of an assignment |
| `ScanStatus`       | `OPEN`, `CLOSED`                         | Whether a scanned port is open or closed |

---

## API Reference

The application exposes two sets of REST endpoints:

### Dashboard API — `POST/GET /api/dashboard/*`

These endpoints are consumed by the **dashboard UI** (frontend).

---

#### `GET /api/dashboard/workers`

Returns a list of **active workers** (workers whose last heartbeat was within the last 4 minutes).

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "worker-alpha",
    "lastSeen": "2026-02-15T16:50:00Z",
    "workerStatus": "IDLE"
  }
]
```

---

#### `POST /api/dashboard/start`

Starts a new scan by distributing CIDR-based IP chunks among the selected workers. The IP range is derived from the configured CIDR block and divided equally (ceiling division) among the provided workers.

**Request Body:**
```json
{
  "workerNames": ["worker-alpha", "worker-beta", "worker-gamma"]
}
```

**Response:** `200 OK`
```
"Starts scanning"
```

**What happens internally:**
1. Parses the CIDR range from `ScanConfig.CIDR` (e.g. `192.168.1.0/24` → 256 IPs).
2. Calculates `chunkSize = ceil(totalIPs / numberOfWorkers)`.
3. Creates one `Assignment` per worker with status `PENDING`, each covering a non-overlapping IP sub-range.

---

#### `GET /api/dashboard/results`

Returns all scan results collected so far.

**Response:** `200 OK`
```json
[
  {
    "workerName": "worker-alpha",
    "ip": "192.168.1.10",
    "port": 80,
    "status": "OPEN",
    "assignmentId": 1
  }
]
```

---

### Worker API — `POST /api/worker/*`

These endpoints are called by **worker nodes**.

---

#### `POST /api/worker/heartbeat`

Workers send periodic heartbeats to register themselves or refresh their presence. If the worker already exists with a pending assignment, the assignment details are returned.

**Request Body:**
```json
{
  "workerName": "worker-alpha"
}
```

**Response scenarios:**

**Scenario 1 — New worker (first heartbeat):**
```
200 OK
"Worker added to database"
```
The worker is created with status `IDLE` and the current timestamp.

**Scenario 2 — Known worker, no pending assignments:**
```
200 OK
"worker has no pending assignments"
```
The worker's `lastSeen` timestamp is updated.

**Scenario 3 — Known worker, has pending assignment:**
```json
200 OK
{
  "workerName": "worker-alpha",
  "startIp": "192.168.1.0",
  "endIp": "192.168.1.85",
  "ports": [8080, 443, 80],
  "interval": 20,
  "assignmentStatus": "RUNNING",
  "assignmentId": 1
}
```
The first pending assignment is transitioned to `RUNNING` and the worker status becomes `BUSY`.

---

#### `POST /api/worker/results`

Workers submit their scan results (can be partial or final). If `isFinished` is `true`, the assignment status transitions to `FINISHED` and the worker goes back to `IDLE`.

**Request Body:**
```json
{
  "workerName": "worker-alpha",
  "assignmentId": 1,
  "isFinished": false,
  "data": [
    { "ip": "192.168.1.10", "port": 80, "status": "OPEN" },
    { "ip": "192.168.1.10", "port": 443, "status": "CLOSED" },
    { "ip": "192.168.1.11", "port": 8080, "status": "OPEN" }
  ]
}
```

**Response:** `200 OK`
```
"Results saved Successfully"
```

---

## Service Layer

### `WorkerService` / `WorkerServiceImp`

Manages worker lifecycle and state.

| Method                | Description                                                      |
|-----------------------|------------------------------------------------------------------|
| `getWorkerByName(name)` | Finds a worker by its unique name                              |
| `updateTimestamp(name)` | Updates the `lastSeen` field to `Instant.now()`                |
| `createWorker(name)`    | Creates a new worker with current timestamp                    |
| `createNewWorker(worker)` | Persists a fully constructed `Worker` entity                 |
| `getActiveWorkers()`    | Returns workers whose last heartbeat was **< 4 minutes ago**  |
| `updateWorkerStatus(worker, status)` | Updates a worker's operational status           |

### `AssignmentService` / `AssignmentServiceImp`

Manages scan assignments and CIDR chunking logic.

| Method                          | Description                                                        |
|---------------------------------|--------------------------------------------------------------------|
| `startScan(selectedWorkers)`    | Splits CIDR range into chunks and creates `PENDING` assignments    |
| `getAssignmentsForWorker(...)`  | Finds assignments by worker name, worker status, and assignment status |
| `updateAssignmentStatus(id, status)` | Transitions an assignment's lifecycle state                   |
| `getAssignment(assignmentId)`   | Retrieves a single assignment by ID                                |

#### CIDR Chunking Algorithm

```
Given: CIDR = "192.168.1.0/24" → startIP = 192.168.1.0, endIP = 192.168.1.255
       Workers selected: 3

chunkSize = ceil((255 - 0 + 1) / 3) = ceil(256/3) = 86

Worker 0: 192.168.1.0   → 192.168.1.85
Worker 1: 192.168.1.86  → 192.168.1.171
Worker 2: 192.168.1.172 → 192.168.1.255
```

### `ScanResultsService` / `ScanResultsImp`

Handles persistence and retrieval of scan results.

| Method                  | Description                                              |
|-------------------------|----------------------------------------------------------|
| `saveScanResults(list)` | Bulk-saves a list of `ScanResults` entities              |
| `getAllScanResults()`   | Returns all results, converting IP longs back to strings |

---

## Utility Classes

### `IpUtility`

Provides IP address conversion and CIDR range calculation.

| Method                    | Description                                         | Example                                    |
|---------------------------|-----------------------------------------------------|--------------------------------------------|
| `ipToLong(String ip)`     | Converts dotted-quad IP to `long`                   | `"192.168.1.1"` → `3232235777L`            |
| `longToIp(long ip)`       | Converts `long` back to dotted-quad string          | `3232235777L` → `"192.168.1.1"`            |
| `cidrToRange(String cidr)`| Parses CIDR notation, returns `[networkIP, broadcastIP]` | `"192.168.1.0/24"` → `[3232235776, 3232236031]` |

---

## Configuration

### `ScanConfig`

Hardcoded scan parameters (modify these constants to change scan behavior):

```java
public final class ScanConfig {
    public static final String CIDR = "192.168.1.0/24";       // Target network range
    public static final int INTERVAL_IN_SECONDS = 20;          // Scan interval
    public static final int[] PORTS = {8080, 443, 80};         // Ports to scan
}
```

### `application.properties`

```properties
spring.application.name=master
spring.datasource.url=jdbc:mysql://localhost:3306/distributedtask
spring.datasource.username=root
spring.datasource.password=123456789

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

| Property                       | Description                                      |
|--------------------------------|--------------------------------------------------|
| `spring.datasource.url`       | MySQL connection URL (database: `distributedtask`) |
| `spring.jpa.hibernate.ddl-auto` | `update` — auto-creates/modifies schema on startup |
| `spring.jpa.show-sql`         | Logs all SQL queries to console                    |

---

## Getting Started

### Prerequisites

- **Java 21** (JDK)
- **MySQL 8.x** running on `localhost:3306`
- **Maven 3.9+** (or use the included `mvnw` wrapper)

### Setup

1. **Create the MySQL database:**
   ```sql
   CREATE DATABASE distributedtask;
   ```

2. **Update credentials** in `src/main/resources/application.properties` if needed:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Build and run:**
   ```bash
   # Using Maven wrapper
   ./mvnw spring-boot:run

   # Or on Windows
   mvnw.cmd spring-boot:run
   ```

4. The application starts on **`http://localhost:8080`** by default.

---

## Workflow

The complete scan lifecycle follows this sequence:

```
       ┌─────────────────────────────────────────────────────┐
       │              1. WORKER REGISTRATION                 │
       │                                                     │
       │  Worker sends POST /api/worker/heartbeat            │
       │  → Master creates Worker (status: IDLE)             │
       │  → Worker repeats heartbeat periodically            │
       └──────────────────────┬──────────────────────────────┘
                              ▼
       ┌─────────────────────────────────────────────────────┐
       │           2. SCAN INITIATION (Dashboard)            │
       │                                                     │
       │  Dashboard calls GET /api/dashboard/workers         │
       │  → Gets list of active workers (lastSeen < 4 min)   │
       │                                                     │
       │  Dashboard calls POST /api/dashboard/start          │
       │  → Sends list of selected worker names              │
       │  → Master splits CIDR into chunks                   │
       │  → Creates PENDING assignments per worker           │
       └──────────────────────┬──────────────────────────────┘
                              ▼
       ┌─────────────────────────────────────────────────────┐
       │           3. ASSIGNMENT DISTRIBUTION                │
       │                                                     │
       │  Worker sends next heartbeat                        │
       │  → Master finds PENDING assignment for worker       │
       │  → Returns AssignmentResponse (IP range, ports)     │
       │  → Assignment status → RUNNING                      │
       │  → Worker status → BUSY                             │
       └──────────────────────┬──────────────────────────────┘
                              ▼
       ┌─────────────────────────────────────────────────────┐
       │           4. RESULTS SUBMISSION                     │
       │                                                     │
       │  Worker scans IPs and sends results via             │
       │  POST /api/worker/results                           │
       │  → Can send partial results (isFinished=false)      │
       │  → Final batch sets isFinished=true                 │
       │  → Assignment status → FINISHED                     │
       │  → Worker status → IDLE (ready for new work)        │
       └──────────────────────┬──────────────────────────────┘
                              ▼
       ┌─────────────────────────────────────────────────────┐
       │          5. RESULTS VIEWING (Dashboard)             │
       │                                                     │
       │  Dashboard calls GET /api/dashboard/results         │
       │  → Returns all scan results with IP, port, status   │
       └─────────────────────────────────────────────────────┘
```

### Step-by-Step Example

1. **Worker registers** by sending heartbeats:
   ```bash
   curl -X POST http://localhost:8080/api/worker/heartbeat \
     -H "Content-Type: application/json" \
     -d '{"workerName": "scanner-01"}'
   ```

2. **Dashboard lists active workers:**
   ```bash
   curl http://localhost:8080/api/dashboard/workers
   ```

3. **Dashboard starts a scan** with selected workers:
   ```bash
   curl -X POST http://localhost:8080/api/dashboard/start \
     -H "Content-Type: application/json" \
     -d '{"workerNames": ["scanner-01", "scanner-02"]}'
   ```

4. **Worker receives assignment** on next heartbeat:
   ```bash
   curl -X POST http://localhost:8080/api/worker/heartbeat \
     -H "Content-Type: application/json" \
     -d '{"workerName": "scanner-01"}'
   # → Returns: { startIp, endIp, ports, interval, assignmentId }
   ```

5. **Worker submits results:**
   ```bash
   curl -X POST http://localhost:8080/api/worker/results \
     -H "Content-Type: application/json" \
     -d '{
       "workerName": "scanner-01",
       "assignmentId": 1,
       "isFinished": true,
       "data": [
         {"ip": "192.168.1.1", "port": 80, "status": "OPEN"},
         {"ip": "192.168.1.1", "port": 443, "status": "CLOSED"}
       ]
     }'
   ```

6. **Dashboard views results:**
   ```bash
   curl http://localhost:8080/api/dashboard/results
   ```

---

## License

This project is developed as part of a workshop exercise.
