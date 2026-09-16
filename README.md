<p align="center">
  <img src="https://github.com/rpy2006/Upi-Announce-App/blob/main/assets/file_000000007170820e8291ca19348fb674.png" alt="AnnounceUPI banner" width="100%">
</p>
<h1 align="center">AnnounceUPI</h1>

<p align="center">
  Real-time UPI payment monitoring with instant voice announcements — built for merchants, shopkeepers, and anyone who wants to hear their payments come in.
</p>

<p align="center">
  <a href="https://github.com/rpy2006/Upi-Announce-App/releases/download/v1.0.0/Upi-Announce-App-v1.0.0.apk">
    <img src="https://img.shields.io/badge/Download-APK-3D8BFF?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
  </a>
  <img src="https://img.shields.io/badge/Platform-Android-black?style=for-the-badge" alt="Platform">
  <img src="https://img.shields.io/badge/Version-1.0.0-informational?style=for-the-badge" alt="Version">
</p>

---

## About

**AnnounceUPI** listens for incoming UPI payment notifications and SMS alerts, then announces the amount out loud — so you never have to unlock your phone or check your notification bar to know a payment came through. Built for busy checkout counters, small shops, and anyone accepting UPI payments on the go.

Everything runs **on-device**. Your transaction history and settings stay in a local database on your phone — nothing is uploaded to a server unless you explicitly export it yourself.

## Features

- 🔊 **Real-time voice announcements** — detects payments from notifications and SMS, and reads out the amount instantly
- 🌐 **Multi-language announcements** — choose the language your amounts are announced in
- 🎚️ **Customizable voice settings** — speech speed, custom prefix phrases, repeat announcements, Bluetooth-only mode
- 📊 **Dashboard & insights** — daily/weekly/monthly totals, a progress ring toward your daily target, trend charts, top payment sources, and peak-hour activity
- 🎯 **Daily targets** — set a goal and get notified (with a dedicated announcement) when you hit it
- 🔕 **Do Not Disturb & Daily Summary** — quiet hours for announcements, plus an end-of-day summary
- 🔒 **Security & privacy** — App Lock, Stealth Mode (hide app activity), and Incognito Mode
- 🎨 **Multiple themes** — Snow, Midnight, AMOLED, and Sepia
- 🚫 **Excluded apps & minimum amount filters** — fine-tune exactly which payments get announced
- 💾 **Data control** — export transactions to CSV, export/import your app settings, auto-delete old transactions, and clear history anytime

## Screenshots

<p align="center">
  <img src="https://github.com/rpy2006/Upi-Announce-App/blob/main/assets/file_000000007170820e8291ca19348fb674.png" alt="AnnounceUPI preview" width="70%">
</p>

## Download

Grab the latest release APK here:

**[⬇️ Download AnnounceUPI v1.0.0 (APK)](https://github.com/rpy2006/Upi-Announce-App/releases/download/v1.0.0/Upi-Announce-App-v1.0.0.apk)**

> Since this isn't distributed via the Play Store, you'll need to allow "Install from unknown sources" for your browser or file manager when installing the APK.

## Installation

1. Download the APK using the link above.
2. Open the downloaded file on your Android device.
3. If prompted, allow installation from unknown sources.
4. Follow the in-app setup to grant notification access and SMS permissions (required to detect payments).

## Tech Stack

- **Language:** Java
- **Platform:** Android (native, XML layouts)
- **Storage:** Local SQLite database — no cloud backend
- **Architecture:** Fragment-based UI with a shared `TransactionRepository` and `SharedPreferences`-backed settings

## Privacy

AnnounceUPI reads notification and SMS content only to detect payment amounts for announcements. This data is never transmitted anywhere — it lives entirely in a local database on your device unless you choose to export it yourself (e.g. via the in-app CSV export).

## Contributing

Issues and pull requests are welcome. If you run into a bug or have a feature idea, feel free to open an issue on this repository.

## License

This project is provided as-is. Check the repository for license details, or contact the developer for usage terms.

---

<p align="center">Made by Yadav Enterprises</p>
