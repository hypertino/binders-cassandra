name := "cassandra-binders"

version := "0.4.0"

organization := "com.hypertino"

crossScalaVersions := Seq("2.12.4", "2.11.12")

scalaVersion := crossScalaVersions.value.head

resolvers ++= Seq(
  Resolver.sonatypeRepo("public")
)

libraryDependencies ++= Seq(
  "com.datastax.cassandra" % "cassandra-driver-core" % "2.1.10.3", // 3.2.0, 3.1.2, 2.1.10.3
  "com.google.guava" % "guava" % "19.0",
  "com.hypertino" %% "binders" % "1.2.1",
  "io.monix" %% "monix" % "2.3.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.1.8" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % "test",
  "org.cassandraunit" % "cassandra-unit" % "2.1.3.1" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "junit" % "junit" % "4.12" % "test"
)

//libraryDependencies += "com.google.code.findbugs" % "jsr305" % "1.3.+"

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value
    // in Scala 2.10, quasiquotes are provided by macro paradise
    case Some((2, 10)) =>
      libraryDependencies.value ++ Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.1.0" cross CrossVersion.binary)
  }
}

fork in Test := true