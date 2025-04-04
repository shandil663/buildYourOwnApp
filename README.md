# TaskGenius – AI-Powered Smart Planner 🧠📅

TaskGenius is a voice-enabled, AI-assisted productivity app built as part of the Rapido Android Intern Project. The app helps users organize their tasks intelligently, track progress visually, and stay ahead of their schedules — all wrapped in a sleek, modern UI.

---

## 🚀 Features

- 🎙️ **Voice Input for Tasks** – Add tasks using natural speech via speech-to-text.
- 🤖 **AI-Powered Categorization** – Auto-assign categories and labels based on task content.
- ⏱ **Smart Scheduling** – Automatically adjusts the task order based on due time and priority.
- 📊 **Donut Chart + Timer** – Visual representation of task progress (Elapsed, In Progress, Remaining).
- 🧠 **Task Status Detection** – Dynamically marks tasks as "Upcoming", "Ongoing", "Completed", or "Expired".
- 💾 **Offline Support** – Persist data locally using Room database.
- ✨ **Smooth Animations** – One-time color animation for donut chart upon screen entry.
- 📱 **Responsive UI** – Built with Jetpack Compose, clean design, and state-driven UI updates.

---

## 🛠️ Tech Stack

| Layer          | Technologies Used                                                                 |
|----------------|------------------------------------------------------------------------------------|
| **Language**   | Kotlin                                                                             |
| **Architecture** | MVVM + Clean Architecture (Presentation, Data Layers)                  |
| **UI**         | Jetpack Compose, Material 3                                                       |
| **Persistence**| Room Database (SQLite abstraction)                                                |
| **Async Work** | Kotlin Coroutines + Flow                                                          |
| **Navigation** | Navigation Compose                                                                |
| **Other APIs** | Gemini API, Speech-to-Text API                                               |
| **Charts**     | Custom Canvas Drawing for Donut Chart & Timer                                     |

---

## 📸 Demo

Check out the video demo showcasing full functionality *https://drive.google.com/drive/folders/1MxzyxrJb8uYiGPAvUqC1Tila2drLtuhN?usp=sharing*

---

## ⚠️ Known Limitations

- Smart scheduling logic is basic and rule-based.
- Currently portrait-mode only.
- No cloud sync.
- UI can be improved.

---

## 🛤️ Future Improvements

- 🧠 Smarter scheduling using ML models or user behavior tracking.
- ☁️ Cloud sync with Firebase/Google account.
- 📈 Weekly progress stats & insights.

---
