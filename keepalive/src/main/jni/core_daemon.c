/*
 * Original Copyright 2015 Mars Kwok
 * Modified work Copyright (c) 2020, weishu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/file.h>
#include <sys/inotify.h>
#include <sys/wait.h>
#include <malloc.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/prctl.h>

#include <android/log.h>

#define TAG		"CoreDaemon"
#define LOGI(...)	__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...)	__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...)	__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define	LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static char* packageList[] = {"com.hinnka.keepalivedemo", "com.hellogeek.fycleanking"};
static int support = 0;

JNIEXPORT jint JNICALL
Java_com_hinnka_libcoredaemon_DaemonNative_nativeDaemon(JNIEnv *env, jobject jobj, jobject context) {
    jclass clz = (*env)->GetObjectClass(env, context);
    jmethodID method = (*env)->GetMethodID(env, clz, "getPackageName", "()Ljava/lang/String;");
    jstring str = (*env)->CallObjectMethod(env, context, method);
    char *packageName = (char *) (*env)->GetStringUTFChars(env, str, 0);
    for (int i = 0; i < sizeof(packageList); ++i) {
        if (strcmp(packageList[i], packageName) == 0) {
            support = 1;
            break;
        }
    }
    if (support == 0) {
        exit(1);
    }
    return 1;
}

JNIEXPORT jint JNICALL
Java_com_hinnka_libcoredaemon_DaemonNative_nativeHoldFileLock(JNIEnv *env, jobject jobj, jstring str) {
    if (support == 0) {
        exit(1);
    }
    char *lock_file_path = (char *) (*env)->GetStringUTFChars(env, str, 0);
//    LOGD("start try to lock file >> %s <<", lock_file_path);
    int lockFileDescriptor = open(lock_file_path, O_RDONLY);
    if (lockFileDescriptor == -1) {
        lockFileDescriptor = open(lock_file_path, O_CREAT, S_IRUSR);
    }
    int lockRet = flock(lockFileDescriptor, LOCK_EX);
    if (lockRet == -1) {
//        LOGE("lock file failed >> %s <<", lock_file_path);
        return 0;
    } else {
//        LOGD("lock file success  >> %s <<", lock_file_path);
        return 1;
    }
}

JNIEXPORT jint JNICALL
Java_com_hinnka_libcoredaemon_DaemonNative_nativeSetSid(JNIEnv *env, jobject jobj) {
    return setsid();
}

JNIEXPORT jint JNICALL
Java_com_hinnka_libcoredaemon_DaemonNative_nativeWaitOneFileLock(JNIEnv *env, jobject jobj, jstring str) {
    char *lock_file_path = (char *) (*env)->GetStringUTFChars(env, str, 0);
    int lockFileDescriptor = open(lock_file_path, O_RDONLY);
    if (lockFileDescriptor == -1) {
        lockFileDescriptor = open(lock_file_path, O_CREAT, S_IRUSR);
    }
    int lockRet;
    while ((lockRet = flock(lockFileDescriptor, LOCK_EX|LOCK_NB)) != -1) {
        flock(lockFileDescriptor, LOCK_UN);
        usleep(1000);
    }
    if (lockRet == -1) {
        flock(lockFileDescriptor, LOCK_EX);
    }
    return 1;
}