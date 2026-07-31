# SafeReach Android Mobile App Installation Guide

This document provides step-by-step instructions on how to transfer, install, and run the SafeReach Android mobile application (`safereach-app-debug.apk`) on your physical mobile phone.

---

## Step 1: Locate the Installation File (.APK)
On your computer, the compiled installation file is saved at the root of your project:
* File Path: `c:\Users\rupes\AndroidStudioProjects\SAFEREACH3\safereach-app-debug.apk`

---

## Step 2: Transfer the File to your Mobile Phone
Choose any of these easy methods to send the file to your mobile phone:

### Method A: USB Cable (Recommended)
1. Connect your Android phone to your computer using a USB cable.
2. On your phone, change the USB connection mode to **File Transfer / MTP**.
3. On your computer, open File Explorer and drag `safereach-app-debug.apk` into your phone's internal storage (e.g. the **Downloads** folder).

### Method B: Google Drive / Cloud
1. Open Google Drive on your computer browser.
2. Upload the `safereach-app-debug.apk` file.
3. Open the Google Drive app on your phone, locate the file, and tap **Download**.

---

## Step 3: Enable Installation from Unknown Sources
By default, Android blocks installing apps outside of the Google Play Store. To install the app:
1. Open **Settings** on your phone.
2. Go to **Apps & Notifications** -> **Special App Access** -> **Install Unknown Apps** (or search for *"unknown apps"* in Settings).
3. Select your file manager app (or Google Drive/Chrome) and toggle the switch to **Allow from this source**.

---

## Step 4: Install and Run the App
1. Open the **Files / File Manager** app on your mobile phone.
2. Navigate to your **Downloads** folder.
3. Tap on `safereach-app-debug.apk`.
4. Click **Install**. Once finished, tap **Open**.

---

## Step 5: Connect to the Local Backend Server
To fetch live data and connect drivers with users:
* Make sure your phone is connected to the **same Wi-Fi network** as your computer.
* The mobile app is pre-configured to communicate directly with your computer's local IP address: **`http://172.23.50.133/safereach/`**.
