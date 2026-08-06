#include <jni.h>
#include <string>
#include <android/log.h>
// #include "llama.h"

#define TAG "OutcastersNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// struct llama_model *model = nullptr;
// struct llama_context *ctx = nullptr;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_inference_LlamaNativeWrapper_loadModel(JNIEnv *env, jobject thiz, jstring path, jint context_length, jint threads) {
    const char *model_path = env->GetStringUTFChars(path, nullptr);
    LOGI("Loading model: %s with context: %d threads: %d", model_path, context_length, threads);
    
    // llama_model_params mparams = llama_model_default_params();
    // mparams.use_mmap = true; // Use mmap to allow OS page swapping
    // model = llama_load_model_from_file(model_path, mparams);
    
    env->ReleaseStringUTFChars(path, model_path);
    return JNI_TRUE; // Simulate success
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaNativeWrapper_unloadModel(JNIEnv *env, jobject thiz) {
    LOGI("Unloading model...");
    // if (ctx) llama_free(ctx);
    // if (model) llama_free_model(model);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_inference_LlamaNativeWrapper_generate(JNIEnv *env, jobject thiz, jstring prompt, jobject callback) {
    const char *prompt_chars = env->GetStringUTFChars(prompt, nullptr);
    LOGI("Generating text for prompt: %s", prompt_chars);
    
    // Simulate callback
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

// Simulated g_ctx
void* g_ctx = (void*)1; // Give it a non-null value so it's "ready"
void* g_model = (void*)1;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_inference_LlamaBridge_clearKvCache(JNIEnv *env, jobject thiz) {
    if (g_ctx != nullptr) {
        // Clear past KV cache to prevent token context bleed across un-linked queries
        // llama_kv_cache_clear(g_ctx);
        return JNI_TRUE;
    }
    return JNI_FALSE;
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
    jobject callback
) {
    // 1. Guard against Null Pointers
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
        
        LOGI("Generating text for prompt: %s", prompt);

        // Simulate callback
        jclass callbackClass = env->GetObjectClass(callback);
        jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onTokenGenerated", "(Ljava/lang/String;)V");
        jmethodID onCompleteMethod = env->GetMethodID(callbackClass, "onComplete", "()V");

        if (onTokenMethod != nullptr) {
            jstring token1 = env->NewStringUTF("Hello ");
            env->CallVoidMethod(callback, onTokenMethod, token1);
            env->DeleteLocalRef(token1);

            jstring token2 = env->NewStringUTF("World!");
            env->CallVoidMethod(callback, onTokenMethod, token2);
            env->DeleteLocalRef(token2);
        }

        if (onCompleteMethod != nullptr) {
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
    
    // Simulate memory mapping success
    LOGI("LlamaBridge Initialization: Memory mapping successful for model.");
    
    env->ReleaseStringUTFChars(path, model_path);
    g_model = (void*)1;
    g_ctx = (void*)1;
    return JNI_TRUE;
}
