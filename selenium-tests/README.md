# SafeReach E2E Test Suite Documentation

This directory contains the End-to-End (E2E) automated testing suite and performance load-testing utilities for the SafeReach platform.

---

## 📂 Directory Structure

```text
selenium-tests/
├── tests/
│   └── login-tests.js        # Web E2E Selenium tests (Chrome)
├── test_cases.csv            # 300 core test cases matrix (Excel/Sheets ready)
├── generate_test_cases.py    # Python script compiling test_cases.csv
├── generate_all_reports.py   # Python script compiling the 1800 consolidated reports
└── load-test.js              # Native Node.js performance HTTP benchmarking tool
```

---

## 💻 Local Testing Setup & Execution

### 1. Web E2E Tests (Selenium)

#### Prerequisites
* Install [Google Chrome](https://www.google.com/chrome/).
* Install [Node.js](https://nodejs.org/) (version 18 or higher).

#### Running the tests
1. Install the Selenium WebDriver client:
   ```bash
   npm install selenium-webdriver
   ```
2. Make sure your local frontend is running:
   ```bash
   cd frontend
   npm run dev
   ```
3. Execute the E2E script:
   ```bash
   node selenium-tests/tests/login-tests.js
   ```

---

### 2. Mobile E2E Tests (Appium)

#### Prerequisites
* Install [Appium Server](https://appium.io/).
* Install [Android Studio & Emulator](https://developer.android.com/studio).

#### Running the tests
1. Install Appium WebDriver client:
   ```bash
   npm install webdriverio
   ```
2. Start the Appium Server:
   ```bash
   appium
   ```
3. Run the Appium script:
   ```bash
   node appium-tests/tests/appium-login-tests.js
   ```

---

### 3. Baseline Load & Stress Testing

To stress-test your local Apache/MySQL API endpoints under 100 concurrent virtual users for 1 minute:
```bash
node selenium-tests/load-test.js
```

---

## 🚀 CI/CD Automation (GitHub Actions)

The platform is integrated with GitHub Actions via `.github/workflows/e2e-testing.yml`.

### Workflow Stages
On every `push` or `pull_request` to `main`, the runner automatically:
1. Initializes a Node environment.
2. Installs testing clients.
3. Executes the test suites in parallel.
4. Generates and uploads Excel-compatible `.csv` reports as downloadable **workflow build artifacts**.

---

## 🛠️ Troubleshooting Guide

### 1. `Error: Cannot find module 'selenium-webdriver'`
* **Cause**: Testing libraries are missing in the active execution path.
* **Fix**: Run `npm install selenium-webdriver` in the project root.

### 2. `Error: Connection Refused` (Local Server)
* **Cause**: XAMPP Apache or MySQL service is stopped.
* **Fix**: Open the **XAMPP Control Panel** on your computer and verify that Apache and MySQL are running (highlighted in green).

### 3. Mobile Emulator cannot connect to database
* **Cause**: The base URL is configured with an inactive IP address.
* **Fix**: Ensure your computer and device/emulator are on the same Wi-Fi. Verify that the `BASE_URL` in `app/src/main/java/com/example/safereach/network/RetrofitClient.kt` matches your active local IP address.
