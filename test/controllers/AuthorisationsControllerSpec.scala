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

package controllers

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
import models.AuthorisationRequest
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.ukimauthcheckerapi.controllers.AuthorisationsController

import java.time.LocalDate
import scala.concurrent.Future

class AuthorisationsControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with TestCommonGenerators with ScalaCheckPropertyChecks {
  val controller = new AuthorisationsController(stubControllerComponents())

  "AuthorisationsController" should {

    "return 200 OK with empty body for valid JSON request with date populated" in {
      forAll { authRequest: AuthorisationRequest =>
        val authRequestWithDate = authRequest.copy(date = Some(LocalDate.now()))
        val request = FakeRequest().withBody(authRequestWithDate)
        val result: Future[Result] = controller.authorisations(request)
        status(result) mustBe OK
        contentAsString(result) mustBe empty
      }
    }

    "return 200 OK when no date is provided with empty body for valid JSON request" in {
      forAll { authRequest: AuthorisationRequest =>
        val authRequestWithoutDate = authRequest.copy(date = None)
        val request = FakeRequest().withBody(authRequestWithoutDate)
        val result: Future[Result] = controller.authorisations(request)

        status(result) mustBe OK
        contentAsString(result) mustBe empty
      }
    }

  }
}

