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
LOCAL_JAVA_LIBRARIES := junit-targetdex
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := $(local_module_name)
LOCAL_NO_EMMA_INSTRUMENT := true
LOCAL_NO_EMMA_COMPILE := true
LOCAL_MODULE_PATH := $(TARGET_OUT_DATA)
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(harmony_jdwp_test_src_files)
LOCAL_JAVA_LIBRARIES := junit
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := $(local_module_name)-host
include $(BUILD_HOST_JAVA_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

jdwp_test_runtime := oatexecd
#jdwp_test_runtime := oatexecd -verbose:jdwp
jdwp_test_classpath := /data/$(jdwp_tests_jar):/system/framework/junit-targetdex.jar

# If this fails complaining about TestRunner, build "external/junit" manually.
.PHONY: run-jdwp-tests
run-jdwp-tests: $(TARGET_OUT_DATA)/$(jdwp_tests_jar) $(TARGET_OUT_JAVA_LIBRARIES)/junit-targetdex.jar
	adb shell stop
	adb remount
	adb sync
	adb shell $(jdwp_test_runtime) -cp $(jdwp_test_classpath) \
          -Djpda.settings.verbose=true \
          -Djpda.settings.syncPort=34016 \
          -Djpda.settings.debuggeeJavaPath="$(jdwp_test_runtime)" \
          org.apache.harmony.jpda.tests.share.AllTests

.PHONY: run-jdwp-tests-ri
run-jdwp-tests-ri: $(HOST_OUT_JAVA_LIBRARIES)/$(local_module_name)-host.jar $(HOST_OUT_JAVA_LIBRARIES)/junit.jar
	java -cp $(HOST_OUT_JAVA_LIBRARIES)/$(local_module_name)-host.jar:$(HOST_OUT_JAVA_LIBRARIES)/junit.jar \
          -Djpda.settings.verbose=true \
          -Djpda.settings.syncPort=34016 \
          -Djpda.settings.debuggeeJavaPath=java \
          org.apache.harmony.jpda.tests.share.AllTests
