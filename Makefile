SRCDIR= ./src
OBJDIR= ./class
SRCPATH= $(shell find $(SRCDIR) -type f -name '*.java')
SRC= $(patsubst $(SRCDIR)/%, %, $(SRCPATH))
OBJPATH= $(patsubst $(SRCDIR)/%.java, $(OBJDIR)/%.class, $(SRCPATH))
OBJ= $(patsubst %.java, %.class, $(SRC))
JAR= run.jar

.PHONY: all
all: $(OBJPATH) $(JAR)

$(OBJPATH):
	javac -d $(OBJDIR) $(SRCPATH)

$(JAR): $(OBJPATH)
	jar cvmf MANIFEST.MF $(JAR) -C $(OBJDIR) .

.PHONY: clean
clean:
	rm -rf $(OBJDIR)
	rm -f $(JAR)

.PHONY: re
re: clean all

.PHONY: play
play:
	java -cp class PlayLevel $(SEED)
