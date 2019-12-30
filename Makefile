BUILD = build/libs

CFLAGS = -std=c99 -O2 -Wall -D_XOPEN_SOURCE=600 -fPIC

LIB_PATH = -I/usr/local/include -I${JAVA_HOME}/include
UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
	LIB_PATH += -I${JAVA_HOME}/include/linux
endif
ifeq ($(UNAME_S),Darwin)
	CFLAGS += -DDEBUG
	LIB_PATH += -I${JAVA_HOME}/include/darwin
endif

all:
	mkdir -p build/libs
	gcc $(CFLAGS) $(LIB_PATH) src/main/c/i2c.c -o $(BUILD)/i2c.jnilib -shared


clean:
	rm -rf $(BUILD)/*
