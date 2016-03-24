/*
 * Stolen from https://github.com/aaaaalbert/HelloAndroidJni
 */
#include <Python.h>
#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>

# define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, __FILE__, __VA_ARGS__))

/*
 * This function should be called when the Java code does
 * `System.loadLibrary()`.
 * See https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/invocation.html#JNI_OnLoad
*/
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("Look Ma and Pa, I'm writing to the Android log!");
    return JNI_VERSION_1_6;
}


JNIEXPORT void JNICALL
Java_com_sensibility_1testbed_ScriptActivity_startEmbeddedPython(JNIEnv *env, jobject instance)
{
    jclass main_activity_class;
    jmethodID accelgetter;

    LOGI("Before Py_Initialize...");
    Py_Initialize();
    LOGI("After Py_Initialize...");
    PyRun_SimpleString("open('/sdcard/foo_was_bar', 'w')\n");
//from time import time,ctime\n"
//       "print 'Today is',ctime(time())\n");
    LOGI("Before Py_Finalize...");
    Py_Finalize();
    LOGI("Done. Bye!");
/*
    float anacceleration;
    char floatinstring[16];
    size_t size = 256;
    char current_working_dir[size];

    LOGI("Blix Blypsilon %s", getcwd(current_working_dir, size));


    main_activity_class = (*env)->FindClass(env,
            "com/example/aaaaa/helloandroidjni/MainActivity");

    accelgetter = (*env)->GetStaticMethodID(env, main_activity_class,
            "getAccelValue", "()F");

    anacceleration = (*env)->CallStaticFloatMethod(env, main_activity_class, accelgetter);



    LOGI("Acceleration!!!! It's %f", anacceleration);

    sprintf(floatinstring, "%f", anacceleration);
    return (*env)->NewStringUTF(env, floatinstring);
*/
}
