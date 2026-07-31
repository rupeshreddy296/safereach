# Changelog

All notable changes and updates to the SafeReach platform will be documented in this file.

## [1.1.0] - 2026-07-31

### Added
* Implemented functional administrative sub-pages including **Live Monitor**, **SOS Requests**, **Citizen Management**, **Reports**, **Notifications**, and **Settings** cards.
* Embedded **Change Password** security panels across all Citizen, Driver, and Admin portals.
* Introduced a localized **Nearby Emergency Facilities** panel listing dynamic ICU/General/NICU hospital bed capacities on the driver dispatch screen.
* Configured automated **Selenium** and **Appium** E2E testing pipelines inside `.github/workflows/e2e-testing.yml`.
* Built local **load-test.js** HTTP benchmarking utility.

### Fixed
* Fixed a race condition where Google Maps initialized blank if scripts were still loading during mount.
* Corrected Android `RetrofitClient` base URL configuration to point to active local Wi-Fi adapter IP `172.23.50.133`.
* Cleaned up redundant menu categories and unresolved TypeScript imports.
