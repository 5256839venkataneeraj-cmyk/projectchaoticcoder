# 🎨 Campus Route-to-Art

> **Transforming the routine university commute into collectible algorithmic digital art.**  
> *Built for DevJams Hackathon*

---

## 🌟 Project Overview

**Campus Route-to-Art** is a full-stack mobile platform that converts everyday university footsteps and campus commutes into unique, scalable generative vector art. Instead of reducing human movement to clinical step counts and generic calorie metrics, our engine captures real-time geographic breadcrumbs and normalizes them onto an algorithmic 500x500 2D vector canvas. By utilizing Catmull-Rom to Cubic Bézier spline math, raw GPS coordinates are smoothed into continuous geometric contours, minting one-of-a-kind SVG artworks classified into dynamic rarity tiers (`Common`, `Uncommon`, `Rare`, `Epic`, and `Legendary`).

Designed with a sleek cyberpunk dark-mode aesthetic, students can record real-world campus treks or trigger our built-in **15-Second Hackathon Live Simulator** to generate recognizable landmark geometry (such as the 5-Pointed Star of Campus Art). As walks are logged, users build their personal art vault, unlock streak milestones, inspect raw SVG vector geometry, and compete on the real-time campus leaderboard.

---

## 📱 UI Showcase & Visual Journey

| Live Walk Tracker & GPS Simulator | Generative SVG Minting Reveal |
| :---: | :---: |
| ![Live Walk Tracker Screen](https://via.placeholder.com/400x800/090D16/38BDF8?text=Walk+Tracker+%26+Simulator) | ![Minting Celebration Modal](https://via.placeholder.com/400x800/090D16/EC4899?text=Generative+SVG+Minting) |

| Personal Art Vault Gallery | Real-Time Campus Leaderboard |
| :---: | :---: |
| ![Personal Art Gallery Screen](https://via.placeholder.com/400x800/090D16/8B5CF6?text=Personal+Art+Gallery) | ![Campus Leaderboard Screen](https://via.placeholder.com/400x800/090D16/FCD34D?text=Campus+Leaderboard) |

---

## 🛠️ Technology Stack

- **Frontend Mobile Application:**
  - **React Native & Expo:** Cross-platform mobile client for iOS, Android, and Web.
  - **React Navigation:** High-performance bottom tab navigation (`Walk Tracker`, `Art Gallery`, `Leaderboard`).
  - **react-native-svg (`SvgXml`):** Native hardware-accelerated vector graphics rendering.
  - **expo-sharing & expo-file-system:** Seamless cross-platform artwork export and sharing.
  - **Design System:** Custom Dark Cyberpunk theme tokens with neon cyan, purple, emerald, and gold accents.

- **Backend API & Generative Engine:**
  - **Node.js & Express:** Scalable REST API with modular controllers and routes.
  - **MongoDB & Mongoose:** Resilient data schemas with automatic in-memory fallbacks for development.
  - **Generative Art Engine:** Dynamic 500x500 bounding box normalization, aspect ratio preservation, and Catmull-Rom to Cubic Bézier spline interpolation.
  - **Gamification Engine:** 24-to-48-hour rolling streak retention algorithm and total distance tracking.

---

## 🚀 Quick Start & Local Setup

### 1. Prerequisites
- **Node.js** (v18 or higher)
- **npm** (v9 or higher)
- **Expo Go App** (on iOS or Android for physical device testing) or an emulator.

---

### 2. Backend Setup & Seeding

Open a terminal in the root directory:

```bash
# Navigate to backend directory
cd backend

# Install dependencies
npm install

# (Optional) Seed the database with 10 competitive campus runners
npm run seed

# Start backend server (Runs on http://localhost:5000)
npm start
```

---

### 3. Frontend Setup & Launch

Open a second terminal in the root directory:

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start the Expo Dev Server (Runs on http://localhost:8081)
npx expo start
```

Press:
- `a` to open in Android Emulator
- `i` to open in iOS Simulator
- `w` to open in Web Browser
- Scan the QR code with **Expo Go** on your physical phone!

---

## ⚡ Hackathon Live Demo Walkthrough

1. Open the app to the **Walk Tracker** tab.
2. Tap the top-right button: **`⚡ Simulate Walk`** to open the **15s Hackathon Live Simulator**.
3. Tap **`🚀 Start 15s Star Walk Simulation`**.
4. Watch mock GPS coordinates stream every 1 second tracing the **5-Pointed Star of Campus Art** with live progress tracking.
5. At 15 seconds, the auto-stop routine uploads the session, mints the generative SVG artwork, and opens the celebration reveal modal!
6. Navigate to the **Art Gallery** to inspect your minted piece, and check the **Leaderboard** for updated streak and distance rankings.

---

## 📂 Repository Structure

```
├── backend/
│   ├── src/
│   │   ├── controllers/    # Session, Art, and User controllers
│   │   ├── models/         # Mongoose schemas (WalkSession, Artwork, User)
│   │   ├── routes/         # Express REST API endpoints
│   │   ├── services/       # Generative Art spline & normalization engine
│   │   └── utils/          # Database seeding scripts
│   ├── server.js           # Server entry point
│   └── package.json
│
├── frontend/
│   ├── src/
│   │   ├── components/     # ArtworkSvg, ArtworkCard, ArtworkModal
│   │   ├── screens/        # TrackerScreen, GalleryScreen, LeaderboardScreen, HomeMap
│   │   ├── services/       # Axios API client & Expo Sharing service
│   │   └── theme.js        # Centralized theme tokens & design system
│   ├── App.js              # Tab navigation entry point
│   └── package.json
│
├── PITCH_SCRIPT.md         # 3-minute DevJams hackathon pitch script
└── README.md               # Repository documentation
```

---

## 📄 License & Attribution

Developed with ❤️ for **DevJams Hackathon**. MIT License.
