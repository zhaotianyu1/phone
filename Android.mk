
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# Module name should match apk name to be installed
LOCAL_MODULE := TclPhone

# ==============================================================================
# The template file for APK

ifeq ($(shell test $(PLATFORM_SDK_VERSION) -ge 26 && echo AndroidO), AndroidO)
LOCAL_PROPRIETARY_MODULE := true
endif

LOCAL_SRC_FILES := TclPhone.apk

LOCAL_CFLAGS := -Wall -O2 -U_FORTIFY_SOURCE -fstack-protector-all

LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX:=$(COMMON_ANDROID_PACKAGE_SUFFIX)

ifeq ($(shell test $(PLATFORM_SDK_VERSION) -ge 26 && echo AndroidO), AndroidO)
LOCAL_MODULE_PATH := $(TARGET_OUT_VENDOR_APPS)
else
LOCAL_MODULE_PATH := $(TARGET_OUT_APPS) 
endif
LOCAL_CERTIFICATE:= platform
LOCAL_MODULE_TAGS := optional
include $(BUILD_PREBUILT)




