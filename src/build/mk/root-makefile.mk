EMPTY :=

CP ?= cp
MKDIRS ?= mkdir -p
RMDIRS ?= rm -rfv
LN ?= ln -s

MVN ?= mvn

__DIR__  = $(dir $(@))
__VDIR__ = $(dir $(*))

SRC_JAVA_DIR := src/main/java
BUILD_JAR_DIR := target/classes

JAVA_API_OBJS = $(shell cd api/$(BUILD_JAR_DIR) && find . -name "*.class")
JAVA_IMPL_OBJS = $(shell cd impl/$(BUILD_JAR_DIR) && find . -name "*.class")
JAVA_CLI_OBJS = $(shell cd cli/$(BUILD_JAR_DIR) && find . -name "*.class")
JAVA_SAMPLE_OBJS = $(shell cd sample/$(BUILD_JAR_DIR) && find . -name "*.class")

SAMPLE_RESOURCE_FILES := \
	secret/server.keystore \
	secret/server.truststore

SAMPLE_RESOURCE_SOURCES := \
	$(addprefix sample/src/main/resources/,$(SAMPLE_RESOURCE_FILES))

SAMPLE_BUNDLE_OBJS := \
	$(SAMPLE_RESOURCE_SOURCES)

JAVA_OBJS := \
	$(JAVA_API_OBJS) \
	$(JAVA_IMPL_OBJS) \
	$(JAVA_SAMPLE_OBJS)

.PHONY: all clean clean-% build build-%
all: clean-java build-java
	$(MAKE) -C . bin/demo.jar

clean: clean-java
	$(RMDIRS) target out bin

clean-java:
	$(MVN) clean

build-java:
	$(MVN) package

$(BUILD_JAR_DIR)/META-INF/MANIFEST.MF: cli/src/main/resources/MANIFEST.MF
	@$(MKDIRS) $(__DIR__)
	$(CP) $(<) $(@)

$(BUILD_JAR_DIR)/htdocs/index.html: sample/src/main/html/pages/index.html
	@$(MKDIRS) $(__DIR__)
	$(CP) $(<) $(@)

$(BUILD_JAR_DIR)/htdocs/favicon.css: sample/src/main/css/%.css
	@$(MKDIRS) $(__DIR__)
	$(CP) $(^) $(@)

$(BUILD_JAR_DIR)/htdocs/css/%.css: sample/src/main/css/%.css
	@$(MKDIRS) $(__DIR__)
	$(CP) $(^) $(@)

DEMO_TARGETS := \
	$(BUILD_JAR_DIR)/META-INF/MANIFEST.MF \
	$(BUILD_JAR_DIR)/htdocs/index.html \
	$(BUILD_JAR_DIR)/htdocs/css/reset.css \
	$(BUILD_JAR_DIR)/htdocs/css/style.css

libexec/demo-0.1.0.jar: $(DEMO_TARGETS)
	@$(MKDIRS) $(__DIR__)
#	# Copy target directories
	$(CP) -rv */$(BUILD_JAR_DIR)/* $(BUILD_JAR_DIR)
#	# Copy target files
#	# $(CP) -v $(SAMPLE_BUNDLE_OBJS) $(BUILD_JAVA_DIR)
#	#
#	# (cd $(BUILD_JAVA_DIR) && zip ../../$(@) $(subst $$,\$$,$(JAVA_OBJS)) META-INF/MANIFEST.MF)
	(cd $(BUILD_JAR_DIR) && zip -r ../../$(@) *)

bin/demo.jar: libexec/demo-0.1.0.jar
	@$(MKDIRS) $(__DIR__)
	$(LN) -f ../$(<)  $(@)