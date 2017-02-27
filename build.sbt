scalaVersion := "2.11.8"

run in Compile := {
  state.value.log.info("To run, assemble a fat jar via `assembly`")
}

mainClass in assembly := Some("AsciiBird")

assemblyJarName in assembly := "asciibird.jar"

libraryDependencies ++= Seq(
  "org.slf4j"                % "slf4j-api"                % "1.7.24",
  "org.slf4j"                % "log4j-over-slf4j"         % "1.7.24",
  "ch.qos.logback"           % "logback-classic"          % "1.2.1",
  "org.scala-lang.modules"  %% "scala-parser-combinators" % "1.0.5",
  "com.googlecode.lanterna"  % "lanterna"                 % "2.1.9",
  "jline"                    % "jline"                    % "2.11",
  "org.scalatest"           %% "scalatest"                % "3.0.1" % Test,
  "org.mockito"              % "mockito-core"             % "1.9.5" % Test
)
