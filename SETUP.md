# 🚀 Cluely Keyboard - Setup & Development Guide

## Quick Setup

### 1. Prerequisites
- Android Studio (Hedgehog or later)
- JDK 17 or higher
- Android API 29+
- OpenRouter API key (free at https://openrouter.ai)

### 2. Clone & Build
```bash
git clone https://github.com/filiksyos/cluely-keyboard-android.git
cd cluely-keyboard-android
./gradlew build
```

### 3. Install APK
```bash
./gradlew installDebug
```

Or use Android Studio:
- Open project in Android Studio
- Click **Run > Run 'app'**
- Select your device/emulator

### 4. Enable Keyboard
1. Open **Settings → System → Languages & input → Virtual keyboard**
2. Tap **Manage keyboards**
3. Enable **Cluely Keyboard**
4. Return to **Virtual keyboard**
5. Select **Cluely Keyboard** as default

### 5. Configure API Key
1. Open the Cluely Keyboard settings app from your home screen
2. Enter your OpenRouter API key
3. Grant required permissions when prompted

## 📝 Development

### Project Structure
```
app/src/main/java/dev/cluely/keyboard/
├── CluelyKeyboardApp.kt          # Hilt application
├── di/
│   └── AppModule.kt              # Dependency injection
├── domain/
│   └── models/
│       └── ChatMessage.kt        # Data models
├── data/
│   ├── api/
│   │   └── OpenRouterClient.kt   # Vision API client
│   ├── storage/
│   │   └── ApiKeyStore.kt        # Encrypted settings
│   └── screenshot/
│       ├── ScreenshotManager.kt  # Screenshot orchestration
│       └── ScreenshotService.kt  # MediaProjection service
└── ui/
    ├── ime/
    │   ├── CluelyIMEService.kt   # Main keyboard service
    │   └── KeyboardView.kt       # Custom keyboard layout
    ├── overlay/
    │   └── ChatOverlayService.kt # Floating chat window
    └── settings/
        └── SettingsActivity.kt   # Settings UI
```

### Key Components

#### 1. **CluelyIMEService** - Keyboard Input
Extends `InputMethodService` and handles:
- QWERTY keyboard layout
- Text input capture
- Screenshot button trigger

```kotlin
// Custom key handling
keyboardListener?.invoke("a", 0)  // Text input
keyboardListener?.invoke(null, SCREENSHOT_KEY_CODE)  // Screenshot
```

#### 2. **ScreenshotService** - Screen Capture
Uses `MediaProjectionManager` to:
- Capture current screen
- Convert to bitmap
- Encode as base64 (optimized for API)

```kotlin
// Launch from IME
intent = Intent(context, ScreenshotService::class.java)
startService(intent)
```

#### 3. **ChatOverlayService** - Floating UI
Windows overlay with:
- Compose UI for chat
- Auto-analyzing screenshot
- Display AI responses

#### 4. **OpenRouterClient** - Vision API
Ktor HTTP client for:
- Image-to-text analysis (GPT-4V/Claude 3.5)
- Chat continuations
- Error handling

```kotlin
openRouterClient.analyzeScreenshot(base64Image, "What's this?")
```

## 🔧 Configuration

### Change AI Model
Edit `OpenRouterClient.kt`:

```kotlin
private val model = "openai/gpt-4-vision"  // Current
// Or try:
// private val model = "anthropic/claude-3.5-sonnet"
// private val model = "google/gemini-2.0-flash-exp"
```

### Adjust Screenshot Compression
Edit `ScreenshotService.kt`:

```kotlin
bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream)  // 85% quality
// Lower = smaller file size but lower quality
```

## 🐛 Common Issues

### "API key not configured"
1. Open Cluely Keyboard settings
2. Enter your OpenRouter API key
3. Make sure it's actually saved (you should see it in the field)

### Screenshot overlay not appearing
1. Go to Settings → Apps → Special app access → Display over other apps
2. Enable permission for Cluely Keyboard
3. Try again

### Keyboard not showing
1. Go to Settings → System → Languages & input → Virtual keyboard
2. Ensure Cluely Keyboard is set as default
3. Try opening a text field and long-pressing for input method selector

### Crashes with "InputMethodService not bound"
1. Make sure keyboard is enabled in system settings
2. Restart the device
3. Check logcat for detailed error: `adb logcat | grep CluelyKeyboard`

## 🔍 Testing

### Manual Testing Checklist
- [ ] Keyboard appears when text field is tapped
- [ ] Characters type correctly
- [ ] Delete key works
- [ ] Screenshot button triggers capture
- [ ] Overlay appears with analysis
- [ ] Can ask follow-up questions
- [ ] Settings persist after app restart

### Using Logcat
```bash
# Watch for errors
adb logcat | grep -E "(CluelyKeyboard|Error|Exception)"

# Specific component
adb logcat | grep OpenRouterClient
adb logcat | grep ScreenshotService
adb logcat | grep ChatOverlayService
```

## 📦 Building Release APK

```bash
./gradlew assembleRelease
# APK at: app/build/outputs/apk/release/app-release.apk
```

Before releasing:
1. Update version in `app/build.gradle.kts`
2. Test thoroughly on multiple devices
3. Sign APK with your key

## 📚 Resources

- [Android IME Documentation](https://developer.android.com/guide/topics/text/creating-input-method)
- [MediaProjection API](https://developer.android.com/reference/android/media/projection/MediaProjectionManager)
- [OpenRouter API Docs](https://openrouter.ai/docs)
- [Jetpack Compose](https://developer.android.com/compose)

## 🤝 Contributing

Want to improve Cluely Keyboard?

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make changes
4. Test thoroughly
5. Push and create a Pull Request

### Good First Issues
- Add more keyboard layouts (Dvorak, Colemak, etc.)
- Improve screenshot compression algorithm
- Add chat message history
- Implement voice input for questions
- Create custom themes

## 📄 License

MIT License - See [LICENSE](LICENSE) for details.

---

**Questions?** Check existing GitHub issues or create a new one!