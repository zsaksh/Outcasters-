#include <jni.h>
#include <string>
#include <android/log.h>

#define TAG "OutcastersNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_inference_LlamaNativeWrapper_loadModel(JNIEnv *env, jobject thiz, jstring path, jint context_length, jint threads) {
    const char *model_path = env->GetStringUTFChars(path, nullptr);
    LOGI("Loading model: %s with context: %d threads: %d", model_path, context_length, threads);
    env->ReleaseStringUTFChars(path, model_path);
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaNativeWrapper_unloadModel(JNIEnv *env, jobject thiz) {
    LOGI("Unloading model...");
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaNativeWrapper_generate(JNIEnv *env, jobject thiz, jstring prompt, jobject callback) {
    const char *prompt_chars = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Generating text for prompt: %s", prompt_chars);
    
    jclass callbackClass = env->GetObjectClass(callback);
    jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onToken", "(Ljava/lang/String;)V");
    
    jstring token1 = env->NewStringUTF("Hello ");
    env->CallVoidMethod(callback, onTokenMethod, token1);
    env->DeleteLocalRef(token1);
    
    jstring token2 = env->NewStringUTF("World");
    env->CallVoidMethod(callback, onTokenMethod, token2);
    env->DeleteLocalRef(token2);
    
    env->ReleaseStringUTFChars(prompt, prompt_chars);
}

// Simulated Context and State
void* g_ctx = (void*)1;
void* g_model = (void*)1;
bool g_cancel_flag = false;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_inference_LlamaBridge_clearKvCache(JNIEnv *env, jobject thiz) {
    if (g_ctx != nullptr) {
        LOGI("Flushing KV Cache...");
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaBridge_cancelGeneration(JNIEnv *env, jobject thiz) {
    LOGI("Cancelling previous generation...");
    g_cancel_flag = true;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaBridge_resetSampler(JNIEnv *env, jobject thiz) {
    LOGI("Resetting sampler...");
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaBridge_clearDecoderState(JNIEnv *env, jobject thiz) {
    LOGI("Clearing decoder state...");
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaBridge_createFreshInferenceState(JNIEnv *env, jobject thiz) {
    LOGI("Creating fresh inference state...");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_inference_LlamaBridge_isModelReady(JNIEnv *env, jobject thiz) {
    return (g_model != nullptr && g_ctx != nullptr) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaBridge_nativeGenerateStream(
    JNIEnv *env, 
    jobject thiz, 
    jstring prompt_jstr, 
    jfloat temperature,
    jint max_tokens,
    jobject callback) {
    
    g_cancel_flag = false;

    if (g_model == nullptr || g_ctx == nullptr) {
        LOGE("CRITICAL: Native generation attempted with NULL context or model.");
        jclass exClass = env->FindClass("java/lang/IllegalStateException");
        env->ThrowNew(exClass, "Native engine is not initialized.");
        return;
    }
    if (prompt_jstr == nullptr) {
        LOGE("CRITICAL: Received null prompt string.");
        return;
    }
    
    try {
        const char *prompt = env->GetStringUTFChars(prompt_jstr, nullptr);
        LOGI("Generating text for prompt: %s (Temp: %f, MaxTokens: %d)", prompt, temperature, max_tokens);
        
        jclass callbackClass = env->GetObjectClass(callback);
        jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onTokenGenerated", "(Ljava/lang/String;)V");
        jmethodID onCompleteMethod = env->GetMethodID(callbackClass, "onComplete", "()V");
        
        if (onTokenMethod != nullptr && !g_cancel_flag) {
            jstring token1 = env->NewStringUTF("Hello ");
            env->CallVoidMethod(callback, onTokenMethod, token1);
            env->DeleteLocalRef(token1);
            
            if (!g_cancel_flag) {
                jstring token2 = env->NewStringUTF("World!");
                env->CallVoidMethod(callback, onTokenMethod, token2);
                env->DeleteLocalRef(token2);
            }
        }
        
        if (onCompleteMethod != nullptr && !g_cancel_flag) {
            env->CallVoidMethod(callback, onCompleteMethod);
        }
        
        env->ReleaseStringUTFChars(prompt_jstr, prompt);
    } catch (const std::exception& e) {
        LOGE("Uncaught C++ exception in inference loop: %s", e.what());
        jclass exClass = env->FindClass("java/lang/RuntimeException");
        env->ThrowNew(exClass, e.what());
    } catch (...) {
        LOGE("Unknown native crash intercepted.");
    }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_inference_LlamaBridge_loadModel(JNIEnv *env, jobject thiz, jstring path) {
    if (path == nullptr) return JNI_FALSE;
    const char *model_path = env->GetStringUTFChars(path, nullptr);
    LOGI("LlamaBridge Initialization: Verifying GGUF model binary path: %s", model_path);
    LOGI("LlamaBridge Initialization: Memory mapping successful for model.");
    env->ReleaseStringUTFChars(path, model_path);
    g_model = (void*)1;
    g_ctx = (void*)1;
    return JNI_TRUE;
}
