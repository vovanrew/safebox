name := "safebox"
 
version := "1.0" 

lazy val `safebox` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  jdbc ,
  ehcache ,
  ws ,
  specs2 % Test ,
  guice,
  //--mongo driver
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.3.0",
  //--scalikejdbc
  "org.scalikejdbc" %% "scalikejdbc"       % "3.2.2",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "3.2.2",
  "mysql" % "mysql-connector-java" % "5.1.41",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  //--bcrypt hashing library
  "com.github.t3hnar" %% "scala-bcrypt" % "3.1"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )