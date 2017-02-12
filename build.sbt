organization := "com.company"

name := "toosheh"

version := "0.0.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation", "-language:postfixOps")

def akka (
           module: String,
           version: String = "2.4.4"
         ) = "com.typesafe.akka" %% module % version

fork := true

libraryDependencies ++= Seq(
  "com.bisphone" %% "sarf" % "0.7.0"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "com.bisphone" %% "testkit" % "0.4.0" % Test,
  akka("akka-testkit") % Test,
  akka("akka-stream-testkit") % Test
)