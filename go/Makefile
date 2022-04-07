# build: builds the example application
.PHONY: build
build:
	@go build -o example github.com/camunda-cloud/camunda-cloud-get-started/go

# fmt: runs the gofmt in order to format all go files
.PHONY: fmt
fmt:
	@go fmt ./...

# license: add license headers to go files
.PHONY: license
license: deps
	@addlicense -c 'Camunda Services GmbH' -l apache *.go

# check: basic validation
.PHONY: check
check: deps
	@addlicense -check -l apache *.go

# deps: install dependencies
.PHONY: deps
deps:
	@go install github.com/google/addlicense/...@latest

# run: runs the example
.PHONY: run
run: build
	@echo "wait until server is ready, at least 30 seconds..." && sleep 30 && ./example

.PHONY: start-docker
start-docker:
	@docker-compose -f ../docker-compose.yaml up -d

.PHONY: stop-docker
stop-docker:
	@docker-compose -f ../docker-compose.yaml down -v

# all: gets dependencies, formats code, adds licensing, builds, runs checks, starts docker dependencies,
# runs application, stops docker dependencies
.DEFAULT: all
.PHONY: all
all: deps fmt license build check start-docker run stop-docker