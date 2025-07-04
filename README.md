# PregMap - Mothers App

A comprehensive Android application designed to support mothers during pregnancy with various authentication methods and user-friendly features.

## Features

- Email Authentication
- Phone Authentication
- Google Sign-In
- Modern Material Design UI
- Firebase Integration

# PregMap – TwinAI Setup

## OpenAI API Key Setup for TwinAI

To use TwinAI (the AI chat assistant), you need an OpenAI API key. **Never hardcode your API key in source files.**

### 1. Get your OpenAI API Key
- Sign up at https://platform.openai.com/ and create an API key (starts with `sk-...`).

### 2. Add your API key to `local.properties`
- In your project root, open (or create) a file named `local.properties`.
- Add this line:

```
OPENAI_API_KEY=sk-...your-key-here...
```

### 3. Access the API key in your code
- Use Gradle to read the key from `local.properties` and expose it as a BuildConfig field, or load it securely in your networking layer.
- **Do not commit your API key or `local.properties` to version control.**

### 4. Where to use it
- The TwinAI screen is implemented in `PregMap/app/src/main/java/com/elvis/pregmap/ui/screens/TwinAIScreen.kt`.
- Replace the stub in `getTwinAIResponse()` with your OpenAI API call, using the API key from your secure source.

---

**Example (Kotlin, using BuildConfig):**
```kotlin
val apiKey = BuildConfig.OPENAI_API_KEY
```

**Or (manual load):**
```kotlin
val apiKey = context.getString(R.string.openai_api_key)
```

---

For more details, see the comments in `TwinAIScreen.kt`.
