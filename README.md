# Cluely Keyboard for Android

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
</p>

## 🎯 Overview

**Cluely Keyboard** is an Android IME (Input Method Editor) that captures screenshots and provides AI-powered chat assistance based on screen context. Inspired by Cluely, this keyboard brings vision-based AI directly into your typing experience.

## ✨ Features

### 📷 Screenshot Capture
- **One-tap screenshot** button integrated into the keyboard
- Captures current screen content
- Automatic processing with AI vision

### 💬 AI Vision Chat
- **Floating chat overlay** appears on screenshot
- **OpenRouter Vision API** analyzes screen content
- Ask questions about what's on your screen
- Context-aware AI responses

### ⌨️ Full Keyboard
- Standard QWERTY layout
- Lightweight and fast
- System-wide IME support

## 🏗️ Architecture

```
Cluely Keyboard MVP
├── IME Service (InputMethodService)
│   ├── QWERTY keyboard layout
│   └── Screenshot capture button
├── Screenshot Service (MediaProjection)
│   ├── Screen capture handling
│   └── Image processing
├── Chat Overlay (Floating Window)
│   ├── Jetpack Compose UI
│   ├── Chat interface
│   └── AI response display
└── OpenRouter Integration
    ├── Vision API client (Ktor)
    ├── GPT-4 Vision / Claude Vision
    └── Image to text analysis
```

## 🚀 Technology Stack

- **Language:** Kotlin 2.0
- **UI Framework:** Jetpack Compose + XML Views
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt/Dagger
- **Networking:** Ktor Client
- **Storage:** DataStore (API keys)
- **AI Provider:** OpenRouter API
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 35 (Android 15)

## 📦 Setup

### 1. Clone the repository
```bash
git clone https://github.com/filiksyos/cluely-keyboard-android.git
cd cluely-keyboard-android
```

### 2. Get OpenRouter API Key
1. Sign up at [openrouter.ai](https://openrouter.ai)
2. Get your API key from the dashboard

### 3. Build and Install
```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Enable the Keyboard
1. Go to **Settings → System → Languages & input → Virtual keyboard**
2. Enable **Cluely Keyboard**
3. Select it as your default keyboard

### 5. Configure API Key
1. Open any text field to activate keyboard
2. Tap the **Settings** icon
3. Enter your OpenRouter API key

## 🎮 Usage

### Taking Screenshots with AI Chat
1. Open any app with a text field
2. Activate Cluely Keyboard
3. Tap the **📷 Screenshot** button in the keyboard toolbar
4. Grant screen capture permission (first time only)
5. A floating chat bubble appears with screenshot analysis
6. Ask questions about the screen content
7. Get AI-powered context-aware responses

### Example Use Cases
- **Reading assistance:** "Summarize this article"
- **Translation:** "Translate this text to Spanish"
- **Explanations:** "Explain this code/math problem"
- **Data extraction:** "What's the address shown here?"
- **Shopping:** "Find cheaper alternatives to this product"

## 🔐 Privacy

- ✅ Screenshots processed only when you tap the button
- ✅ API key stored locally using encrypted DataStore
- ✅ No analytics or tracking
- ✅ Screenshots not saved to storage
- ✅ Open source - audit the code yourself

## 🛠️ Development

### Project Structure
```
app/src/main/java/dev/cluely/keyboard/
├── di/              # Hilt modules
├── domain/          # Use cases & models
├── data/            # Repositories & API
│   ├── api/        # OpenRouter client
│   ├── storage/    # DataStore
│   └── screenshot/ # MediaProjection service
├── ui/
│   ├── ime/        # Keyboard IME service
│   ├── overlay/    # Floating chat window
│   └── settings/   # Settings screen
└── utils/           # Extensions & helpers
```

### Key Components

**1. CluelyIMEService** - Main keyboard service
```kotlin
class CluelyIMEService : InputMethodService() {
    // Handles keyboard input and screenshot button
}
```

**2. ScreenshotService** - Captures screen
```kotlin
class ScreenshotService {
    // Uses MediaProjection API
}
```

**3. ChatOverlayService** - Floating chat
```kotlin
class ChatOverlayService : Service() {
    // WindowManager overlay with Compose UI
}
```

**4. OpenRouterClient** - AI API integration
```kotlin
class OpenRouterClient(private val httpClient: HttpClient) {
    // Vision API calls
}
```

## 📋 Roadmap

- [ ] **v1.0 (Current MVP)**
  - [x] Basic IME keyboard
  - [x] Screenshot capture
  - [x] Floating chat overlay
  - [x] OpenRouter vision integration
  - [ ] Settings screen
  - [ ] Permission handling

- [ ] **v1.1**
  - [ ] Multiple AI model support
  - [ ] Chat history
  - [ ] Copy responses to clipboard
  - [ ] Screenshot annotation

- [ ] **v1.2**
  - [ ] Voice input for questions
  - [ ] Offline mode with local vision models
  - [ ] Custom keyboard themes
  - [ ] Quick action buttons

## 🤝 Contributing

Contributions are welcome! This is an MVP, so there's lots of room for improvement:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

MIT License - See [LICENSE](LICENSE) for details.

## 🙏 Acknowledgments

- **LiteKite/Android-IME** - Foundation for IME implementation
- **Cluely** - Inspiration for vision-based screen assistance
- **OpenRouter** - Multi-model AI API platform

## ⚠️ Disclaimer

This is an experimental MVP. Use at your own risk. Screenshots are sent to OpenRouter's API for processing. Review their [privacy policy](https://openrouter.ai/privacy) before use.

---

**Built with ❤️ for better mobile productivity**