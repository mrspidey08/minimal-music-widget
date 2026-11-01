# Minimal Music Widget (4:1)

A modern, ultra-lightweight Android music widget app built with sharp geometric controls, zero bloat, pure black AMOLED styling, and dynamic song title / custom text heading support.

---

## ✨ Features

- **4:1 Aspect Ratio**: Perfectly sized horizontal widget designed specifically for modern Android home screens.
- **Dynamic Song Title Header**:
  - Automatically fetches and displays the active song title from Spotify, YouTube Music, Apple Music, VLC, etc.
  - Automatically updates in real time whenever the song or track changes.
- **Settings Toggle (Song Title vs Custom Text)**:
  - Toggle between displaying the **Song Title** or **Custom Text** (e.g., `"MUSIC IS LIFE"`).
  - Configurable directly from the companion app.
- **Sharp & Bold Geometric Icons**: Custom vector drawables matching the minimalist design:
  - **Play (`▶`)**: Sharp right-pointing triangle.
  - **Pause (`❚❚`)**: Dual sharp rectangular bars.
  - **Previous (`|◀`)**: Sharp vertical bar with left-pointing triangle.
  - **Next (`▶|`)**: Sharp right-pointing triangle with vertical bar.
- **Dual-Layer Media Playback Control**:
  1. **Direct MediaSession Transport**: When notification listener access is enabled, directly commands active playback sessions.
  2. **Universal KeyEvent Fallback**: Dispatches system media key events (`KEYCODE_MEDIA_PLAY`, `KEYCODE_MEDIA_PAUSE`, `KEYCODE_MEDIA_PREVIOUS`, `KEYCODE_MEDIA_NEXT`) via `AudioManager` and broadcast receiver intents.
- **Ultra-Lightweight & Battery Efficient**: Pure native Android `RemoteViews` implementation—zero Compose bloat, instant response time, no background battery drain.

---

## 📱 Installation & Setup

### 1. Install the APK
The pre-compiled APK is ready at:
```
d:\Music widget\MinimalMusicWidget.apk
```
To install onto a connected device or emulator via ADB:
```bash
adb install "d:\Music widget\MinimalMusicWidget.apk"
```

### 2. Enable Media Session Access
1. Open the **Minimal Music Widget** app on your phone.
2. Tap **"GRANT NOTIFICATION ACCESS"**.
3. Toggle on **Minimal Music Widget** in the Android settings page.
   *(This gives the app permission to read active song titles and control playback).*

### 3. Add the 4:1 Widget to your Home Screen
1. Long-press any empty space on your Android home screen.
2. Select **Widgets**.
3. Scroll down and select **Minimal Music Widget (4:1)**.
4. Drag and drop it onto your home screen!
