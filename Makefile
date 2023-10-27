CP ?= cp
MKDIRS ?= mkdir -p
RMDIRS ?= rm -rfv
LN ?= ln -s

MVN ?= mvn
JAVA ?= java
CURL ?= curl

__DIR__  = $(dir $(@))
BUILD_JAR_DIR := target/classes

JAVA_DEBUG_PORT ?= 5005
JAVA_DEBUG_OPTS ?= -agentlib:jdwp=transport=dt_socket,address=$(JAVA_DEBUG_PORT),server=y,suspend=n
JAVA_OPTS ?=

.PHONY: all clean clean-% build build-% run run-% debug debug-% download download-%
all: clean-java build-java
	$(MAKE) -C . bin/demo.jar

clean: clean-java
	$(RMDIRS) target out bin

clean-java:
	$(MVN) clean

debug-demo: JAVA_OPTS += $(JAVA_DEBUG_OPTS)
run-demo debug-demo: bin/demo.jar
	(cd bin && $(JAVA) -jar $(JAVA_OPTS) demo.jar)

sample/src/3pp/js/htmx.min.js:
	@$(MKDIRS) $(__DIR__)
	$(CURL) -L -o $(@) https://unpkg.com/htmx.org@1.9.6/dist/htmx.min.js

download-dep-javascript: \
		sample/src/3pp/js/htmx.min.js

build-java: download-dep-javascript
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

$(BUILD_JAR_DIR)/htdocs/js/%.js: sample/src/3pp/js/%.js
	@$(MKDIRS) $(__DIR__)
	$(CP) $(^) $(@)

DEMO_TARGETS := \
	$(BUILD_JAR_DIR)/META-INF/MANIFEST.MF \
	$(BUILD_JAR_DIR)/htdocs/index.html \
	$(BUILD_JAR_DIR)/htdocs/css/reset.css \
	$(BUILD_JAR_DIR)/htdocs/css/style.css \
	$(BUILD_JAR_DIR)/htdocs/js/htmx.min.js \

libexec/demo-0.1.0.jar: $(DEMO_TARGETS)
	@$(MKDIRS) $(__DIR__)
	$(RM) $(@)
	$(CP) -rv */$(BUILD_JAR_DIR)/* $(BUILD_JAR_DIR)
	(cd $(BUILD_JAR_DIR) && zip -r ../../$(@) *)

bin/demo.jar: libexec/demo-0.1.0.jar
	@$(MKDIRS) $(__DIR__)
	$(LN) -f ../$(<)  $(@)