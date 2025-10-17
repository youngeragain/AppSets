#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring

//purple_1native为什么不能写成purple_native, 因为写成purple_native会将native看成是purple的子包
//而实际上在定义包的时候，purple_native是一个包名
JNICALL
Java_xcj_app_purple_1native_starter_PurpleNative_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jint

JNICALL
Java_xcj_app_purple_1native_starter_Purple_getNumber(
        JNIEnv *env, jobject
) {
    return random();
}

