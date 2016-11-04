resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.8.0")

scalaVersion := "2.11.8"

val scalazVersion = "7.2.2"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.scalaz" %% "scalaz-effect" % scalazVersion,
  "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.chuusai" %% "shapeless" % "2.3.1",
  "org.scalacheck" %% "scalacheck" % "1.13.2"
)


initialCommands in console := Seq(
  "import scalaz._, Scalaz._",
  "import shapeless._"
).mkString("; ")

//scalacOptions ++= Seq("-Xlog-implicits")

scalacOptions in Compile ++= Seq(
  "-language:higherKinds",
  "-feature",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xlint")

scalacOptions in (Compile, console) := Seq(
  "-language:higherKinds",
  "-feature")

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
