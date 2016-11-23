JAVAC   = javac
sources = project/benchmarks/Main.java \
          project/benchmarks/Cache.java \
          project/sequential/LFUCache.java \
          project/coarsegrain/CoarseGrainLFUCache.java
classes = $(sources:.java=.class)

all: $(classes)

%.class : %.java
	$(JAVAC) $<