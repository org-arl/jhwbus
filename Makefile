VERSION = 1.1.0
BUILD = build
CFLAGS = -std=c99 -O2 -Wall -Wextra -Werror -Wno-unused-parameter -D_XOPEN_SOURCE=600 -fPIC
LIB_PATH = -I/usr/local/include -I${JAVA_HOME}/include

UNAME_S := $(shell uname -s | tr '[:upper:]' '[:lower:]')
UNAME_M := $(shell uname -m | tr '[:upper:]' '[:lower:]')
ifeq ($(UNAME_S),linux)
	LIB_PATH += -I${JAVA_HOME}/include/linux
	LFLAGS = -li2c
	OUTFILE = libi2c-${UNAME_M}.so
endif

ifeq ($(UNAME_S),darwin)
	LIB_PATH += -I${JAVA_HOME}/include/darwin
	LFLAGS = 
	OUTFILE = libi2c-${UNAME_M}.dylib
endif

ifdef DEBUG
	CFLAGS += -DDEBUG
endif

all:
	mkdir -p build/libs/native
	mkdir -p build/classes
	gcc $(CFLAGS) $(LIB_PATH) src/main/c/i2c.c -o $(BUILD)/libs/native/$(OUTFILE) -shared $(LFLAGS)
	javac -d $(BUILD)/classes src/main/java/org/arl/jhwbus/*.java

test: all
	javac -d $(BUILD)/classes -cp $(BUILD)/classes test/main/java/org/arl/jhwbus/*.java

run: test
	MS

jar: all
	jar cf build/libs/jhwbus-$(UNAME_S)-$(VERSION).jar -C build/classes . -C build libs/native/$(OUTFILE)
	@echo "Generated build/libs/jhwbus-$(UNAME_S)-$(VERSION).jar"

clean:
	rm -rf $(BUILD)/*
