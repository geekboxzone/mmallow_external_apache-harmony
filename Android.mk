# -*- mode: makefile -*-

LOCAL_PATH := $(call my-dir)

define all-harmony-test-java-files-under
  $(foreach dir,$(1),$(patsubst ./%,%,$(shell cd $(LOCAL_PATH) && find $(dir)/$(2) -name "*.java" 2> /dev/null)))
endef

harmony_test_dirs := annotation archive logging math nio nio_char prefs regex support text
harmony_test_src_files := \
    $(call all-harmony-test-java-files-under,$(harmony_test_dirs),src/test/java) \
    $(call all-harmony-test-java-files-under,luni,src/test/{api,impl}/{common,unix})

define harmony-test-resource-dirs
  $(shell cd $(LOCAL_PATH) && ls -d $(1)/src/test/{java,resources} 2> /dev/null)
endef
harmony_test_resource_dirs := \
    $(call harmony-test-resource-dirs,$(harmony_test_dirs)) \
    $(call harmony-test-resource-dirs,luni)

harmony_test_javac_flags=-encoding UTF-8
harmony_test_javac_flags+=-Xmaxwarns 9999999

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(harmony_test_src_files)
LOCAL_JAVA_RESOURCE_DIRS := $(harmony_test_resource_dirs)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core core-junit
LOCAL_JAVACFLAGS := $(harmony_test_javac_flags)
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := apache-harmony-tests
LOCAL_NO_EMMA_INSTRUMENT := true
LOCAL_NO_EMMA_COMPILE := true
include $(BUILD_STATIC_JAVA_LIBRARY)

ifeq ($(WITH_HOST_DALVIK),true)
    include $(CLEAR_VARS)
    LOCAL_SRC_FILES := $(harmony_test_src_files)
    LOCAL_JAVA_RESOURCE_DIRS := $(harmony_test_resource_dirs)
    LOCAL_NO_STANDARD_LIBRARIES := true
    LOCAL_JAVA_LIBRARIES := core-hostdex core-junit-hostdex
    LOCAL_JAVACFLAGS := $(harmony_test_javac_flags)
    LOCAL_MODULE_TAGS := tests
    LOCAL_MODULE := apache-harmony-tests-host
    LOCAL_NO_EMMA_INSTRUMENT := true
    LOCAL_NO_EMMA_COMPILE := true
    LOCAL_BUILD_HOST_DEX := true
    include $(BUILD_HOST_JAVA_LIBRARY)
endif
