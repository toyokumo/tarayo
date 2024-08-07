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
	clojure -T:build install

.PHONY: test
test:
	clojure -M:dev:test unit
	clojure -M:dev:1.9:test unit
	clojure -M:dev:1.10:test unit

.PHONY: test-integration
test-integration:
	clojure -M:dev:test it
	clojure -M:dev:1.9:test it
	clojure -M:dev:1.10:test it

.PHONY: outdated
outdated:
	clojure -Tantq outdated

.PHONY: benchmark
benchmark:
	cd benchmark && clojure -M:postal
	cd benchmark && clojure -M:tarayo

.PHONY: deploy
deploy:
	clojure -T:build deploy

.PHONY: clean
clean:
	rm -rf target
