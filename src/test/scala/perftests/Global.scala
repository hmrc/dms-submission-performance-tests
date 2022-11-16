/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package perftests

import akka.actor.ActorSystem
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Global {

  private val configuration: ConfigObjectSource = {
    val default = ConfigSource.default
    if (default.at("runLocal").loadOrThrow[Boolean]) {
      default.withFallback(ConfigSource.resources("services-local.conf"))
    } else {
      default.withFallback(ConfigSource.resources("services.conf"))
    }
  }

  val isLocal: Boolean = configuration.at("runLocal").loadOrThrow[Boolean]
  val isSmokeTest: Boolean = configuration.at("perftest.runSmokeTest").loadOrThrow[Boolean]

  val rampUpTime: FiniteDuration = configuration.at("perftest.rampupTime").loadOrThrow[Int].minutes
  val rampDownTime: FiniteDuration = configuration.at("perftest.rampdownTime").loadOrThrow[Int].minutes
  val constantRateTime: FiniteDuration = configuration.at("perftest.constantRateTime").loadOrThrow[Int].minutes
  val loadPercentage: Double = configuration.at("perftest.loadPercentage").loadOrThrow[Double]

  val internalAuth: Service = configuration.at("services.internal-auth").loadOrThrow[Service]
  val dmsSubmission: Service = configuration.at("services.dms-submission").loadOrThrow[Service]
  val dmsSubmissionStub: Service = configuration.at("services.dms-submission-stub").loadOrThrow[Service]

  val internalAuthToken: String = configuration.at("internal-auth.token").loadOrThrow[String]

  private implicit val system: ActorSystem = ActorSystem()
  val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()
}
