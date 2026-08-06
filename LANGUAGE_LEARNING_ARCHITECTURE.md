# Next-Gen Adaptive Language Learning Backend Architecture

## 1. Core System Requirements & Hybrid Inference Strategy
The backend is designed as a hybrid edge-cloud architecture to deliver zero-latency experiences for voice/text while leveraging massive cloud models for curriculum planning and complex reasoning.
- **Dynamic Curriculum Generation**: Uses cloud-hosted Reinforcement Learning (RL) and LLMs to dynamically adjust difficulty, vocabulary, and grammar based on user mastery.
- **Conversational AI Roleplay**: Uses on-device SLMs (Small Language Models) for instant text/voice roleplay, seamlessly handing off to the cloud when complex semantic understanding is required.
- **Advanced Spaced Repetition (SRS)**: Employs a vector database to track semantic clusters of vocabulary, moving beyond simple flashcards to contextual memory tracking.
- **Real-time Cultural Context (RAG)**: Cloud-based RAG pipeline fetches live news, idioms, and cultural events relevant to the target language and injects them into the curriculum LLM.

## 2. Architectural Modules
### User State & Mastery Tracker (Vector + Relational)
- Maps the user's proficiency across linguistic concepts.
- Uses **Vector DB (e.g., Pinecone/Milvus)** to store embeddings of learned vocabulary and grammar rules. This allows the curriculum generator to query for "concepts semantically close to what the user already knows" or "concepts the user is forgetting."
- Uses **Relational DB (e.g., PostgreSQL)** for deterministic data: XP, streaks, unlocked levels, and basic user profiles.

### Content Generation Engine (LLM Pipeline)
- Orchestrates curriculum generation.
- **Inputs**: User Vector State, Current Context, RAG Cultural Data.
- **Outputs**: Personalized exercises, reading materials, and dialogue prompts.

### Voice Processing Pipeline (STT/TTS)
- **Local/Edge STT**: Whisper.cpp or on-device speech recognition for low-latency transcription.
- **Cloud STT**: Cloud fallback for challenging accents or languages requiring massive models.
- **Edge TTS**: Fast on-device TTS for immediate feedback.
- **Cloud TTS**: Ultra-realistic TTS (e.g., ElevenLabs or VITS) pre-fetched during lesson generation.

### RAG Engine for Cultural Insights
- Crawls and indexes target-language news, blogs, and cultural wikis.
- Chunks and stores them in a Vector DB.
- At lesson generation time, retrieves relevant cultural context based on the current vocabulary theme (e.g., retrieving an article about "Dia de los Muertos" when learning Mexican Spanish vocabulary for "family" and "traditions").

### Progress & Gamification API
- A high-throughput, low-latency microservice (e.g., Go or Rust) managing XP, leagues, and streaks using Redis for leaderboards and PostgreSQL for persistent state.

## 3. Multilingual Architecture Strategy
To support 40+ languages without fragmented logic, the system uses a **Universal Semantic Abstraction**:
- **Language-Agnostic Core**: The RL engine and Mastery Tracker operate on semantic embeddings and abstract grammar tags (e.g., `PAST_TENSE`, `GENDER_AGREEMENT`), not raw strings.
- **Language Packs (Adapters)**: Specific rules for character-based (Chinese/Japanese) or complex script (Hindi/Arabic) languages are handled by language-specific LLM system prompts and tokenizer adapters.
- **Unicode & Rendering**: The frontend handles script rendering (e.g., Ruby characters for Kanji, right-to-left for Arabic). The backend strictly communicates in UTF-8 JSON.

## 4. Deliverables

### A. High-Level Data Flow: "Dynamic Lesson" Sequence
1. **App Launch**: Client requests a new lesson via `GET /api/v1/lesson/next`.
2. **State Retrieval**: Backend fetches user mastery vectors from Vector DB and recent performance from PostgreSQL.
3. **Context Injection**: RAG Engine retrieves relevant cultural insights based on the target vocabulary.
4. **LLM Generation**: The Content Engine prompts a cloud LLM (e.g., GPT-4 or Claude 3) with the user state + RAG context to generate a personalized JSON payload of exercises and roleplays.
5. **Pre-fetching**: Cloud TTS generates audio for the exercises in the background and caches it.
6. **Execution**: Client receives the payload and begins the lesson.
7. **Roleplay**: User interacts via voice. Local STT transcribes. On-device SLM generates immediate responses for low latency, syncing state back to the cloud asynchronously.
8. **Evaluation**: User responses are evaluated. The backend updates the Vector DB mastery state using an RL reward function.

### B. Database Schema Recommendations
- **Relational (PostgreSQL)**:
  - `Users` (id, target_language, native_language, streak, xp)
  - `SessionLogs` (id, user_id, timestamp, duration, score)
- **Vector DB (Pinecone/Milvus/Qdrant)**:
  - `MasteryVectors`: Vector representation of a user's understanding of specific concepts.
    - Metadata: `user_id`, `concept_id`, `language`, `strength_score`, `last_reviewed_at`.
- **In-Memory (Redis)**:
  - Leaderboards, active streaks, rate limiting, and real-time session caching.

### C. API Endpoint Design
```yaml
POST /api/v1/lessons/generate
  Description: Generates a personalized dynamic lesson.
  Request: { "user_id": "123", "target_language": "es-MX", "session_time_limit_mins": 10 }
  Response: { "lesson_id": "abc", "exercises": [...], "cultural_context": "..." }

POST /api/v1/roleplay/message
  Description: Evaluates a user's roleplay message and generates a response.
  Request: { "lesson_id": "abc", "user_text": "Hola, me gustaria un cafe.", "audio_blob": <optional> }
  Response: { "ai_response_text": "¡Claro! ¿Lo quieres con leche?", "ai_audio_url": "...", "grammar_feedback": "..." }

POST /api/v1/mastery/update
  Description: Updates the user's vector mastery state after an exercise.
  Request: { "user_id": "123", "concept_id": "es_past_preterite", "performance_score": 0.85 }
  Response: { "status": "success", "new_mastery_level": "intermediate" }
```

### D. Latency Minimization Strategies
1. **Edge/Cloud Handoff**: Run a 1B/3B parameter SLM (e.g., Llama 3 1B) directly on the Android device for instant conversational replies. Only call the cloud for lesson generation or complex grammar correction.
2. **Streaming LLM Outputs**: For cloud generation, use Server-Sent Events (SSE) to stream exercises to the client as they are generated, rather than waiting for the entire lesson payload.
3. **Optimistic TTS Prefetching**: Generate and cache TTS audio for likely AI responses during roleplay *before* the user even finishes speaking.
4. **WebSocket Audio Streaming**: Stream user audio to the cloud STT via WebSockets (if edge STT is insufficient) so transcription happens concurrently with speaking.
