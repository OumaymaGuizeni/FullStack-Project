# 🏊 Natation Portal - Swimming Club CMS & Member Portal

Natation Portal is a state-of-the-art, full-stack CMS and interactive Member Portal designed for swimming federations, clubs, coaches, and swimmers. The platform is divided into a robust administration panel (**BackOffice**) and a personalized subscriber dashboard (**FrontOffice**), unified inside a secure, high-performance web application.

---

## 🚀 Key Functional Modules

### ⚙️ BackOffice (Admin Portal)
*   **Club CMS**: Complete CRUD (Create, Read, Update, Delete) capability to manage club names, target cities, ranks, and trophy tallies.
*   **Active Swimmer & Coach Roster**: Add, modify, and delete active accounts, manage emails, passwords, addresses, phone numbers, and trophies.
*   **Relationship Management (Athlete Assignment)**: Associate swimmers and coaches to specific clubs dynamically.
*   **DB Constraints Soft-Delete**: Programmatic unassignment cascades that detach athletes first before club removal to prevent SQL database constraint crashes.

### 👤 FrontOffice (Member Portal)
*   **Club Hierarchy Chart**: Real-time visual tree mapping showing the club structure, assigned coaching staff, and active team athletes.
*   **Personal Profile Settings**: Swimmers and coaches can edit their profile info (email, address, phone number) and change passwords.
*   **Trophy Tracker**: Swimmers can input and record their achievements, immediately updating the backend database.
*   **Unified Auth Flow**: Auto-routes authenticated users to the correct interface based on their role (`ADMIN` ➔ `/dashboard` vs. `SWIMMER` / `COACH` ➔ `/dashboard-users`).

### 🌟 Advanced Functions
1.  **AI Real-Time Chatbot Assistant**: Leverages Java 11 `HttpClient` in the Spring Boot backend to actively query and scrape **Wikipedia API** and **DuckDuckGo API** networks, delivering instant answers on public swimming topics with desktop reference links.
2.  **AI Predictive Analytics (Future Trends)**: Features an integrated `AnalyticsService` forecasting future trophy gains. Displayed in Angular via a **Dual-Bar Performance Chart** (solid bars for current trophies, dashed bars for projected future season trophy targets).
3.  **Live Database Self-Correcting Sync**: Programmatically aggregates all assigned user trophies and updates the club's stored database count at query-time, keeping database states 100% accurate.

---

## 🛠️ Technological Stack

### ☕ Backend (Spring Boot)
*   **Framework**: Spring Boot 3.x
*   **Security**: Spring Security 6.x (Stateless **JWT** Token authentication & **BCrypt** Password salting and hashing)
*   **Database**: File-based **H2 Database** engine (H2 console enabled at `/h2-console`)
*   **ORM**: Spring Data JPA & Hibernate
*   **Language**: Java 17+

### 🅰️ Frontend (Angular)
*   **Framework**: Angular 17+
*   **Architecture**: Lightweight **Standalone Components**
*   **State Management**: Session token persistence using HTML5 `localStorage`
*   **Visual Design**: CSS Grid and Flexbox layouts, HSL design variables, and smooth animations

---

## 📁 Repository Structure

### ☕ Backend Structure (`backend/demo/src/main/java/com/natation/`)
*   `📂 config/` : Security filter chains, JWT filters, Exception handlers, and CORS rules.
*   `📂 controller/` : REST endpoints (`AuthController`, `ClubController`, `UserController`, `ChatbotController`).
*   `📂 service/` : Business logic and web scraping APIs (`ClubService`, `UserService`, `AnalyticsService`, `ChatbotService`).
*   `📂 repository/` : JPA interfaces accessing the H2 database.
*   `📂 entity/` : Java objects mapped to database tables (`User`, `Club`).
*   `📂 dto/` : Flat Data Transfer Objects for API security (`LoginRequest`, `RegisterRequest`, `PredictionDTO`).

### 🅰️ Frontend Structure (`frontend/src/app/`)
*   `📂 core/` : System infrastructure, guards (`admin.guard.ts`), and services (`user.service.ts`, `club.service.ts`).
*   `📂 shared/` : Shared TypeScript interfaces (`user.ts`, `club.ts`).
*   `📂 modules/` : Page features (`dashboard`, `dashboard-users`, `clubs`, `users`, `auth`).

---

## 🏁 How to Run the Application

### 1. Backend Setup (Spring Boot)
1.  Navigate to the `backend/demo` directory:
    ```bash
    cd backend/demo
    ```
2.  Build and run the application using Maven:
    ```bash
    ./mvnw spring-boot:run
    ```
3.  The backend will start on **`http://localhost:8080`**.
4.  *Note*: The database console is active at **`http://localhost:8080/h2-console`** (JDBC URL: `jdbc:h2:file:./data/natationdb`, Username: `sa`, Password: empty).

### 2. Frontend Setup (Angular)
1.  Navigate to the `frontend` directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm start
    ```
4.  Open your browser to **`http://localhost:4200`**.

---

## 🔑 Default Credentials (Seed Data)

The application automatically seeds a primary Administrator account upon startup:
*   **Username**: `admin`
*   **Email**: `admin@natation.com`
*   **Password**: `00000000`
