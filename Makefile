JAVAC   = javac
sources = project/benchmarks/Main.java \
          project/cache/Cache.java \
          project/cache/CoarsegrainLFU.java \
          project/cache/ConcurrentLFU.java \
          project/cache/NonLinearizableLFU.java \
          project/cache/SequentialLFU.java \
          project/util/ConcurrentDoublyLinkedList.java \
          project/util/IODevice.java

classes = $(sources:.java=.class)

all: $(classes)

%.class : %.java
	$(JAVAC) $<
