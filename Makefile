SRC= $(shell find src/ -type f -name '*.java')
OBJ= $(patsubst src/%.java, class/%.class, $(SRC))

.PHONY: all
all: $(OBJ)

class/%.class: src/%.java
	javac --source-path ./src -d ./class $<

.PHONY: clean
clean:
	rm $(OBJ)

.PHONY: play
play:
	java -cp class PlayLevel
