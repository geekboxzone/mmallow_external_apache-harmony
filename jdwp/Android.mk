# -*- mode: makefile -*-

LOCAL_PATH := $(call my-dir)

define all-harmony-test-java-files-under
  $(patsubst ./%,%,$(shell cd $(LOCAL_PATH) && find $(2) -name "*.java" 2> /dev/null))
endef

harmony_jdwp_test_src_files := \
    $(call all-harmony-test-java-files-under,,src/test/java/)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(harmony_jdwp_test_src_files)
LOCAL_JAVA_LIBRARIES := junit-targetdex
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := apache-harmony-jdwp-tests
LOCAL_NO_EMMA_INSTRUMENT := true
LOCAL_NO_EMMA_COMPILE := true
LOCAL_MODULE_PATH := $(TARGET_OUT_DATA)/jdwp
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(harmony_jdwp_test_src_files)
LOCAL_JAVA_LIBRARIES := junit
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := apache-harmony-jdwp-tests-host
include $(BUILD_HOST_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(harmony_jdwp_test_src_files)
LOCAL_JAVA_LIBRARIES := junit
LOCAL_MODULE_TAGS := tests
LOCAL_MODULE := apache-harmony-jdwp-tests-hostdex
LOCAL_BUILD_HOST_DEX := true
include $(BUILD_HOST_JAVA_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))

#jdwp_test_runtime_target := oatexec
jdwp_test_runtime_target := oatexecd
jdwp_test_runtime_host := $(ANDROID_BUILD_TOP)/art/tools/art

jdwp_test_runtime_options :=
jdwp_test_runtime_options += -verbose:jdwp
#jdwp_test_runtime_options += -Xint
#jdwp_test_runtime_options += -verbose:threads
jdwp_test_timeout_ms := 10000 # 10s.

jdwp_test_classpath_host := $(ANDROID_HOST_OUT)/framework/apache-harmony-jdwp-tests-hostdex.jar:$(ANDROID_HOST_OUT)/framework/junit-hostdex.jar
jdwp_test_classpath_target := /data/jdwp/apache-harmony-jdwp-tests.jar:/data/junit/junit-targetdex.jar

# If this fails complaining about TestRunner, build "external/junit" manually.
.PHONY: run-jdwp-tests
run-jdwp-tests: $(TARGET_OUT_DATA)/jdwp/apache-harmony-jdwp-tests.jar $(TARGET_OUT_DATA)/junit/junit-targetdex.jar
	adb shell stop
	adb remount
	adb sync
	adb shell $(jdwp_test_runtime_target) -cp $(jdwp_test_classpath_target) \
          -Djpda.settings.verbose=true \
          -Djpda.settings.syncPort=34016 \
          -Djpda.settings.debuggeeJavaPath="$(jdwp_test_runtime_target) $(jdwp_test_runtime_options)" \
          -Djpda.settings.timeout=$(jdwp_test_timeout_ms) \
          -Djpda.settings.waitingTime=$(jdwp_test_timeout_ms) \
          org.apache.harmony.jpda.tests.share.AllTests

# If this fails complaining about TestRunner, build "external/junit" manually.
.PHONY: run-jdwp-tests-host
run-jdwp-tests-host: $(HOST_OUT_JAVA_LIBRARIES)/apache-harmony-jdwp-tests-hostdex.jar $(HOST_OUT_JAVA_LIBRARIES)/junit-hostdex.jar
	$(jdwp_test_runtime_host) -cp $(jdwp_test_classpath_host) \
          -Djpda.settings.verbose=true \
          -Djpda.settings.syncPort=34016 \
          -Djpda.settings.debuggeeJavaPath="$(jdwp_test_runtime_host) $(jdwp_test_runtime_options)" \
          -Djpda.settings.timeout=$(jdwp_test_timeout_ms) \
          -Djpda.settings.waitingTime=$(jdwp_test_timeout_ms) \
          org.apache.harmony.jpda.tests.share.AllTests

.PHONY: run-jdwp-tests-ri
run-jdwp-tests-ri: $(HOST_OUT_JAVA_LIBRARIES)/apache-harmony-jdwp-tests-host.jar $(HOST_OUT_JAVA_LIBRARIES)/junit.jar
	java -cp $(HOST_OUT_JAVA_LIBRARIES)/apache-harmony-jdwp-tests-host.jar:$(HOST_OUT_JAVA_LIBRARIES)/junit.jar \
          -Djpda.settings.verbose=true \
          -Djpda.settings.syncPort=34016 \
          -Djpda.settings.debuggeeJavaPath=java \
          org.apache.harmony.jpda.tests.share.AllTests
