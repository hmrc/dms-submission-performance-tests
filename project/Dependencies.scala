import sbt._

object Dependencies {

  private val gatlingVersion = "3.9.0"

  val test = Seq(
    "io.gatling"            %  "gatling-test-framework"    % gatlingVersion,
    "io.gatling.highcharts" %  "gatling-charts-highcharts" % gatlingVersion,
    "com.github.pureconfig" %% "pureconfig"                % "0.17.7",
    "org.playframework"     %% "play-ahc-ws-standalone"    % "3.0.3",
    "org.playframework"     %% "play-ws-standalone-json"   % "3.0.3",
  ).map(_ % Test)
}
