#include <jni.h>

//
// Created by moon on 2023/1/9.
//


JNIEXPORT void JNICALL
Java_sample_JniTest_testObjectArray(JNIEnv *env, jobject thiz) {
    jclass clzz = (*env)->FindClass(env,"sample/ObjectA");
    int size = 10;
    jobjectArray array = (*env)->NewObjectArray(env,size,clzz,thiz);

    jmethodID init = (*env)->GetMethodID(env,clzz,"<init>","(I)V");
    jmethodID show = (*env)->GetMethodID(env,clzz,"show","()V");

    for(int i=0;i<size;i++){
        jobject obj = (*env)->NewObject(env,clzz,init,i);
        (*env)->SetObjectArrayElement(env,array,i,obj);
    }

    for(int i=0;i<size;i++){
        jobject obj = (*env)->GetObjectArrayElement(env,array,i);
        (*env)->CallVoidMethod(env,obj,show);
    }

}

JNIEXPORT void JNICALL
Java_sample_JniTest_testBooleanArray(JNIEnv *env, jobject thiz) {
    int size = 10;
    jbooleanArray array = (*env)->NewBooleanArray(env,size);

    jboolean * b = (*env)->GetBooleanArrayElements(env,array,NULL);

    jclass clzz = (*env)->FindClass(env,"sample/ObjectA");
    jmethodID show = (*env)->GetStaticMethodID(env,clzz,"show1", "(Z)V");

    for(int i=0;i<size;i++){
        jboolean obj = b[i];

        (*env)->CallStaticVoidMethod(env,clzz,show,obj);
    }

}