name := """plush"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  ws,
  "net.debasishg" %% "redisclient" % "2.13",
  "com.github.t3hnar" %% "scala-bcrypt" % "2.4",
  "com.notnoop.apns" % "apns" % "0.2.0",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "org.scalatestplus" %% "play" % "1.0.0" % "test"
  )

scalaVersion := "2.11.7"

