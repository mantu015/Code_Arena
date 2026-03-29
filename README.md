# Code Arena 🏆

Code Arena is a competitive programming platform designed to simulate environments like LeetCode. It features a premium React-based frontend with an integrated Monaco Code Editor, paired with a robust Spring Boot backend handling logic, code submissions, and a points-based leaderboard system.

---

## 🚀 Tech Stack

- **Frontend**: React (Vite), vanilla CSS (Custom Glassmorphism Design System), `lucide-react` for icons, `@monaco-editor/react` for the in-browser IDE.
- **Backend**: Spring Boot 3, Java 17, Spring Data JPA, H2 In-Memory Database.

---

## 🛠️ Prerequisites

To run this project locally, ensure you have the following installed on your machine:
- [Node.js](https://nodejs.org/) (v16.x or higher recommended)
- [Java Development Kit (JDK)](https://adoptium.net/) (v17 or higher recommended)
- Maven (optional, if you're not using the IDE to build)

---

## 💻 Getting Started

This repository is split into two independent parts: the `frontend` application and the `backend` server. 

### 1. Starting the Backend Server (Spring Boot)

The backend handles API requests, database persistence, and simulated code execution logic.

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run the Spring Boot application using your preferred IDE (like IntelliJ IDEA or Eclipse) by running the `CodeArenaApplication.java` main class.
3. The server will launch by default on **`http://localhost:8080`**.
4. You can view your H2 SQL database at **`http://localhost:8080/h2-console`** (JDBC URL: `jdbc:h2:mem:codearena`, Username: `sa`, Password: *blank*).

### 2. Starting the Frontend UI (React)

The frontend contains the interactive platform interface and coding workspace.

1. Open a new terminal window and navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install the necessary NPM packages:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. Access the web interface at **`http://localhost:5173`** (or the port specified in your terminal output).

---

## 🔗 Connecting Frontend & Backend

The frontend and backend communicate via a REST API. To ensure seamless integration, follow these configuration steps:

### 1. API Configuration
The frontend uses an environment variable to determine the backend API location. 
- **File**: `frontend/.env`
- **Variable**: `VITE_API_BASE_URL` (Default: `http://localhost:8080`)

If your backend is running on a different port or host, update this file and restart the frontend development server.

### 2. CORS Handling
The backend is configured to allow requests from the standard Vite development port (`http://localhost:5173`). 
- **Config file**: `backend/src/main/resources/application.properties`
- **Property**: `spring.mvc.cors.allowed-origins`

---

## 🔐 Authentication & Gated UI

Certain features are protected and only become visible after a user signs in:

- **Anonymous Users**: Can view the landing page and basic features. "Start Coding", "Leaderboard", and navigation links are hidden.
- **Signed-in Users**: Gain full access to problem sets, the interactive workspace, and the global leaderboard.

*Simply use the "Sign In" or "Sign Up" buttons in the navigation bar to unlock the full potential of Code Arena!*
