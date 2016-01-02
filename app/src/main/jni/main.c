//
// Created by perne on 02/01/16.
//

#include "lille_telecom_opencvpernemorin_MainActivity.h"

JNIEXPORT jstring JNICALL Java_lille_telecom_opencvpernemorin_MainActivity_getStringFromNative(JNIEnv* env, jobject thiz)
{
    return (*env)->NewStringUTF(env, "Hello NDK :)");
}
