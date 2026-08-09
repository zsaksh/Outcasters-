<div align="center">
  <h1>Outcasters</h1>
  <p><b>Local-first, serverless, on-device AI academic companion for Android</b></p>
</div>

## 📌 Overview
Outcasters is a powerful, privacy-first academic companion that runs completely offline on your Android device. Designed to help students, researchers, and learners, the app brings the capabilities of modern Large Language Models (LLMs) directly to your pocket without requiring an internet connection or costly subscriptions. 

By utilizing MediaPipe for native on-device inference and ML Kit for blazing-fast OCR, Outcasters empowers you to learn, summarize, and understand complex concepts with zero latency and full privacy.

## ✨ Key Features
- **100% Offline AI Inference**: Chat with powerful open-source models (like Gemma) running natively on your device via MediaPipe GenAI Tasks.
- **On-Device OCR**: Instantly extract text from physical books, documents, and notes using ML Kit, completely offline.
- **Model Hub**: Built-in download manager for Hugging Face models (GGUF/TFLite).
- **Multiple AI Modes**: 
  - *Concept Mode*: Break down complex ideas.
  - *Language Mode*: Practice translation and language comprehension.
  - *Interview Mode*: Prepare for tests and interviews with targeted Q&A.
- **Privacy First**: All your data—from chats to scanned documents—is stored locally in a Room database. Your queries never leave your device.
- **Modern Jetpack Compose UI**: A beautiful, fluid Material Design 3 interface with a dark/light theme and animated transitions.

## 🛠 Tech Stack & Architecture
Outcasters follows **Clean Architecture** combined with **MVVM** and modern Android development practices.

- **UI / Presentation**: Jetpack Compose, Material 3, Navigation Compose
- **Domain**: Kotlin Coroutines & Flow for reactive state management and streaming responses.
- **Data / Persistence**:
  - `Room Database` for storing chat history and model metadata.
  - `DataStore` for user preferences.
- **AI & Machine Learning**:
  - `MediaPipe Tasks GenAI` (`LlmInference`) for on-device LLM execution.
  - `Google ML Kit Text Recognition` for fast, offline OCR.
- **Networking** (For Model Downloads only):
  - `OkHttp` & `Ktor`

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 35 (Android 15)
- **Device Requirements**: A physical device with at least 4GB of RAM is recommended for running on-device models like Gemma 2B.

### Installation
1. Clone the repository
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run the app on your device or emulator.

### Downloading Models
To use the chat features, navigate to the **Model Hub** in the app to download an on-device model (e.g., Gemma 2B CPU version). The app handles fetching, verifying, and loading the model into memory.

## 🏗 Architecture Details
- `com.example.ui`: Contains all Jetpack Compose screens (`ChatScreen`, `ModelHubScreen`, `CameraScreen`, etc.) and ViewModels.
- `com.example.backend`: Infrastructure logic including the `ModelManager`, Hugging Face `DownloadManager`, and OCR processing pipelines.
- `com.example.inference`: The core `LlamaInferenceEngine` that acts as an abstraction over MediaPipe's `LlmInference` to manage model state and text generation.
- `com.example.data` & `com.example.domain`: Room DAOs, database setup, and repositories for saving chat histories.

## 📜 License
This project is licensed under the MIT License.
