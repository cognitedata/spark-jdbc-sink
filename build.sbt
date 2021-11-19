val sparkVersion = "3.2.0"

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.first
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("org", "aopalliance", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "git.properties" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val artifactory = "https://cognite.jfrog.io/cognite/"

resolvers += "libs-release" at artifactory + "libs-release/"
publishTo := {
  if (isSnapshot.value)
    Some("snapshots" at artifactory + "libs-snapshot-local/")
  else
    Some("releases"  at artifactory + "libs-release-local/")
}

lazy val root = (project in file("."))
  .settings(
    organization := "com.cognite.spark",
    name := "spark-jdbc-sink",
    assemblyJarName in assembly := "spark-jdbc-sink-with-dependencies.jar",
    version := "0.0.4",
    scalaVersion := "2.12.15",
    libraryDependencies ++= Seq(
      "org.wso2.carbon.metrics" % "org.wso2.carbon.metrics.jdbc.reporter" % "2.3.7",
      "org.apache.commons" % "commons-dbcp2" % "2.1.1",
      "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
        exclude("org.glassfish.hk2.external", "javax.inject"),
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
        exclude("org.glassfish.hk2.external", "javax.inject"),
      "org.eclipse.jetty" % "jetty-servlet" % "9.3.20.v20170531" % "provided"
    ),
  )

// Don't include Scala in the assembly, we should use the version included in Spark instead
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
