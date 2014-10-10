name := """plush"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "net.debasishg" % "redisclient_2.11" % "2.13",
  "com.notnoop.apns" % "apns" % "0.2.0",
  ws
)
