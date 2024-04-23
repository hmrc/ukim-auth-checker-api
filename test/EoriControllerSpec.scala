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

import controllers.EoriController
import controllers.actions.ValidateNumberOfStrings
import models.ErrorState
import services.EoriValidatorService
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.{Clock, Instant, ZoneOffset}

class EoriControllerSpec extends AnyWordSpec with Matchers with ScalaFutures {
  val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)

  private val validatorService = new EoriValidatorService()
  private val validateNumberOfStrings = new ValidateNumberOfStrings()

  private val controller = new EoriController(stubControllerComponents(), validatorService, fixedClock, validateNumberOfStrings)

  private def requestWithEoris(eoris: Vector[String]) = FakeRequest(GET, s"?eoris=${eoris.mkString(",")}")

  "validateEoris" when {
    "erroring" should {
      "return 400 when any EORI is in an invalid format" in {
        val eoris = Vector("GB212345678901", "XI98765432098")
        val response = controller.validateEoris()(requestWithEoris(eoris))
        response.futureValue.header.status shouldBe Status.BAD_REQUEST

        val body = contentAsString(response)

        body shouldBe ErrorState.PartialInvalidFormat.errorMessage
      }
      "return 400 when all EORIs are in an invalid format" in {
        val eoris = Vector("GB21234567891", "XI98765432098")
        val response = controller.validateEoris()(requestWithEoris(eoris))
        response.futureValue.header.status shouldBe Status.BAD_REQUEST

        val body = contentAsString(response)

        body shouldBe ErrorState.InvalidFormat.errorMessage
      }
      "return 400 if over 3000 Strings are provided in request" in {
        val eoris = Vector.fill(3001)("XI98765432098")
        val response = controller.validateEoris()(requestWithEoris(eoris))
        response.futureValue.header.status shouldBe Status.BAD_REQUEST

        val body = contentAsString(response)

        body shouldBe ErrorState.NumberOfStringsExceeded.errorMessage
      }
    }

    "passing validation" should {
      "return a response containing all valid EORIs with current date when unspecified" in {
        val eoris = Vector("GB112345678901", "XI987654320982", "GB124567810122")
        val response = controller.validateEoris()(requestWithEoris(eoris))

        response.futureValue.header.status shouldBe Status.OK

        val body = contentAsString(response)


        body shouldBe Json.obj(
          "date" -> fixedClock.instant().atZone(ZoneOffset.UTC).toLocalDate,
          "eoris" -> eoris
        ).toString()
      }

      "return a response containing all valid EORIs with the given date" in {
        val eoris = Vector("GB112345678901", "XI987654320982", "GB124567810122")
        //TODO - What would we do if a user requested a date in the future? Would that be a possible edge case, or reject the request?
        val requestedDate = fixedClock.instant().atZone(ZoneOffset.UTC).toLocalDate.minusWeeks(2)

        val response = controller.validateEoris(Some(requestedDate))(requestWithEoris(eoris))
        response.futureValue.header.status shouldBe Status.OK

        val body = contentAsString(response)


        body shouldBe Json.obj(
          "date" -> requestedDate,
          "eoris" -> eoris
        ).toString()
      }
    }
  }
}