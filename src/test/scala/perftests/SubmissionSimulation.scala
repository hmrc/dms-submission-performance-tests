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

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.HeaderNames
import io.gatling.http.Predef._
import org.slf4j.LoggerFactory
import perftests.Global._
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class SubmissionSimulation extends Simulation {

  private val logger = LoggerFactory.getLogger(classOf[SubmissionSimulation])

  private val submissionRequest =
    http("Submission request")
      .post(s"${dmsSubmission.baseUrl}/dms-submission/submit")
      .asMultipartForm
      .bodyPart(RawFileBodyPart("form", "files/test.pdf").fileName("form.pdf"))
      .formParam("callbackUrl", s"${dmsSubmissionStub.baseUrl}/dms-submission-stub/callback")
      .formParam("metadata.store", "true")
      .formParam("metadata.source", "api-tests")
      .formParam("metadata.timeOfReceipt", DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now))
      .formParam("metadata.formId", "formId")
      .formParam("metadata.customerId", "customerId")
      .formParam("metadata.submissionMark", "submissionMark")
      .formParam("metadata.casKey", "casKey")
      .formParam("metadata.classificationType", "classificationType")
      .formParam("metadata.businessArea", "businessArea")
      .header(HeaderNames.Authorization, internalAuthToken)
      .check(
        status.is(202),
      )

  private val submissionScenario: ScenarioBuilder =
    scenario("submission")
      .exec(submissionRequest)

  private val maxLoad: Int = (20 * (loadPercentage / 100.0)).toInt

  if (isLocal) {
    before {
      ensureLocalAuthToken()
    }
  }

  if (isSmokeTest) {
    setUp(
      submissionScenario.inject(atOnceUsers(1))
    ).protocols(http)
  } else {
    setUp(
      submissionScenario.inject(
        rampUsersPerSec(0).to(maxLoad).during(rampUpTime),
        constantUsersPerSec(maxLoad).during(constantRateTime),
        rampUsersPerSec(maxLoad).to(0).during(rampDownTime)
      ).protocols(http)
    )
  }

  private def ensureLocalAuthToken(): Unit = {
    logger.info("checking auth token")
    val tokenIsValid = Await.result(
      wsClient.url(s"${internalAuth.baseUrl}/test-only/token")
        .withHttpHeaders("AUTHORIZATION" -> internalAuthToken)
        .get(),
      5.seconds
    ).status == 200

    if (tokenIsValid) {
      logger.info("auth token is already valid")
    } else {
      logger.info("creating valid auth token")
      import play.api.libs.ws.JsonBodyWritables._
      val response = Await.result(
        wsClient.url(s"${internalAuth.baseUrl}/test-only/token")
          .post(
            Json.obj(
              "token" -> internalAuthToken,
              "principal" -> "test",
              "permissions" -> Seq(
                Json.obj(
                  "resourceType" -> "dms-submission",
                  "resourceLocation" -> "submit",
                  "actions" -> List("WRITE")
                )
              )
            )
          ),
        5.seconds
      )
      require(response.status == 201, "Unable to create auth token")
      logger.info("auth token created")
    }
  }
}
