ThisBuild / scalaVersion := "3.8.3"
ThisBuild / scalacOptions ++= Seq(
  "-no-indent",
  "--deprecation",
  "-Wunused:all",
  "-Werror"
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(
    name := "otel-cats-demo",
    fork := true
  )
  .settings(
    libraryDependencies ++= Seq(
      // config
      "com.typesafe" % "config" % "1.4.8",

      // cats-effect
      "org.typelevel" %% "cats-effect" % "3.7.0",

      // circe
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15",

      // http4s
      "org.http4s" %% "http4s-dsl" % "0.23.34",
      "org.http4s" %% "http4s-core" % "0.23.34",
      "org.http4s" %% "http4s-circe" % "0.23.34",
      "org.http4s" %% "http4s-ember-server" % "0.23.34",
      "org.http4s" %% "http4s-ember-client" % "0.23.34",

      // logging
      "ch.qos.logback" % "logback-classic" % "1.5.32",
      "org.typelevel" %% "log4cats-slf4j" % "2.8.0",

      // database
      "com.mysql" % "mysql-connector-j" % "9.7.0",
      "org.flywaydb" % "flyway-mysql" % "12.6.1",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC12",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC12",

      // tapir
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.13.19",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.13.19",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.13.19",

      // otel
      "io.opentelemetry" % "opentelemetry-api" % "1.62.0",
      "io.opentelemetry" % "opentelemetry-sdk" % "1.62.0",
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "1.62.0" % Runtime,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % "1.62.0" % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-jdbc" % "2.27.0-alpha",
      "io.opentelemetry.instrumentation" % "opentelemetry-hikaricp-3.0" % "2.27.0-alpha",
      "org.typelevel" %% "otel4s-oteljava" % "1.0.0",
      "org.typelevel" %% "otel4s-core" % "1.0.0",
      "org.typelevel" %% "otel4s-oteljava-context-storage" % "1.0.0",
      "io.github.arturaz" %% "otel4s-doobie" % "0.15.0",
      "org.http4s" %% "http4s-otel4s-middleware-trace-client" % "0.18.0",
      "org.http4s" %% "http4s-otel4s-middleware-trace-server" % "0.18.0",
    )
  )
  .settings(
    javaAgents += JavaAgent("io.github.irevive" % "otel4s-opentelemetry-javaagent" % "2.27.0"),
    javaOptions ++= Seq(
      "-Dcats.effect.trackFiberContext=true",
      "-Dotel.java.global-autoconfigure.enabled=true",
      "-Dotel.exporter.otlp.protocol=grpc",
      "-Dotel.exporter.otlp.endpoint=http://localhost:4317",
      "-Dotel.bsp.schedule.delay=200",
      "-Dotel.service.name=products-service",
      "-Dotel.resource.attributes=service.namespace=local-demo,service.version=1.1,deployment.environment=local,telemetry.sdk.language=scala",
      "-Dotel.propagators=tracecontext,baggage",
      "-Dotel.traces.sampler=parentbased_traceidratio",
      "-Dotel.instrumentation.common.default-enabled=true",
      "-Dotel.instrumentation.grpc.enabled=true",
      "-Dotel.traces.sampler.arg=1.0",
      "-Dotel.sdk.disabled=false",
      "-Dotel.instrumentation.jdbc.enabled=true",
      "-Dotel.instrumentation.logback-appender.enabled=true",
      "-Dotel.instrumentation.logback-appender.experimental.capture-mdc-attributes=*",
      "-Dotel.instrumentation.logback-appender.experimental.capture-logger-context-attributes=true",
    )
  )
