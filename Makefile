all:
		@bin/sbt assembly && cp target/scala-2.10/asciibird.jar bin

clean:
		@bin/sbt clean
		@(test -e bin/asciibird.jar && rm bin/asciibird.jar) || true
		@(test -e bin/asciibird.log && rm bin/asciibird.log) || true
