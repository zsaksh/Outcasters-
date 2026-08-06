package com.example.backend.vision

class OcrRouter {
    fun routeImage(imagePath: String): OcrRouteDecision {
        // detect text blocks, check if math, plain text, etc.
        return OcrRouteDecision.OcrOnly
    }
}

sealed class OcrRouteDecision {
    object OcrOnly : OcrRouteDecision()
    object OcrPlusLocalModel : OcrRouteDecision()
    object VisionModel : OcrRouteDecision()
}
