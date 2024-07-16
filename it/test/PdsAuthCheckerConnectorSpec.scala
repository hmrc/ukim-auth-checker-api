/*
 * Copyright 2024 HM Revenue & Customs
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

import base.TestCommonGenerators
import config.UKIMSServicesConfig
import connectors.PdsAuthCheckerConnectorImpl
import models.{Eori, PdsAuthCheckerResponse, PdsAuthCheckerResult}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.ukimauthcheckerapi.config.AppConfig
import com.github.tomakehurst.wiremock.client.WireMock._

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PdsAuthCheckerConnectorSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with HttpClientV2Support
    with TestCommonGenerators
    with IntegrationPatience
    with WireMockSupport {

  private val configuration = Configuration(
    "appName" -> "pds-auth-checker-api",
    "microservice.services.pds-auth-checker-api.host" -> wireMockHost,
    "microservice.services.pds-auth-checker-api.port" -> wireMockPort
  )

  private val mockUKIMSServicesConfig = new UKIMSServicesConfig(configuration)

  private val wiremockServerConfig = new AppConfig(
    configuration,
    mockUKIMSServicesConfig
  )

  private val pdsPath = "/authorisations"

  private val pdsConnector =
    new PdsAuthCheckerConnectorImpl(httpClientV2, wiremockServerConfig)

  implicit val hc = HeaderCarrier()

  "PdsAuthCheckerConnector" when {
    "a request is made" should {
      "return a successful response with body for a valid response from PdsAuthCheckerApi" in {
        givenPdsReturns(
          200,
          pdsPath,
          s"""{
             |  "processingDate": "2021-01-01",
             |  "authType": "UKIM",
             |  "results": [
             |    {
             |      "eori": "GB120000000999",
             |      "valid": false,
             |      "code": 1
             |    },
             |    {
             |      "eori": "GB120001000919",
             |      "valid": true,
             |      "code": 0
             |    }
             |  ]
             |}""".stripMargin
        )

        val response = pdsConnector
          .check(authorisationRequestGen.sample.get)
          .futureValue

        response shouldBe Right(
          PdsAuthCheckerResponse(
            LocalDate.of(2021, 1, 1),
            "UKIM",
            Seq(
              PdsAuthCheckerResult(Eori("GB120000000999"), valid = false, 1),
              PdsAuthCheckerResult(Eori("GB120001000919"), valid = true, 0)
            )
          )
        )
      }
      "return a failure with an exception if malformed body returned from PdsAuthCheckerApi" in {
        givenPdsReturns(
          200,
          pdsPath,
          s"""{
             |  "processingDate": "2021-01-01",
             |  "results": [
             |    {
             |      "eori": "GB120000000999",
             |      "valid": "apple",
             |      "code": 1
             |    },
             |    {
             |      "eori": "GB120001000919",
             |      "valid": true,
             |      "code": "cars"
             |    }
             |  ]
             |}""".stripMargin
        )

        val response = pdsConnector
          .check(authorisationRequestGen.sample.get)

        whenReady(response.failed) { r =>
          r shouldBe a[Exception]
        }

      }
    }
  }
  private def givenPdsReturns(status: Int, url: String, body: String): Unit =
    wireMockServer.stubFor(
      post(urlEqualTo(url))
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )
}
