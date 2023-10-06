MVN   ?= ./mvnw
FLAGS ?= --batch-mode --show-version --errors
POM   := pom.xml

.PHONY: build
build:
	./mvnw $(FLAGS) package --file $(POM)

.PHONY: check
check:
	./mvnw $(FLAGS) verify --file $(POM)

.PHONY: run
run:
	./mvnw $(FLAGS) package servermc:install servermc:copy-plugin servermc:start --file $(POM)

.PHONY: clean
clean:
	./mvnw $(FLAGS) clean --file $(POM)
