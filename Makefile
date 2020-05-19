.PHONY: repl lint install test outdated clean

PWD=$(shell pwd)

repl:
	iced repl --with-kaocha with-profile +it

lint:
	clj-kondo --lint src:test:integration
	cljstyle check

install:
	lein install

test:
	lein test-all

outdated:
	lein outdated

clean:
	lein clean
