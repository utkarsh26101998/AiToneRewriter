# ✍️ AI Tone Rewriter

A floating Android assistant that rewrites your messages with the perfect tone — right from any messaging app.

![Android](https://img.shields.io/badge/Android-8.0%2B-green) ![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue) ![License](https://img.shields.io/badge/License-MIT-yellow)

## Features

- **Floating Chat Head** — Draggable bubble that works over any app (WhatsApp, Telegram, Instagram, SMS)
- **8 Tone Modes** — Professional, Friendly, Funny, Romantic, Flirty, Polite, Short & Crisp, Assertive
- **One-Tap Rewrite** — Select a tone and your message is instantly rewritten
- **Auto-Replace** — Rewritten text automatically replaces the original in the input field
- **Privacy First** — No messages stored, no logging, text processed only on-demand
- **Minimal UI** — Fast, lightweight, stays out of your way

## Architecture

```
MVVM Architecture
├── Model      → Mood.kt (tone definitions + system prompts)
├── View       → MainActivity, Floating Bubble, Mood Popup
├── Service    → FloatingWidgetService (overlay), TextAccessibilityService
└── AI Engine  → AIRewriteEngine (OpenAI API integration)
```

## Project Structure

```
app/src/main/
├── java/com/utkarsh/aitonerewriter/
│   ├── MainActivity.kt                 # Permission setup & service launcher
│   ├── service/
│   │   ├── FloatingWidgetService.kt    # Floating bubble + mood popup overlay
│   │   └── TextAccessibilityService.kt # Reads/writes text in input fields
│   ├── ai/
│   │   └── AIRewriteEngine.kt          # OpenAI API integration
│   └── model/
│       └── Mood.kt                     # 8 tone definitions with prompts
├── res/
│   ├── layout/
│   │   ├── activity_main.xml           # Main permission setup screen
│   │   ├── layout_floating_bubble.xml  # Chat head bubble
│   │   ├── layout_mood_popup.xml       # Tone selection popup
│   │   └── item_mood_button.xml        # Individual mood button
│   ├── drawable/                       # Shapes, selectors, icons
│   ├── values/                         # Colors, strings, themes
│   └── xml/
│       └── accessibility_config.xml    # Accessibility service config
└── AndroidManifest.xml                 # Permissions & service declarations
```

## Setup Instructions

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **Android SDK** API 34 (Android 14)
- **JDK 17**
- **OpenAI API Key** ([Get one here](https://platform.openai.com/api-keys))

### Step 1: Clone the Repository

```bash
git clone https://github.com/utkarsh26101998/AiToneRewriter.git
cd AiToneRewriter
```

### Step 2: Configure API Key

Add your OpenAI API key to `local.properties`:

```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
OPENAI_API_KEY=sk-your-api-key-here
```

### Step 3: Open in Android Studio

1. Open Android Studio
2. File → Open → Select the `AiToneRewriter` folder
3. Wait for Gradle sync to complete

### Step 4: Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# APK location
# app/build/outputs/apk/debug/app-debug.apk
```

Or press **Run ▶️** in Android Studio with a connected device/emulator.

### Step 5: App Setup (On Device)

1. **Open the app** — You'll see the permission setup screen
2. **Grant Overlay Permission** — Tap the button, enable "Allow display over other apps"
3. **Enable Accessibility Service** — Tap the button, find "AI Tone Rewriter" in the list, enable it
4. **Launch Widget** — Tap "Launch Floating Widget"
5. **Open any messaging app** — Start typing, tap the floating bubble, select a tone!

## How It Works

```
1. User types a message in WhatsApp/Telegram/etc.
2. User taps the floating ✍️ bubble
3. Mood selection popup appears with 8 tones
4. User taps a tone (e.g., "Professional")
5. AccessibilityService captures the typed text
6. Text is sent to OpenAI API with the tone's system prompt
7. Rewritten text replaces the original in the input field
```

## Available Tones

| Tone | Emoji | Description |
|------|-------|-------------|
| Professional | 💼 | Formal, business-appropriate |
| Friendly | 😊 | Warm, approachable |
| Funny | 😂 | Witty, humorous |
| Romantic | ❤️ | Sweet, heartfelt |
| Flirty | 😏 | Playful, charming |
| Polite | 🙏 | Courteous, respectful |
| Short & Crisp | ⚡ | Concise, direct |
| Assertive | 💪 | Confident, commanding |

## Permissions

| Permission | Why |
|-----------|-----|
| `SYSTEM_ALERT_WINDOW` | Display floating bubble over other apps |
| `INTERNET` | Send text to OpenAI API for rewriting |
| `FOREGROUND_SERVICE` | Keep the floating widget alive |
| `BIND_ACCESSIBILITY_SERVICE` | Read/write text in input fields |

## Privacy

- **No messages are stored** — ever
- **No logging** of user text
- Text is only captured when you explicitly tap a tone
- API calls use HTTPS encryption
- No analytics or tracking

## Customization

### Use a Different AI Provider

Edit `AIRewriteEngine.kt` to change the API endpoint:

```kotlin
// For Claude API
private val baseUrl = "https://api.anthropic.com/v1/messages"

// For Gemini API
private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
```

### Add Custom Tones

Add new entries to the `Mood` enum in `Mood.kt`:

```kotlin
SARCASTIC(
    displayName = "Sarcastic",
    emoji = "🙄",
    systemPrompt = "Rewrite in a sarcastic tone..."
)
```

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **AI**: OpenAI GPT-4o-mini
- **HTTP**: OkHttp 4
- **UI**: Android Views + WindowManager Overlay

## License

MIT License — feel free to use, modify, and distribute.
