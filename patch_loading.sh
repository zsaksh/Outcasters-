sed -i 's/System.gc()/System.gc()\n        _modelState.value = com.example.backend.models.ModelState.Loading/g' app/src/main/java/com/example/inference/LlamaInferenceEngine.kt
