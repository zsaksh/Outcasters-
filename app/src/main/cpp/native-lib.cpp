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
