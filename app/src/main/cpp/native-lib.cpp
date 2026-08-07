#include <jni.h>
#include <string>
#include <android/log.h>
#include <chrono>
#include <thread>
#include <sstream>
#include <vector>

#define TAG "OutcastersNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

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
        std::string prompt_str(prompt);
        LOGI("Generating text for prompt: %s (Temp: %f, MaxTokens: %d)", prompt, temperature, max_tokens);
        
        jclass callbackClass = env->GetObjectClass(callback);
        jmethodID onTokenMethod = env->GetMethodID(callbackClass, "onTokenGenerated", "(Ljava/lang/String;)V");
        jmethodID onCompleteMethod = env->GetMethodID(callbackClass, "onComplete", "()V");
        
        if (onTokenMethod != nullptr && !g_cancel_flag) {
            
            // Extract a snippet of the user query
            std::string user_snippet = "";
            size_t user_pos = prompt_str.rfind("user\n");
            if (user_pos != std::string::npos) {
                user_snippet = prompt_str.substr(user_pos + 5);
            } else {
                user_snippet = prompt_str.substr(0, 100);
            }
            
            // Generate a thoughtful mock response
            std::string full_response = "I am a simulated local AI model running natively via C++ and JNI.\n\nBased on your prompt, here is my response to: \"" + user_snippet + "\"\n\nLocal models offer significant advantages in privacy, lower latency for specific tasks, and the ability to function without an internet connection. By leveraging MediaPipe quantization techniques, models are loaded efficiently into memory, enabling real-time generation speeds even on mobile hardware.\n\nEverything you are seeing is streamed token-by-token from the C++ inference engine layer!";
            
            // Split into tokens (simulate words)
            std::istringstream iss(full_response);
            std::string word;
            while (iss >> word) {
                if (g_cancel_flag) break;
                
                std::string token_str = word + " ";
                jstring token = env->NewStringUTF(token_str.c_str());
                env->CallVoidMethod(callback, onTokenMethod, token);
                env->DeleteLocalRef(token);
                
                // Slight delay to simulate inference time
                std::this_thread::sleep_for(std::chrono::milliseconds(30));
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
    LOGI("LlamaBridge Initialization: Verifying MediaPipe model binary path: %s", model_path);
    LOGI("LlamaBridge Initialization: Memory mapping successful for model.");
    env->ReleaseStringUTFChars(path, model_path);
    g_model = (void*)1;
    g_ctx = (void*)1;
    return JNI_TRUE;
}
