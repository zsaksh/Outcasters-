sed -i 's/retrievalContext: String/retrievalContext: String,\n        isNewSession: Boolean = true/g' app/src/main/java/com/example/backend/inference/PromptRouter.kt
sed -i 's/targetLanguage = targetLanguage/targetLanguage = targetLanguage,\n            isNewSession = isNewSession/g' app/src/main/java/com/example/backend/inference/PromptRouter.kt
