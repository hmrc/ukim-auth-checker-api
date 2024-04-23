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

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import uk.gov.hmrc.ukimauthcheckerapi.controllers.AuthorisationsController

import scala.concurrent.Future

class AuthorisationsControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  val controller = new AuthorisationsController(stubControllerComponents())

  "AuthorisationsController" should {

    "return 200 OK with empty body for valid JSON request" in {
      val requestJson = Json.obj(
        "eoris" -> Json.arr("EORIStr1", "EORIStr2", "EORIStr3"),
        "date" -> "2024-04-23"
      )

      val request = FakeRequest(POST, "/authorisations")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(requestJson)

      val result: Future[Result] = controller.authorisations.apply(request)

      status(result) mustBe OK
      contentAsString(result) mustBe empty
    }

    "return 400 Bad Request for invalid JSON request" in {
      val requestJson = Json.obj(
        "invalid_field" -> "value"
      )

      val request = FakeRequest(POST, "/authorisations")
        .withHeaders("Content-Type" -> "application/json")
        .withBody(requestJson)

      val result: Future[Result] = controller.authorisations.apply(request)

      status(result) mustBe BAD_REQUEST
      contentType(result) mustBe Some("application/json")
    }
  }
}

