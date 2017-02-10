organization := "com.company"

name := "toosheh"

version := "0.0.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation", "-language:postfixOps")

libraryDependencies ++= Seq(
  "com.bisphone" %% "sarf" % "0.7.0"
)
