//
// Created by hanghang on 2021/6/4.
//

#include <jni.h>
#include <iosfwd>

//jstring Java_com_example_healthy_utils_BreakpadTest_testMethod(JNIEnv* env,jobject thiz){
//return  env->NewStringUTF("test breakpad");
//}

jint testMethod(JNIEnv* env){
//    return env->NewStringUTF("test breakpad");
    return 10;
};

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env;
    if(vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK){
        return JNI_ERR;
    }

    jclass c = env->FindClass("com/example/healthy/utils/BreakpadTest");
    if(c == nullptr)
        return JNI_ERR;

    static const JNINativeMethod methods[] = {
            {"testMethod", "()I", reinterpret_cast<void**>(testMethod)}
    };

    int rc = env->RegisterNatives(c, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if(rc != JNI_OK)
        return rc;

    return JNI_VERSION_1_6;
}