# SVU LMS - Native Android App

A feature-rich, native Android application designed to provide students of the Syrian Virtual University (SVU) with a seamless and efficient mobile experience for the Learning Management System (LMS). This project was developed as a graduation requirement for the Bachelor's degree in Communications Technology.

---

## 📱 App Preview

A quick look at the app's splash screen and the main dashboard, available in both English and Arabic, with full dark mode support.

<p align="center">
  <img src="https://raw.githubusercontent.com/Omar-Al-Boushi/Android-LMS-App/master/demo/Animation.gif" width="260" alt="Splash Screen">&nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Omar-Al-Boushi/Android-LMS-App/master/demo/Animation2.gif" width="260" alt="English Dashboard">&nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/Omar-Al-Boushi/Android-LMS-App/master/demo/Animation3.gif" width="260" alt="Arabic Dashboard & Dark Mode">
</p>

---

## 🎯 The Problem

The official web-based LMS for the Syrian Virtual University, while functional on desktops, offers a suboptimal experience on mobile devices. Students face several challenges daily, including:

* **Repetitive Logins:** No session persistence, requiring frequent re-authentication.
* **Poor Mobile UX:** Non-optimized web interfaces that are difficult to navigate on small screens.
* **Lack of Overview:** No central dashboard to quickly view academic progress and upcoming deadlines.
* **Inefficient Navigation:** Slow page loads and complex navigation paths to access essential resources like course materials and assignments.

This project aims to bridge this "digital efficiency gap" by providing a dedicated, mobile-first solution.

## ✨ Features

This application is built from the ground up to address the shortcomings of the web version and introduce new, student-centric features.

* **📊 Comprehensive Dashboard:** An intuitive dashboard provides an at-a-glance overview of academic progress, including course status (passed, registered, remaining) and assignment statistics.
* **📚 Advanced Course Management:** Easily browse, search, and filter courses. Switch between card and list views, and add courses to a "Favorites" list for quick access.
* **📄 Seamless File & Assignment Handling:** Download course materials and view them offline. Submit assignments through a simple and reliable upload interface.
* **🔔 Smart Notifications & Calendar:** Stay updated with important deadlines and announcements through a dedicated notifications screen and an integrated academic calendar.
* **👤 Full Profile Management:** View and edit your personal profile, including contact information and a personal bio.
* **🎨 Personalized Experience:**
    * **Dual Language Support:** Full UI support for both Arabic and English.
    * **Light & Dark Modes:** Switch between themes for comfortable viewing in any lighting condition.
* **🔑 Secure & Persistent Login:** A secure login system with a "Remember Me" option to eliminate repetitive logins.
* **✈️ Offline Capabilities:** Designed to work efficiently even with a weak or intermittent internet connection by caching essential data locally.

## 🛠️ Tech Stack & Architecture

This project was built using modern, industry-standard tools and practices to ensure a high-quality, maintainable, and scalable application.

* **Core:**
    * **Language:** Java
    * **IDE:** Android Studio
* **Architecture:**
    * **Pattern:** Repository Pattern to abstract data sources and separate concerns.
    * **Structure:** Organized into logical packages (`ui`, `data`, `utils`) for clean code.
* **Data & Storage:**
    * **Local Database:** SQLite for robust, offline-first data storage.
    * **User Preferences:** SharedPreferences for managing user settings like theme and language.
* **Design & UI:**
    * **UI/UX Design:** Figma for prototyping and interface design.
    * **Design System:** Material Design 3 for a modern and consistent user interface.
    * **Graphics:** Adobe Illustrator & Photoshop for creating visual assets.
* **Version Control:**
    * **Platform:** Git & GitHub for source code management.

## 🏛️ Project Structure

The application follows a clean architecture pattern that separates the UI, data, and utility logic into distinct packages.

```
org.svuonline.lms
│
├── data/
│   ├── db/           # Database helper and contract classes
│   ├── model/        # POJO model classes for entities
│   └── repository/   # Repository classes to handle data operations
│
├── ui/
│   ├── activities/   # Activity classes for each screen
│   ├── adapters/     # RecyclerView adapters
│   └── fragments/    # Fragment classes for the dashboard sections
│
├── Notifications/
│
└── utils/
    └── # Utility classes for common tasks
```

This structure ensures that the application is:

* **Maintainable:** Easy to locate and modify code.
* **Scalable:** Simple to add new features without refactoring the entire app.
* **Testable:** Logic is decoupled from the UI, making unit testing more straightforward.

## 👥 Authors

This project was developed by:

* **Omar Al Boushi**
* **Lana Kaddourah**
* **Abeer Kharfan**

### Supervision

* **Dr. Abdo Alkhoury**

---

*This project is submitted as a partial fulfillment of the requirements for the Bachelor's degree in Communications Technology at the Syrian Virtual University (SVU), Semester S24.*
