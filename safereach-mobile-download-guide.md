# 📱 SafeReach Android Mobile App Installation Guide

This guide details how to download, install, and configure the SafeReach WebView Android app on your physical mobile phone.

---

## 🛠️ Step 1: Connect to the Shared Local Network

For the mobile application to connect to your computer's database and server, your phone **must** be on the same local network interface as your computer.

1. **Start Mobile Hotspot:**
   * Turn on the **Mobile Hotspot** on your phone.
   * Connect your **computer** to your phone's hotspot.
2. **Verify IP Address:**
   * Your current server IP address on this network is: **`10.98.13.174`**
   * Keep the development server running on your computer:
     ```bash
     npm run dev
     ```

---

## 📥 Step 2: Download the App directly on your phone

Since the dev server is exposed to your local network, you can download the application package (`.apk`) directly to your phone via Wi-Fi:

1. Open the browser (Chrome / Safari) on your mobile phone.
2. Enter the following URL to download the file directly:
   👉 **`http://10.98.13.174:3000/safereach-app-debug.apk`**
3. Tap **Download Anyway** if your browser warns you about installing files from external links.

> [!TIP]
> Alternatively, you can copy the file **`safereach-app-debug.apk`** from the root of your project directory directly to your phone using a USB cable.

---

## ⚙️ Step 3: Enable Installation of Unknown Apps

Android blocks installing applications outside of the Google Play Store by default. You need to enable this permission:

1. Go to **Settings** on your phone.
2. Navigate to **Apps** ➔ **Special App Access** ➔ **Install Unknown Apps**.
3. Select the app you used to download the file (e.g. **Chrome** or **My Files / File Manager**).
4. Toggle the switch to **Allow from this source**.

---

## 🚀 Step 4: Install and Run the App

1. Open your phone's **File Manager** or **Downloads** folder.
2. Tap on the downloaded file: **`safereach-app-debug.apk`**.
3. Click **Install** and wait for it to complete.
4. Open the **SafeReach** app.
5. **Location Permission:** Tap **Allow Location Access** when prompted to enable real-time GPS tracking on your map.

---

## 📡 Backend Connectivity Reference
* **Frontend Web URL:** `http://10.98.13.174:3000/`
* **Backend API Base URL:** `http://10.98.13.174/safereach/`
