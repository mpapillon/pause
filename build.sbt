val catsEffectTimeVersion   	= 	"0.0.4"
val catsEffectVersion       	= 	"1.4.0"
val catsVersion             	= 	"1.6.1"
val circeVersion            	= 	"0.11.1"
val doobieVersion           	= 	"0.7.1"
val flywayVersion           	= 	"6.0.0"
val fs2Version              	= 	"1.0.5"
val fuuidVersion            	= 	"0.2.0"
val http4sVersion           	= 	"0.20.13"
val log4catsVersion         	= 	"0.3.0"
val logbackVersion          	= 	"1.2.3"
val pureconfigVersion       	= 	"0.11.1"
val scalaMockVersion        	= 	"4.4.0"
val scalaTestVersion        	= 	"3.0.8"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    organization := "io.github.mpapillon",
    name := "pause",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.9",
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"              % catsVersion,
      "org.typelevel"         %% "cats-effect"            % catsEffectVersion,
      "co.fs2"                %% "fs2-core"               % fs2Version,
      "co.fs2"                %% "fs2-io"                 % fs2Version,
      "org.http4s"            %% "http4s-blaze-server"    % http4sVersion,
      "org.http4s"            %% "http4s-blaze-client"    % http4sVersion,
      "org.http4s"            %% "http4s-circe"           % http4sVersion,
      "org.http4s"            %% "http4s-dsl"             % http4sVersion,
      "org.tpolecat"          %% "doobie-core"            % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"          % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"        % doobieVersion,
      "org.tpolecat"          %% "doobie-scalatest"       % doobieVersion % "it",
      "io.circe"              %% "circe-generic"          % circeVersion,
      "io.circe"              %% "circe-literal"          % circeVersion,
      "io.circe"              %% "circe-jawn"             % circeVersion,
      "io.chrisdavenport"     %% "fuuid"                  % fuuidVersion,
      "io.chrisdavenport"     %% "fuuid-http4s"           % fuuidVersion,
      "io.chrisdavenport"     %% "fuuid-circe"            % fuuidVersion,
      "io.chrisdavenport"     %% "fuuid-doobie"           % fuuidVersion,
      "io.chrisdavenport"     %% "cats-effect-time"       % catsEffectTimeVersion,
      "com.github.pureconfig" %% "pureconfig"             % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-http4s"      % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats"        % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion,
      "io.chrisdavenport"     %% "log4cats-slf4j"         % log4catsVersion,
      "org.scalatest"         %% "scalatest"              % scalaTestVersion % "test, it",
      "org.scalamock"         %% "scalamock"              % scalaMockVersion % "test",
      "ch.qos.logback"        % "logback-classic"         % logbackVersion,
      "org.flywaydb"          % "flyway-core"             % flywayVersion
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0")
  )

logBuffered in Test := false

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings"
)