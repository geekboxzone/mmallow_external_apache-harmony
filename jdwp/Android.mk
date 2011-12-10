# -*- mode: makefile -*-

LOCAL_PATH := $(call my-dir)

define all-harmony-test-java-files-under
  $(patsubst ./%,%,$(shell cd $(LOCAL_PATH) && find $(2) -name "*.java" 2> /dev/null))
endef

harmony_jdwp_test_src_files := \
    $(call all-harmony-test-java-files-under,,src/test/java/)

local_module_name := apache-harmony-jdwp-tests
jdwp_tests_jar := $(local_module_name).jar

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(harmony_jdwp_test_src_files)
LOCAL_NO_STANDARD_LIBRARIES := true
LOCAL_JAVA_LIBRARIES := core core-junit junit-targetdex
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := $(local_module_name)
LOCAL_NO_EMMA_INSTRUMENT := true
LOCAL_NO_EMMA_COMPILE := true
LOCAL_MODULE_PATH := $(TARGET_OUT_DATA)
include $(BUILD_JAVA_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

jdwp_test_runtime := dalvikvm
jdwp_test_classpath := /data/$(jdwp_tests_jar):/system/framework/junit-targetdex.jar


.PHONY: run-jdwp-tests
run-jdwp-tests:
	adb shell stop
	adb remount
	adb sync
	adb shell $(jdwp_test_runtime) -cp $(jdwp_test_classpath) \
          -Djpda.settings.verbose=true \
          -Djpda.settings.syncHost=localhost \
          -Djpda.settings.syncPort=34016 \
          -Djpda.settings.debuggeeJavaPath="$(jdwp_test_runtime)" \
          -Djpda.settings.debuggeeClassPath="$(jdwp_test_classpath)" \
          org.apache.harmony.jpda.tests.share.AllTests
