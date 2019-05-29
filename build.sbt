
lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion     = "2.5.23"
lazy val sttpVersion     = "1.5.17"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.tumakha",
      scalaVersion    := "2.12.8",
      scalacOptions   ++= Seq("-unchecked", "-deprecation", "-target:jvm-1.8")
    )),
    name := "Currency Converter",
    
    assemblyJarName in assembly := "currency-converter.jar",
    
    libraryDependencies ++= Seq(
      "com.typesafe.akka"     %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"     %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"     %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"     %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka"     %% "akka-http-caching"    % akkaHttpVersion,
      "com.softwaremill.sttp" %% "akka-http-backend"    % sttpVersion,
      "com.softwaremill.sttp" %% "json4s"               % sttpVersion,
      "org.json4s"            %% "json4s-jackson"       % "3.6.6",

      "com.typesafe.akka"     %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka"     %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka"     %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"         %% "scalatest"            % "3.0.5"         % Test
    )
  )
