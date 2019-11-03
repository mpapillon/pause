val catsVersion             = "1.6.1"
val catsEffectVersion       = "1.4.0"
val catsEffectTimeVersion   = "0.1.0"
val fs2Version              = "1.0.5"
val http4sVersion           = "0.20.11"
val doobieVersion           = "0.7.1"
val circeVersion            = "0.11.1"
val fuuidVersion            = "0.2.0"
val pureconfigVersion       = "0.11.1"
val specs2Version           = "4.7.0"
val logbackVersion          = "1.2.3"
val enumeratumVersion       = "1.5.13"
val enumertaumDoobieVersion = "1.5.15"
val enumratumCirceVersion   = "1.5.21"
val flywayVersion           = "6.0.0"

val log4catsVersion = "0.3.0"

lazy val root = (project in file("."))
  .settings(
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
      "org.tpolecat"          %% "doobie-specs2"          % doobieVersion % "test",
      "io.circe"              %% "circe-generic"          % circeVersion,
      "io.circe"              %% "circe-literal"          % circeVersion,
      "io.circe"              %% "circe-jawn"             % circeVersion,
      "io.chrisdavenport"     %% "fuuid"                  % fuuidVersion,
      "io.chrisdavenport"     %% "fuuid-http4s"           % fuuidVersion,
      "io.chrisdavenport"     %% "fuuid-circe"            % fuuidVersion,
      "io.chrisdavenport"     %% "fuuid-doobie"           % fuuidVersion,
      "io.chrisdavenport"     %% "cats-effect-time"       % catsEffectTimeVersion,
      "com.beachape"          %% "enumeratum"             % enumeratumVersion,
      "com.beachape"          %% "enumeratum-doobie"      % enumertaumDoobieVersion,
      "com.beachape"          %% "enumeratum-circe"       % enumratumCirceVersion,
      "com.github.pureconfig" %% "pureconfig"             % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-http4s"      % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats"        % pureconfigVersion,
      "com.github.pureconfig" %% "pureconfig-cats-effect" % pureconfigVersion,
      "io.chrisdavenport"     %% "log4cats-slf4j"         % log4catsVersion,
      "org.specs2"            %% "specs2-core"            % specs2Version % "test",
      "org.specs2"            %% "specs2-cats"            % specs2Version % "test",
      "org.specs2"            %% "specs2-mock"            % specs2Version % "test",
      "ch.qos.logback"        % "logback-classic"         % logbackVersion,
      "org.flywaydb"          % "flyway-core"             % flywayVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")
  )

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
