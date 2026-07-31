# Contributing to SafeReach

Thank you for your interest in contributing to the SafeReach emergency dispatch platform! We welcome contributions that improve the reliability, design, and performance of our services.

## Code Contribution Flow

1. **Fork the Repository**: Create a personal copy of the repository on GitHub.
2. **Clone the Project**: Download the code to your local development system.
3. **Create a Branch**: Use a descriptive name (e.g. `feature/live-telemetry` or `bugfix/login-credentials`).
4. **Implement and Verify**:
   - Ensure the React frontend builds successfully (`npm run build`).
   - Run Selenium E2E test suites locally (`node selenium-tests/tests/login-tests.js`).
5. **Commit Changes**: Use clean, descriptive commit messages.
6. **Open a Pull Request**: Submit your changes to the `main` branch of the upstream repository.

## Coding Style Rules
* Maintain clean TypeScript formatting in all React pages.
* Ensure SQL queries use prepared statement syntax in PHP.
* Always clean up unused imports and resources to ensure compilation succeeds.
