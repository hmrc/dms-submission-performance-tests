import sbt._

object Dependencies {

  private val gatlingVersion = "3.4.2"

  val test = Seq(
    "com.typesafe"          %  "config"                    % "1.3.1",
    "io.gatling"            %  "gatling-test-framework"    % gatlingVersion,
    "io.gatling.highcharts" %  "gatling-charts-highcharts" % gatlingVersion,
    "com.github.pureconfig" %% "pureconfig"                % "0.17.2",
    "com.typesafe.play"     %% "play-ahc-ws-standalone"    % "2.1.10",
    "com.typesafe.play"     %% "play-ws-standalone-json"   % "2.1.2",
    "com.typesafe.akka"     %% "akka-stream"               % "2.6.8"
  ).map(_ % Test)
}
