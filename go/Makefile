# build: builds the example application
.PHONY: build
build:
	@go build -o example github.com/camunda/camunda-platform-get-started/go

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

# all: gets dependencies, formats code, adds licensing, builds, runs checks, starts docker dependencies,
# runs application, stops docker dependencies
.DEFAULT: all
.PHONY: all
all: deps fmt license build check