BUILD = build

CFLAGS = -std=c99 -O2 -Wall -D_XOPEN_SOURCE=600 -fPIC

LIB_PATH = -I/usr/local/include -I${JAVA_HOME}/include
UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
	LIB_PATH += -I${JAVA_HOME}/include/linux
	OUTFILE = libi2c.so
endif
ifeq ($(UNAME_S),Darwin)
	CFLAGS += -DDEBUG
	LIB_PATH += -I${JAVA_HOME}/include/darwin
	OUTFILE = libi2c.dylib
endif

all:
	mkdir -p build/libs
	mkdir -p build/classes
	gcc $(CFLAGS) $(LIB_PATH) src/main/c/i2c.c -o $(BUILD)/libs/$(OUTFILE) -shared
	javac -d $(BUILD)/classes src/main/java/org/arl/jhwbus/*.java test/main/java/org/arl/jhwbus/*.java

run:
	cd $(BUILD)/classes; java -Djava.library.path=../libs TestI2C; cd ../..

clean:
	rm -rf $(BUILD)/*
