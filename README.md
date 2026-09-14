# 📢 AnnounceUPI

<p align="center">
  <img src="https://raw.githubusercontent.com/rpy2006/Upi-Announce-App/refs/heads/main/assets/trial.png"
       width="220"
       alt="AnnounceUPI">
</p>

<h2 align="center">Instant Payment Announcements</h2>

<p align="center">
  An Android app that detects incoming UPI payments and announces them through voice.
</p>

<p align="center">
  <a href="https://github.com/rpy2006/Upi-Announce-App/releases/download/v1.0.0/Upi-Announce-App-v1.0.0.apk">
    <img src="https://img.shields.io/badge/Download%20APK-v1.0.0-2ea44f?style=for-the-badge&logo=android&logoColor=white"
         alt="Download APK">
  </a>
</p>

<p align="center">
  <a href="https://github.com/rpy2006/Upi-Announce-App">Repository</a>
  •
  <a href="https://github.com/rpy2006/Upi-Announce-App/releases">Releases</a>
  •
  <a href="https://github.com/rpy2006/Upi-Announce-App/issues">Issues</a>
</p>

---

## 📱 About

**AnnounceUPI** is an Android application designed for shopkeepers, small businesses, and individuals who want an easy way to know when a digital payment has been received.

The app monitors supported payment notifications and bank SMS messages, detects payment amounts, records transactions, and announces successful payments using **Text-to-Speech**.

> **No need to constantly check your phone after every payment.**

---

## ✨ Features

- 🔊 **Voice Payment Announcements**
  - Automatically announces detected payment amounts.

- 🔔 **UPI Notification Detection**
  - Detects incoming payment notifications from supported apps.

- 💬 **SMS Payment Detection**
  - Detects supported payment confirmations received through SMS.

- 💰 **Automatic Amount Recognition**
  - Identifies received INR payment amounts.

- 📊 **Transaction History**
  - Keeps track of detected payments.

- 🔐 **Biometric App Lock**
  - Protect the application using biometric/device authentication.

- 🎨 **Multiple Themes**
  - Snow
  - Midnight
  - AMOLED
  - Sepia

- 🕵️ **Incognito Mode**
  - Continue announcements while hiding visual payment information.

- ⚙️ **Notification Settings**
  - Configure supported payment applications and detection behavior.

---

## 💳 Supported Payment Apps

| Payment App | Status |
|---|:---:|
| Google Pay | ✅ |
| PhonePe | ✅ |
| Paytm | ✅ |
| WhatsApp | ✅ |
| BHIM | ✅ |

> Detection depends on the notification/SMS format provided by the respective payment application or bank.

---

## 🔄 How It Works

```text
Payment Received
       │
       ▼
UPI App / Bank SMS
       │
       ▼
Payment Detection
       │
       ├───────────────┐
       ▼               ▼
Transaction        Amount
  Storage          Detection
       │               │
       └───────┬───────┘
               ▼
        Text-to-Speech
               │
               ▼
       🔊 Payment Announced
