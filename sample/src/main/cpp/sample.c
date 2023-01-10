#include <sys/mman.h>
#include <unistd.h>
#include <jni.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <stdio.h>

int add(int a,int b){

    return a+b;
}

static int mul(int a,int b){
    return a*b;
}

int func1(int a,int b){

    mprotect(0,1024,7);

    pid_t pid = getpid();

    return add(a,b);
}

int test_mmap(){
    void *m = mmap(0,1024,3,0x22,0,0);

    return (int)m;
}

int func2(JavaVM *vm){
    JNIEnv *env;
    (*vm)->GetEnv(vm,&env,JNI_VERSION_1_6);
    return 0;
}



JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved){
    return JNI_VERSION_1_6;
}


JNIEXPORT jint JNICALL
Java_sample_Sample_func3(JNIEnv *env, jobject thiz) {
    jclass clzz = (*env)->FindClass(env,"sample/Sample");
    if (clzz != NULL){
       //先创建对象
       jmethodID c = (*env)->GetMethodID(env,clzz,"<init>","()V");
       va_list v;
       jobject obj = (*env)->NewObjectV(env,clzz,c,v);

       //再调用方法
       jmethodID m = (*env)->GetMethodID(env,clzz,"aTest","()I");

       jint ret = (*env)->CallIntMethodV(env,obj,m,v);

       jfieldID i = (*env)->GetFieldID(env,clzz,"s","I");

       jint vv = (*env)->GetIntField(env,obj,i);

       (*env)->SetIntField(env,obj,i,1000);

       vv = (*env)->GetIntField(env,obj,i);


        //再调用方法
        jmethodID m1 = (*env)->GetStaticMethodID(env,clzz,"bTest","()I");

        jint ret1 = (*env)->CallStaticIntMethodV(env,clzz,m1,v);

        jfieldID i1 = (*env)->GetStaticFieldID(env,clzz,"a","I");

        jint vv1 = (*env)->GetStaticIntField(env,clzz,i1);

        (*env)->SetStaticIntField(env,clzz,i1,2000);

        vv = (*env)->GetIntField(env,obj,i);


       return vv+ret;
   }

    return -1;
}




JNIEXPORT jobject JNICALL
Java_sample_Sample_call(JNIEnv *env, jobject thiz, jobject in) {

}