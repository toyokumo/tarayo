PWD=$(shell pwd)

.PHONY: repl
repl:
	iced repl --with-kaocha with-profile +it

.PHONY: lint
lint:
	clj-kondo --lint src:test:integration
	cljstyle check

.PHONY: install
install:
	lein install

.PHONY: test
test:
	lein test-all

.PHONY: outdated
outdated:
	lein antq

.PHONY: coverage
coverage:
	lein cloverage

.PHONY: benchmark
benchmark:
	cd benchmark && clojure -M:postal
	cd benchmark && clojure -M:tarayo

.PHONY: clean
clean:
	lein clean
