package com.example.backend.inference

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.backend.models.ModelManifest

@RunWith(RobolectricTestRunner::class)
class QualityTest {
    @Test
    fun testOutputValidation_Calculus() {
        val postProcessor = PostProcessor()
        
        assertTrue(postProcessor.validateQuality("What is calculus?", "Calculus is the mathematical study of continuous change.", "math"))
        assertFalse(postProcessor.validateQuality("What is calculus?", "Photosynthesis is how plants make food.", "math"))
        assertFalse(postProcessor.validateQuality("What is calculus?", "Cooking involves heat.", "math"))
    }
    
    @Test
    fun testOutputValidation_Photosynthesis() {
        val postProcessor = PostProcessor()
        
        assertTrue(postProcessor.validateQuality("Explain photosynthesis", "It is the process used by plants.", "concept"))
        assertFalse(postProcessor.validateQuality("Explain photosynthesis", "Calculus is math.", "concept"))
    }
    
    @Test
    fun testOutputValidation_Language() {
        val postProcessor = PostProcessor()
        
        assertFalse(postProcessor.validateQuality("Translate Hello", "The integral of x^2 is x^3/3", "translate"))
        assertTrue(postProcessor.validateQuality("Translate Hello to French", "Bonjour", "translate"))
    }
    
    @Test
    fun testPromptRouting() {
        val router = PromptRouter()
        val manifest = ModelManifest(modelId = "test", displayName = "Test", sourceUrl = "", fileName = "", chatTemplate = "chatml")
        
        val routed1 = router.route("chat", "English", manifest, emptyList(), "What is calculus?", "", "")
        assertEquals("math", routed1.effectiveMode)
        
        val routed2 = router.route("chat", "English", manifest, emptyList(), "Explain photosynthesis", "", "")
        assertEquals("concept", routed2.effectiveMode)
        
        val routed3 = router.route("chat", "English", manifest, emptyList(), "Translate hello", "", "")
        assertEquals("translate", routed3.effectiveMode)
    }
}
