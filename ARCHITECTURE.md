# Outcasters Backend Architecture (Anti-Establishment, Local-First AI)

## 1. Core Ethos & Vision
Outcasters is a raw, powerful, offline-first alternative to gamified, cloud-dependent mainstream apps. It is a strictly local AI academic assistant for Android, built with an edgy, rebellious, and anti-establishment ethos. We believe computation belongs to the user, not to centralized cloud rent-seekers. The backend is engineered to bypass mandatory cloud dependency entirely, enabling uncensored, zero-latency, private doubt-solving on the edge.

## 2. Architectural Principles (Cloud-Grade Edge Resilience)
Drawing from Meta's Llama scaling and Perplexity's latency-optimized RAG pipelines, this architecture adapts distributed cloud patterns for constrained edge environments.

- **Zero-Copy & Mmap**: We exclusively utilize memory-mapped weights (`mmap()`). The OS page cache manages eviction, eliminating redundant RAM allocations and preventing JVM OOMs during model loading.
- **One Active Model (Aggressive Lifecycle)**: Strict memory profiling ensures only a single model context is allocated. Native pointers are aggressively freed when the app backgrounds.
- **MVI/MVVM Reactive Flow**: The inference state is strictly decoupled from the UI. The JNI bridge pushes token chunks to Kotlin `StateFlow`, adhering to Unidirectional Data Flow.
- **Crash-Resistant C++ Boundaries**: Native code panics (e.g., llama.cpp segmentation faults or allocation failures) are caught using structured exception handling or sandbox processes to ensure the JVM remains stable.
- **Streaming-First (Zero-Latency)**: Token generation is pipelined. UTF-8 byte buffers are streamed from JNI to Kotlin dynamically to prevent UI freezes.
- **Future-Compatible (Adapter Pattern)**: The backend is an abstraction layer. While `llama.cpp` handles GGUF today, `ExecuTorch`, `ONNX Runtime`, or future quantizations can be seamlessly hot-swapped without UI rewrites.

## 3. High-Level Modules
1. **Model Registry & Manifest System**: Capability-based matching. Does not assume one model family. Tracks quantizations, VRAM/RAM constraints, and formats (GGUF, safetensors).
2. **Native Inference Bridge (JNI)**: The isolation boundary between Kotlin and C++ execution.
3. **MVI Inference ViewModel**: Manages the `InferenceState` state machine (`Idle`, `Loading`, `Generating`, `Error`).
4. **Hugging Face Downloader & Verifier**: Resilient, chunked HTTP range downloader with SHA256 integrity verification.
5. **Prompt Builder (Dynamic Templating)**: Injects RAG chunks and OCR text into ChatML, Llama 3, or Phi templates dynamically.
6. **On-Device RAG & OCR**: ML Kit for OCR question solving and a lightweight local vector index (BM25 or similar) for Document Q&A.

## 4. Multi-Modal & Academic Flows
- **OCR Question Solving**: Users scan a textbook; ML Kit extracts text; the Prompt Builder formats it into a structured doubt-solving query.
- **Document Q&A**: PDF texts are chunked and embedded/indexed locally. Retrieval injects the top-K chunks into the prompt context window.
- **Global Reach**: Language-agnostic prompts allow the LLM to teach concepts or prep for interviews in Hindi, Spanish, French, etc.

## 5. Graceful Memory Scaling
The backend scales dynamically across device tiers:
- **Ultra-Light (<4GB RAM)**: Defaults to 300M-1B parameters (e.g., SmolLM, Qwen 0.5B).
- **Balanced (4GB-8GB RAM)**: Recommends 1.5B-3B parameters (e.g., Llama 3.2 1B, Phi-3.5 Mini).
- **Premium (8GB+ RAM)**: Unlocks 7B-8B parameter models.
If a user selects an oversized model, the backend intercepts the load, warns the user, and offers a fallback, preventing hard crashes.

## 7. Capability-Based Model Manifest System
The local database acts as the single source of truth for the local model library. It stores:
- **Format & Context specs:** Quantization type, context lengths, file sizes.
- **Model Capabilities:** Reasoning ability, coding, language support, OCR alignment.
- **State Machine Integration:** `installStatus` tracking downloading, corruption, pausing.
- **Active Tracker:** Atomic tracking of the single active model to persist context after restart.

## 8. Hugging Face Stream-Downloader
A custom `OkHttp`-based download engine handles raw Hugging Face endpoint traversal.
- Resolves the repository file tree (`https://huggingface.co/api/models/<repo>/tree/main`).
- Parses quantizations dynamically using RegEx.
- Implements **HTTP 206 Range requests** to support pausing, resuming, and robustly recovering interrupted multi-gigabyte downloads.
- Streams bytes chunk-by-chunk to the file system, skipping RAM buffering entirely to prevent OOM errors.

## 6. Native Inference Engine & Performance Architecture
Designed for sustained low-latency generation on constrained edge hardware.
- **Runtime Modularity:** Modular architecture supporting llama.cpp, MLC, ExecuTorch, ONNX Runtime GenAI through adapters.
- **Zero UI Blocking:** Dedicated inference workers handle mmap, graph allocation, and KV cache.
- **Memory Mapping (mmap):** Zero-copy loading speeds up startup, lowers RAM, and battery usage.
- **Continuous Token Streaming:** Tokens stream directly via JNI callbacks into Kotlin Flow to Compose StateFlow.
- **Prefix Caching & Sliding KV:** Reuse system prompts, and seamlessly handle long conversations via rolling context and semantic eviction.
- **Speculative Decoding & Flash Attention:** Enabled dynamically based on runtime and model support.
- **Safe Cancellation:** Immediate interrupt of generation with zero memory leaks.

## 7. Intelligent Device Profiling
- Hardware profiling executed dynamically (CPU, GPU, NPU, RAM, ABI, Thermal).
- Device Classes: Lite (2-4GB), Standard (4-6GB), Advanced (6-8GB), Flagship (8-12GB), Ultra (12GB+).
- **Automatic Model Recommendation:** Auto-suggests appropriate parameter sizes (e.g. SmolLM2 360M for Lite, Llama-3 8B for Ultra).
- **Thermal Protection:** Dynamically scaling batch size and thread count on throttling detection.
- **Predictive OOM Prevention:** Estimates memory requirements before load attempts.

## 8. Fully Offline Local RAG
- **Zero Cloud Reliance:** Local ingestion of PDF, HTML, Markdown, Scanned Notes.
- **Semantic Chunking:** Preserves headings, lists, equations, rather than arbitrary token cuts.
- **Local Embeddings & Vector DB:** Lightweight BAAI/bge-small executing locally, with SQLite storing the vectors.
- **Hybrid Search:** Combines Vector Similarity with BM25 Keyword Search + Reranking for academic accuracy.

## 9. Offline Vision Pipeline
- **Auto Image Processing:** Deskew, denoise, perspective correction before OCR.
- **Intelligent Routing:** Directs image contents (Text, Formula, Diagram, Table) to the optimal sub-pipeline (OCR, Math Parser, Vision Model).
- **Progressive Analysis:** Emits OCR preview immediately before deep reasoning begins.
- **Educational Output:** Enforces structured responses (Step-by-step, Key Concepts, Practice Questions).

## 10. Adaptive Model Orchestrator
- Intelligent routing of tasks to specialized models:
  - Embeddings → BAAI/bge-small
  - Grammar/Translation → Compact 1B Model
  - Deep Reasoning → 7B-8B Model
  - Vision → Multimodal LLaVA-style Model
- Maximizes responsiveness and minimizes battery drain by picking the right local tool for the specific intent.

## 11. Error Handling & Graceful Degradation
Native C++ errors from llama.cpp or other adapters are caught at the JNI boundary and mapped to Kotlin sealed classes (`InferenceError`).
- **Corrupted Files:** `CorruptedModelFile` triggers a prompt to re-download.
- **Incompatible Hardware:** `HardwareIncompatibility` gracefully warns the user instead of crashing.
- **OOM Prevention:** `OutOfMemory` triggers automatic fallback to a smaller batch size or smaller model.
- **Timeouts:** `GenerationTimeout` aborts generation to prevent battery drain.
- **UI Responsiveness:** All heavy work (mmap, prefill) runs on Dispatchers.IO/Default to prevent ANRs.

## 12. Deliverables & Data Flows

### Module Boundaries & Folder Structure
```
app/src/main/java/com/example/
├── backend/
│   ├── device/      # IntelligentDeviceProfiler, SystemSpecs
│   ├── download/    # ModelDownloaderImpl, HuggingFaceClientImpl
│   ├── inference/   # InferenceEngine, PromptBuilder, ContextManager, IRuntimeAdapter, InferenceError
│   ├── manager/     # ConversationManager, ThermalMemoryManager
│   ├── models/      # ModelManifest, Enums, ModelStore
│   ├── ocr/         # VisionPipeline
│   ├── orchestrator/# AdaptiveModelOrchestrator
│   └── rag/         # LocalRagEngine, Chunking
├── data/            # Room Database Entities and DAOs
└── ui/              # Jetpack Compose UI
```

### Data Flow: Model Load
1. User selects model -> UI calls `IModelRegistry.setActiveModel`.
2. `IInferenceEngine` reads `ModelManifest`.
3. `IInferenceEngine` delegates to `IRuntimeAdapter` (e.g. `LlamaCppAdapter`).
4. Adapter invokes JNI `load_model` in a background worker thread.
5. C++ maps the file via `mmap()` (Zero-Copy).
6. StateFlow updates UI from `Loading` -> `Ready`.

### Data Flow: Inference Stream
1. User submits prompt -> `PromptBuilder` formats into target template (e.g., Llama 3).
2. `ContextManager` trims history to fit Context Window.
3. `IInferenceEngine.generate()` invoked.
4. JNI loop generates tokens.
5. C++ callback passes UTF-8 bytes to Kotlin callback.
6. Kotlin Flow emits strings to Compose `StateFlow`.
7. UI updates dynamically (Typewriter effect).

### C++ to Kotlin JNI Bridging Strategy
- The JNI layer creates an asynchronous event loop that waits on the generation queue.
- Callbacks from C++ use `JNIEnv::CallVoidMethod` attached to the current thread.
- Kotlin wraps the JNI callback with `callbackFlow` to convert push-based C++ events into backpressure-aware Kotlin Coroutine Flows.

### Memory & Performance Protocols
- **Model Storage:** Direct internal app-private storage, no shared media directories.
- **Context Size:** Adaptive window trimming based on active RAM.
- **Unloading:** `IInferenceEngine.unload()` explicitly calls native pointer cleanup and `munmap()` upon app backgrounding or memory pressure broadcasts.

## 11. Advanced Edge ML Infrastructure (JNI & NDK)
- **Memory-Mapping (mmap):** Model weights are strictly loaded via mmap in C++ to keep them outside the JVM heap.
- **Zero-Copy Buffers:** Native code uses direct ByteBuffers for inference calculations to prevent GC freezes in the UI thread.
- **JNI Bridging:** The Kotlin layer exposes asynchronous APIs. C++ executes the generation loop and pushes tokens via JNI callbacks, which are intercepted by Kotlin `callbackFlow` to pipe directly into Jetpack Compose.
- **Process Isolation:** The inference engine runs in a separate Android process (`android:process=":inference"`). If an OOM or segfault occurs natively, it crashes the background process rather than the main app, allowing a clean UI recovery.

## 12. Advanced Inference Pipeline
- **KV-Cache Offloading:** Background tasks monitor memory usage. If context limits are approached, a summarized version of the history replaces the raw older messages.
- **Grammar-Constrained Decoding:** Ensures structured output formatting (e.g. valid JSON) is generated without fail, removing parsing exceptions for components like Flashcard or Quiz generation.
- **Speculative Decoding:** On high RAM devices (Flagship/Ultra), a tiny N-gram draft model runs alongside the target to predict tokens rapidly, accelerating overall output speed.

## 13. Brand Identity & System Prompt
- **Persona:** Outcasters acts as the user's private academic weapon against a broken education system. The tone is sharp, rebellious, and direct.
- **Enforcement:** `SystemPromptProvider` prepends strict behavioral rules to every interaction (no lecturing, no corporate pleasantries), guaranteeing a distinct voice.

## 14. Local Telemetry & Evaluation
- **LLMOps (Silent Logging):** `LLMOpsTracker` logs inference failures and OOM crashes to a local Room table. The `IntelligentDeviceProfiler` reads these logs on startup to dynamically adjust the recommended model size downward if the device repeatedly fails.
- **Negative Signals:** Regenerating responses or stopping generation early records a negative signal locally, enabling offline programmatic tuning of temperature and repetition penalties over time.
- **Quantized Local DB:** RAG vectors use quantized embeddings (int8/binary) to keep the SQLite payload extremely lightweight on-device.

## 15. Dynamic LoRA Swapping (Multi-Agent Architecture)
- **Single Base Model:** Downloads one foundational model (e.g., Llama 3 8B).
- **Micro-Adapters:** Uses hot-swappable LoRA adapters (10-50MB) for specific domains like `math_reasoning`, `french_tutor`, or `interview_prep`.
- **Runtime Switching:** Dynamically injects LoRA weights into the base model's computation graph without unloading the base model from RAM, enabling instant multi-agent behavior.

## 16. Spaced Repetition & Behavioral Sync (The Duolingo Engine)
- **Half-Life Regression (HLR):** Replaces simple flashcards with a local algorithm calculating precise memory decay rates based on error history and reaction times.
- **Dynamic Curriculum:** The Learn tab generates sessions prioritizing concepts closest to being forgotten.
- **Offline Event Queuing:** Stores learning events in an encrypted SQLite queue, syncing opportunistically via WorkManager on Wi-Fi (if opted-in).

## 17. Differential Patching & OTA Updates
- **Delta Updates:** Prevents re-downloading gigabytes for minor model updates by downloading and applying binary diffs locally.
- **OTA Configurations:** System Prompts, Chat Templates, and RAG indexes are fetched as lightweight JSON payloads, allowing behavior tuning without App Store updates.

## 18. Local Constitutional AI & Guardrails (The Anthropic Method)
- **Adversarial Defense:** Injects systemic preambles guarding against prompt injection.
- **Format Enforcers:** Native Logit Processors constrain vocabulary to mathematically guarantee strict JSON or Markdown outputs for UI consistency.
- **Graceful Refusals:** Instantly falls back to hardcoded polite refusals for dangerous or off-topic prompts to conserve battery.

## 19. Thermal & Battery Guardians
- **Thermal API Integration:** Subscribes to `PowerManager.OnThermalStatusChangedListener`.
- **Adaptive Throttling:** Reduces thread count on `MODERATE` thermal status and pauses background RAG indexing on `SEVERE`.
- **Battery Awareness:** Automatically disables heavy features like speculative decoding below 15% battery to ensure core device survival.

## 20. Hybrid & Cloud-Enhanced Execution Architecture (Online Mode)
- **Edge-Cloud Collaborative Speculative Decoding:** Uses the local SLM as a high-speed draft model and a cloud target model for parallel verification, accelerating generation while preserving mathematical correctness via Modified Rejection Sampling.
- **Adaptive Semantic Routing:** A local classifier evaluates query complexity to route simple tasks to the on-device engine, medium tasks to collaborative decoding, and deep reasoning tasks to cloud frontier models. Includes a silent circuit breaker to fall back to the local engine on network drops.
- **Privacy-Preserving Cloud RAG:** Redacts PII locally before dispatching queries. Personal documents are kept strictly local, and only anonymized sub-queries reach external web search APIs.
- **Real-Time Web Search & Citation Fusion:** Merges local vector database chunks with real-time web snippets, rendering them as interactive citation chips in the UI.
- **Federated Learning:** Stores implicit user reward signals locally and computes small LoRA gradient updates during idle charging on Wi-Fi, employing Secure Aggregation to tune global models without exposing raw data.
