# Outcasters

Outcasters is a **minimal, local-first, academic AI app** built for Android, migrating from the previous React + Vite + PWA architecture.

## Architecture

This application adopts **Clean Architecture** combined with **MVVM** and **Jetpack Compose**.
- **UI / Presentation**: Built exclusively with Jetpack Compose following Material 3 guidelines and a "liquid glass" premium aesthetic.
- **Domain**: Interfaces and Use Cases for AI processing, model management, and OCR.
- **Data / Infrastructure**: 
  - `Room` database for persistence (chat history, downloaded model metadata).
  - `ML Kit Text Recognition` for on-device OCR pipeline.
  - `HuggingFaceDownloader` for fetching GGUF models locally.
  - `LlamaInferenceEngine` intended for running local inference using `llama.cpp` JNI bindings.

## Migration Guide (from React PWA)

1. **State Management**:
   - Redux/Zustand has been replaced by `ViewModel` + `StateFlow`.
   - UI observation is done using `collectAsStateWithLifecycle()`.
2. **AI Inference Engine**:
   - `WebLLM` / `Transformers.js` is fully replaced by native `llama.cpp` for Android. Models must be in **GGUF** format.
   - We support streaming natively with Kotlin `Flow` which seamlessly connects with Jetpack Compose.
3. **Database**:
   - IndexedDB is replaced by `Room` (SQLite) ensuring high performance and type safety.
4. **OCR**:
   - Web-based Tesseract/Browser OCR is replaced with native `Google ML Kit`, vastly improving speed, battery use, and accuracy for offline processing.
5. **Storage**:
   - Models are stored in App-specific storage (`Android/data/.../models`). Downloads can be imported via Scoped Storage.

## Building and Running

Ensure you have Android Studio. Sync the gradle files and run on an emulator (API 24+) or physical device. 

For full `llama.cpp` integration, ensure the NDK is installed and compile the `ggml` shared libraries as per the `llama.cpp` Android docs.

## Features Currently Implemented
- Premium UI with Concept/Language/Interview modes.
- OCR camera placeholder and integration scaffold.
- Model Manager UI showing downloads and loaded models.
- Settings placeholder UI.
- Chat UI supporting streaming inputs.
