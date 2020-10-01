VERSION = 1.0.1
BUILD = build
CFLAGS = -std=c99 -O2 -Wall -D_XOPEN_SOURCE=600 -fPIC
LIB_PATH = -I/usr/local/include -I${JAVA_HOME}/include

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
	CFLAGS += -DDEBUG
	LIB_PATH += -I${JAVA_HOME}/include/linux
	OUTFILE = libi2c.so
endif
ifeq ($(UNAME_S),Darwin)
	CFLAGS += -DDEBUG
	LIB_PATH += -I${JAVA_HOME}/include/darwin
	OUTFILE = libi2c.dylib
endif

all:
	mkdir -p build/libs/native
	mkdir -p build/classes
	gcc $(CFLAGS) $(LIB_PATH) src/main/c/i2c.c -o $(BUILD)/libs/native/$(OUTFILE) -shared
	javac -d $(BUILD)/classes src/main/java/org/arl/jhwbus/*.java

test: all
	javac -d $(BUILD)/classes -cp $(BUILD)/classes test/main/java/org/arl/jhwbus/*.java

run: test
	MS

jar: all
	jar cf build/libs/jhwbus-$(UNAME_S)-$(VERSION).jar -C build/classes . -C build libs/native/$(OUTFILE)

clean:
	rm -rf $(BUILD)/*
