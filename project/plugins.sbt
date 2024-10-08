addSbtPlugin("org.scalameta"     % "sbt-scalafmt"              % "2.5.2")
addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"              % "0.13.0")
addSbtPlugin("com.github.gseitz" % "sbt-release"               % "1.0.13")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager"       % "1.8.1")
addSbtPlugin("com.github.cb372"  % "sbt-explicit-dependencies" % "0.3.1")

libraryDependencies += "com.spotify" % "docker-client" % "8.16.0"