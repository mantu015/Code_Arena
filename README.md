# Code Arena 🏆

A full-stack competitive programming platform built to simulate environments like LeetCode. Features a premium glassmorphism React UI with a Monaco Code Editor, real code execution for JavaScript, Python, and Java, a points-based leaderboard, daily challenges, user profiles, and a complete admin panel.

---

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Default Credentials](#-default-credentials)
- [Admin Panel](#-admin-panel)
- [API Reference](#-api-reference)
- [Problems & Execution Engine](#-problems--execution-engine)
- [Rank & Points System](#-rank--points-system)
- [Configuration](#-configuration)

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19 (Vite 8), vanilla CSS, Glassmorphism Design System |
| Icons | `lucide-react` |
| Code Editor | `@monaco-editor/react` (Monaco / VS Code engine) |
| HTTP Client | `axios` |
| Routing | `react-router-dom` v7 |
| Backend | Spring Boot 3.2, Java 17 |
| ORM | Spring Data JPA + Hibernate |
| Database | H2 (file-based persistent) |
| Code Execution | Nashorn ScriptEngine (JS), `ProcessBuilder` (Python, Java) |
| Build Tools | Maven 3.9, Vite 8 |

---

## ✨ Features

### For Users
- **Sign Up / Sign In** — session persisted in `localStorage`, auto-validated against DB on page load
- **Problem Set** — 6 seeded algorithmic problems (Easy / Medium / Hard) with live search and difficulty filter tabs
- **Real Code Execution** — write code in the browser and run it against actual test cases:
  - **JavaScript** — executed via Nashorn ScriptEngine (in-process, instant)
  - **Python** — executed via `python` subprocess with per-test-case isolation
  - **Java** — compiled with `javac` and executed via `java` subprocess
  - Per-test-case result rows showing input → expected → actual output with runtime in ms
- **Submission History** — slide-in drawer on the workspace showing all past attempts per problem
- **Daily Challenge** — auto-assigned problem per day with a live countdown timer and +50 bonus points
- **Global Leaderboard** — podium top-3 display, tier badges, "you" highlight for logged-in user
- **User Profile** — rank badge, stat cards (solved count, acceptance rate, global rank), difficulty breakdown bars, recent submissions
- **Dark / Light Theme** — toggle persisted in `localStorage`

### For Admins
- **Separate admin portal** at `/admin` — completely isolated from the user UI
- **Platform Overview** — total users, problems, submissions, acceptance rate, language distribution, top problems
- **User Management** — view all users with points, delete users (cascades to submissions)
- **Submission Monitor** — all submissions with language, status, timestamp
- **Admin is never visible** in the leaderboard or user-facing stats

---

## 🛠️ Prerequisites

- [Node.js](https://nodejs.org/) v16 or higher
- [JDK 17](https://adoptium.net/) or higher
- [Maven](https://maven.apache.org/) 3.6 or higher (or use your IDE)
- [Python 3](https://www.python.org/) — required for Python code execution
- `javac` / `java` in your system PATH — required for Java code execution

---

## 💻 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Prudhvi-69/Code-arena.git
cd Code-arena
```

### 2. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts on **`http://localhost:8080`**.

The H2 database is file-based and persists at `backend/data/codearena.mv.db`. On first run, tables are created and seeded automatically.

> **H2 Console** (for debugging): `http://localhost:8080/h2-console`
> - JDBC URL: `jdbc:h2:file:./data/codearena;AUTO_SERVER=TRUE`
> - Username: `sa`
> - Password: *(leave blank)*

### 3. Start the Frontend

Open a new terminal:

```bash
cd frontend
npm install
npm run dev
```

Access the app at **`http://localhost:5173`**

---

## 📁 Project Structure

```
Code-arena/
├── backend/
│   ├── src/main/java/com/codearena/
│   │   ├── controller/          # REST controllers
│   │   │   ├── AdminController.java
│   │   │   ├── DailyChallengeController.java
│   │   │   ├── ExecutionController.java
│   │   │   ├── LeaderboardController.java
│   │   │   ├── ProblemController.java
│   │   │   ├── SubmissionController.java
│   │   │   └── UserController.java
│   │   ├── model/               # JPA entities + DTOs
│   │   │   ├── DailyChallenge.java
│   │   │   ├── ExecutionResult.java
│   │   │   ├── Problem.java
│   │   │   ├── Submission.java
│   │   │   └── User.java
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── service/             # Business logic
│   │   │   ├── CodeExecutionService.java   # Real execution engine
│   │   │   ├── ProblemService.java
│   │   │   ├── SubmissionService.java
│   │   │   └── UserService.java
│   │   ├── CodeArenaApplication.java
│   │   ├── CorsConfig.java
│   │   └── DataInitializer.java  # Seeds 6 problems on first run
│   └── src/main/resources/
│       ├── application.properties
│       ├── schema.sql            # Table definitions (IF NOT EXISTS)
│       └── data.sql              # Admin user seed + ALTER TABLE migrations
│
└── frontend/
    └── src/
        ├── components/
        │   ├── AuthModal.jsx     # Sign In / Sign Up / Forgot Password modal
        │   └── Navbar.jsx
        ├── contexts/
        │   └── ThemeContext.jsx
        ├── pages/
        │   ├── AdminDashboard.jsx
        │   ├── AdminLoginPage.jsx
        │   ├── DailyChallengePage.jsx
        │   ├── Home.jsx
        │   ├── LeaderboardPage.jsx
        │   ├── ProblemsPage.jsx
        │   ├── ProfilePage.jsx
        │   └── WorkspacePage.jsx
        ├── App.jsx
        ├── apiConfig.js
        └── index.css
```

---

## 🔑 Default Credentials

### Regular User
There are no pre-seeded regular users. Register a new account via the **Sign Up** button on the navbar.

### Admin Account

| Field | Value |
|---|---|
| **URL** | `http://localhost:5173/admin` |
| **Username** | `admin` |
| **Password** | `admin123` |
| **Role** | `ADMIN` |

> ⚠️ **Important:** Change the admin password before deploying to any public environment. The admin account is seeded via `backend/src/main/resources/data.sql`.

---

## 🛡️ Admin Panel

The admin panel is completely separate from the user-facing app — different URL, different layout, no shared navigation.

### Accessing the Admin Panel

Navigate to: **`http://localhost:5173/admin`**

Log in with the admin credentials above. You will be redirected to **`/admin/dashboard`**.

### Admin Dashboard Tabs

#### Overview Tab
- Total registered users, problems, submissions
- Acceptance rate across all submissions
- Language distribution bar chart (JavaScript, Python, Java, C++)
- Top 5 most attempted problems
- Recent 10 submissions with username, problem title, language, status, timestamp

#### Users Tab
- Full list of all non-admin users sorted by points
- Each row shows: rank number, avatar, username, points, role badge
- **Delete user** button — opens a confirmation modal, deletes the user and all their submissions (FK cascade)

#### Submissions Tab
- All submissions in the system, newest first
- Shows: submission ID, user ID, problem ID, language (color-coded), status, timestamp
- Displays up to 100 most recent entries

### Admin Security
- Admin login is handled by `POST /api/admin/login` — only users with `role = 'ADMIN'` can authenticate
- All admin API endpoints require an `X-Admin-Token: admin:<username>` header
- Admin users are **excluded** from the leaderboard and all user-facing rank calculations
- Admin session is stored separately in `localStorage` under `code_arena_admin`

---

## 📡 API Reference

### Auth
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/users/register` | Register a new user |
| `POST` | `/api/users/login` | Login and get user object |
| `GET` | `/api/users/{id}` | Get user by ID |
| `GET` | `/api/users/{id}/stats` | Get user profile stats |

### Problems
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/problems` | Get all problems |
| `GET` | `/api/problems/{id}` | Get problem by ID |
| `POST` | `/api/problems` | Create a new problem |

### Execution & Submissions
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/execute` | Execute code and run test cases |
| `GET` | `/api/submissions/user/{userId}` | Get all submissions for a user |
| `GET` | `/api/submissions/user/{userId}/solved` | Get count of solved problems |
| `GET` | `/api/submissions/user/{userId}/problem/{problemId}` | Get submissions for a specific problem |

### Leaderboard
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/leaderboard` | Get top 10 users by points (admins excluded) |

### Daily Challenge
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/daily` | Get today's challenge (auto-assigned) |
| `POST` | `/api/daily/claim` | Claim +50 bonus points after solving |

### Admin *(requires `X-Admin-Token` header)*
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/admin/login` | Admin authentication |
| `GET` | `/api/admin/users` | Get all non-admin users |
| `DELETE` | `/api/admin/users/{id}` | Delete a user and their submissions |
| `GET` | `/api/admin/submissions` | Get all submissions |
| `GET` | `/api/admin/stats` | Get full platform statistics |

---

## ⚙️ Problems & Execution Engine

### Seeded Problems

| ID | Title | Difficulty |
|---|---|---|
| 1 | Two Sum | Easy |
| 2 | Add Two Numbers | Medium |
| 3 | Longest Substring Without Repeating Characters | Medium |
| 4 | Median of Two Sorted Arrays | Hard |
| 5 | 3Sum | Medium |
| 6 | Regular Expression Matching | Hard |

### Execution Engine

Code is executed server-side via `POST /api/execute`. Each language has a different execution path:

| Language | Execution Method | Notes |
|---|---|---|
| **JavaScript** | Nashorn `ScriptEngine` (JVM in-process) | Fastest, no subprocess overhead |
| **Python** | `ProcessBuilder` → `python sol.py` | Requires Python 3 in PATH |
| **Java** | `ProcessBuilder` → `javac` + `java Main` | Requires `javac`/`java` in PATH |
| **C++** | Not supported | Shows "Not Supported" message |

**How Java execution works:** The user writes a `class Solution { ... }` — the engine extracts the body, wraps it as a `static class Solution` inside a generated `Main.java` with `import java.util.*`, compiles it, and runs it.

**How Python execution works:** The user can write either a plain function (`def twoSum(...)`) or a class-based solution (`class Solution: def twoSum(self, ...)`). The engine auto-detects class-based solutions and binds the methods as top-level functions before running the test driver.

**Timeout:** 10 seconds per test case. Infinite loops are killed automatically.

---

## 🏅 Rank & Points System

### Points
| Action | Points Awarded |
|---|---|
| Solve a problem (first time) | +10 pts |
| Claim daily challenge bonus | +50 pts |
| Re-submitting an already solved problem | 0 pts (no duplicate award) |

### Rank Tiers

| Tier | Minimum Points | Color |
|---|---|---|
| 🌱 Newcomer | 0 | Gray |
| 💻 Coder | 50 | Green |
| 🔵 Solver | 150 | Blue |
| 🟣 Expert | 300 | Purple |
| 🏆 Master | 600 | Gold |
| 🔥 Grandmaster | 1000 | Red |

---

## ⚙️ Configuration

### Backend — `backend/src/main/resources/application.properties`

```properties
# Persistent H2 file database
spring.datasource.url=jdbc:h2:file:./data/codearena;AUTO_SERVER=TRUE

# H2 web console (dev only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Run schema.sql + data.sql on every startup (safe — uses IF NOT EXISTS + MERGE)
spring.sql.init.mode=always

# Backend port
server.port=8080

# CORS — update if frontend runs on a different port
spring.mvc.cors.allowed-origins=http://localhost:5173
```

### Frontend — `frontend/.env`

```env
VITE_API_BASE_URL=http://localhost:8080
```

Update `VITE_API_BASE_URL` if your backend runs on a different host or port, then restart the dev server.

---

## 🔗 Connecting Frontend & Backend

The Vite dev server proxies `/api/*` requests to `http://localhost:8080` (configured in `vite.config.js`). The `VITE_API_BASE_URL` env variable is used as a fallback for direct axios calls.

If you change the backend port:
1. Update `server.port` in `application.properties`
2. Update `VITE_API_BASE_URL` in `frontend/.env`
3. Update the proxy target in `frontend/vite.config.js`
4. Update `spring.mvc.cors.allowed-origins` if the frontend port also changes

---

## 🔐 Authentication Flow

```
User registers  →  POST /api/users/register  →  User saved with role=USER
User logs in    →  POST /api/users/login      →  Returns {id, username, role}
                                                  Stored in localStorage as code_arena_user

Admin logs in   →  POST /api/admin/login      →  Validates role=ADMIN
                                                  Stored in localStorage as code_arena_admin

On app load     →  GET /api/users/{id}        →  Validates stored session still exists in DB
                                                  Clears localStorage if user not found (DB reset)
```

---

## 📝 Notes

- The H2 database file is stored at `backend/data/codearena.mv.db` and is excluded from git. Data persists across server restarts.
- Problem seeding (`DataInitializer.java`) only runs when the `problems` table is empty — safe on every restart.
- The `schema.sql` uses `CREATE TABLE IF NOT EXISTS` on all tables — safe on every restart.
- `data.sql` uses `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` for migrations and `MERGE INTO` for upserts — safe on every restart.
- Admin users are filtered out of all leaderboard queries, rank calculations, and user-facing stats at the service layer.
