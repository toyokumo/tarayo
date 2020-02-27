.PHONY: repl lint install test clean

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

clean:
	lein clean
