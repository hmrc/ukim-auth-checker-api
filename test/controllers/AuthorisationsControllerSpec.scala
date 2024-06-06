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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.ukimauthcheckerapi.controllers.AuthorisationsController
import models.{AuthorisationRequest, Eori}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthorisationsControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with Results {

  trait Setup {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    val controllerComponents: ControllerComponents = Helpers.stubControllerComponents()
    val controller = new AuthorisationsController(controllerComponents, mockAuthConnector)
    
  }

  "AuthorisationsController" should {

    "return OK when user is authorised" in new Setup {
      val enrolments = Enrolments(Set(Enrolment("enrolmentKey")))
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.successful(enrolments))

      val request = FakeRequest().withBody(AuthorisationRequest(Seq(Eori("test-eori")), None))

      val result = controller.authorisations()(request)

      status(result) shouldBe OK
    }

    "return Unauthorized when there is no active session" in new Setup {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new NoActiveSession("No active session") {}))

      val request = FakeRequest().withBody(AuthorisationRequest(Seq(Eori("test-eori")), None))

      val result = controller.authorisations()(request)

      status(result) shouldBe UNAUTHORIZED
      contentAsString(result) shouldBe "No active session"
    }

    "return Forbidden when user is not authorised" in new Setup {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new AuthorisationException("Forbidden") {}))

      val request = FakeRequest().withBody(AuthorisationRequest(Seq(Eori("test-eori")), None))

      val result = controller.authorisations()(request)

      status(result) shouldBe FORBIDDEN
      contentAsString(result) shouldBe "You are not authorized to access this resource"
    }
  }
}
