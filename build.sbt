name := """seddit"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test
)
libraryDependencies += "com.typesafe.play" %% "play-slick" % "1.1.1"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1"
libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1103-jdbc41"
libraryDependencies += "com.zaxxer" % "HikariCP" % "2.3.9"
libraryDependencies += "jp.t2v" %% "play2-auth" % "0.14.0"

libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.10.1"

libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.10.11"
libraryDependencies += "org.imgscalr" % "imgscalr-lib" % "4.2"

libraryDependencies +="io.github.jto" %% "validation-core" % "1.1"
libraryDependencies +="io.github.jto" %% "validation-json" % "1.1"

libraryDependencies += "org.julienrf" %% "play-jsmessages" % "2.0.0"

libraryDependencies += "com.gilt" % "jerkson_2.11" % "0.6.8"

libraryDependencies += "com.nappin" %% "play-recaptcha" % "1.5"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

routesGenerator := InjectedRoutesGenerator
