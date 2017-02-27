all: sbt
		bin/sbt assembly && cp target/scala-2.11/asciibird.jar bin

clean: sbt
		bin/sbt clean
		(test -e bin/asciibird.jar && rm bin/asciibird.jar) || true
		(test -e bin/asciibird.log && rm bin/asciibird.log) || true

distclean:
		(test -e bin/sbt && rm bin/sbt) || true

sbt:
		cd bin && test -e sbt || curl -sO https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt && chmod u+x sbt

$(VERBOSE).SILENT:
