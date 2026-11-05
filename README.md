# SafeReach — Emergency Ambulance Dispatch & Tracking Platform

SafeReach is a comprehensive, real-time emergency responder dispatch and live tracking platform designed to bridge the gap between citizens in distress, emergency drivers, and hospitals. It consists of three fully integrated subsystems:
1. **Citizen Portal (Web & App)**: For booking emergency ambulances, tracking responder location in real-time, and updating medical files.
2. **Driver Portal (Mobile App & Web)**: For accepting emergency missions, streaming live telemetry coordinates, and updating vitals.
3. **Admin Dashboard (Command Center)**: For monitoring active dispatches, managing citizens/drivers inventory, reviewing performance reports, and configuring policies.

---

## Technical Stack

### 💻 Web Frontend (React)
* **Framework**: React 18, TypeScript, Vite
* **Routing & State**: React Router DOM, Context API
* **Styling**: Tailwind CSS / Vanilla CSS Glassmorphism
* **Map Services**: Google Maps JavaScript API (Directions & Overlays)

### 📱 Mobile App (Android Native)
* **Language & Build**: Kotlin, Gradle
* **Networking**: Retrofit 2, OkHttp 3, Gson
* **Architecture**: MVVM (Model-View-ViewModel)

### ⚙️ Backend & API (PHP)
* **Language**: Native PHP (REST API architectures)
* **Web Server**: Apache (XAMPP Server)
* **Authentication**: JSON Web Token (JWT) Bearer Authorization

### 🗄️ Database
* **Database**: MySQL (XAMPP Server)
* **Storage Engines**: InnoDB, composite unique indexing

---

## Core Features
* **Live GPS Telemetry Tracking**: Real-time driver coordinates sync to database and auto-pan viewer bounds for citizens.
* **Smart Auto-Dispatch**: Automatic assignment of the closest active ambulance to pending SOS bookings.
* **Bed Capacity Alerts**: Instant lookup of surrounding hospitals' general, ICU, and NICU bed vacancies during transit.
* **Unified Security Rules**: Dynamic password resets, JWT authentication guards, and role-based access control.

---

## Folder Structure

```text
SAFEREACH3/
├── .github/workflows/       # GitHub Actions automated workflows
├── app/                     # Android Native mobile application (Kotlin)
├── backend/                 # PHP REST API endpoints & database connection
├── database/                # Schema definitions & database migrations
├── frontend/                # React Web Portal UI (Vite + TS)
├── selenium-tests/          # E2E web testing suites & load test scripts
├── appium-tests/            # E2E appium mobile testing scripts
└── testing-reports/         # Consolidating test spreadsheets (1,800 cases)
```

---

## Local Setup & Installation

### 1. Database Setup (MySQL)
1. Open XAMPP Control Panel and start **Apache** and **MySQL**.
2. Go to `http://localhost/phpmyadmin/` and create a database named `safereach`.
3. Import the schema file located at `database/schema.sql`.

### 2. Backend Configuration
1. Make sure your local repository folder is symlinked or placed inside your XAMPP server's server root (`htdocs/safereach`).
2. Verify connection variables inside `backend/config/db.php`.

### 3. Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install npm packages:
   ```bash
   npm install
   ```
3. Run the Vite development server:
   ```bash
   npm run dev
   ```

### 4. Android Studio Setup
1. Open the project root directory in **Android Studio**.
2. Verify the API base URL in `app/src/main/java/com/example/safereach/network/RetrofitClient.kt` points to your computer's local IP address (e.g., `http://10.98.13.174/safereach/`).
3. Compile and build the debug APK using Gradle wrapper:
   ```bash
   .\gradlew.bat assembleDebug
   ```

---

## API Documentation

### Authentication Endpoints
* `POST /api/auth/login.php` — User credentials verification (returns JWT token).
* `POST /api/auth/register.php` — Create user account.
* `POST /api/auth/change_password.php` — Change password for current session.

### Dispatch & Tracking Endpoints
* `POST /api/requests/create.php` — Create new emergency ambulance dispatch request.
* `GET /api/requests/track.php` — Get active driver telemetry coordinates.
* `POST /api/driver/location.php` — Stream driver coordinates from GPS watch.
* `GET /api/hospitals/list.php` — Fetch available hospital bed capacities.
