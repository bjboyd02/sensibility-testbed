# File based on code samples licensed as follows:
#
# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := python2.7
LOCAL_SRC_FILES := libpython2.7.so
# This is where the header files declaring the module's functions live:
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/python2.7/

include $(PREBUILT_SHARED_LIBRARY)

# Build our module
include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/python2.7/
# XXX SHouldn't these automatically set up through LOCAL_EXPORT_C_INCLUDES 
# above, and our `#include`ing Python.h.
LOCAL_SHARED_LIBRARIES := python2.7 unistd

# I think we should use ld, cause this just gave me two priorly unknown log statements, that seem promising
# 07-26 15:43:18.028 10658-11113/com.sensibility_testbed W/linker: /data/app/com.sensibility_testbed-1/lib/arm/libpython2.7.so: is missing DT_SONAME will use basename as a replacement: "libpython2.7.so"
# 07-26 15:43:18.052 10658-11113/com.sensibility_testbed W/linker: '/data/app/com.sensibility_testbed-1/lib/arm/libsnakei.so' library has invalid DT_NEEDED entry '/home/lukas/sensibility-testbed/app/src/main/obj/local/armeabi/libpython2.7.so'

LOCAL_LDLIBS    := -llog # ld
LOCAL_MODULE    := snakei
LOCAL_SRC_FILES := pythonrun.c outputs.c sensors.c location.c media.c miscinfo.c interpreter.c snakei.c pyhelper.c jnihelper.c cjson.c

include $(BUILD_SHARED_LIBRARY)

