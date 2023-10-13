EMPTY :=

CP ?= cp
MKDIRS ?= mkdir -p
RMDIRS ?= rm -rfv

MVN ?= mvn

__DIR__  = $(dir $(@))
__VDIR__ = $(dir $(*))

SRC_JAVA_DIR := src/main/java
BUILD_JAVA_DIR := target/classes

JAVA_API_OBJS = $(shell cd api/$(BUILD_JAVA_DIR) && find . -name "*.class")
JAVA_IMPL_OBJS = $(shell cd impl/$(BUILD_JAVA_DIR) && find . -name "*.class")
JAVA_CLI_OBJS = $(shell cd cli/$(BUILD_JAVA_DIR) && find . -name "*.class")

JAVA_OBJS := \
	$(JAVA_API_OBJS) \
	$(JAVA_IMPL_OBJS) \
	$(JAVA_CLI_OBJS)

.PHONY: all clean clean-% build build-%
all: clean-java build-java
	$(MAKE) -C . bin/http-server.jar

clean: clean-java
	$(RMDIRS) target out bin

clean-java:
	$(MVN) clean

build-java:
	$(MVN) package

$(BUILD_JAVA_DIR)/META-INF/MANIFEST.MF: cli/src/main/resources/MANIFEST.MF
	@$(MKDIRS) $(__DIR__)
	$(CP) $(<) $(@)

HTTP_SERVER_TARGETS := \
	$(BUILD_JAVA_DIR)/META-INF/MANIFEST.MF

bin/http-server.jar: $(HTTP_SERVER_TARGETS)
	@$(MKDIRS) $(__DIR__)
	$(CP) -rv */$(BUILD_JAVA_DIR)/* $(BUILD_JAVA_DIR)
	(cd $(BUILD_JAVA_DIR) && zip ../../$(@) $(subst $$,\$$,$(JAVA_OBJS)) META-INF/MANIFEST.MF)