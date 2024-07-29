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

import connectors.PdsAuthCheckerConnector
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json.Json
import org.mockito.ArgumentMatchers.any
import play.api.mvc.{ControllerComponents, Result, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.ukimauthcheckerapi.controllers.AuthorisationsController
import models.{AuthorisationRequest, AuthorisedBadRequestCode, Eori, EoriValidationError, ErrorMessage, PdsAuthCheckerResponse, PdsAuthCheckerResult, UKIMAuthCheckerResponse, UKIMAuthCheckerResult, ValidationErrorResponse}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer

import services.ConverterService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.authorise.Predicate

import java.time.LocalDate

class AuthorisationsControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with Results {

  implicit val sys = ActorSystem("Test")
  implicit val mat = Materializer.matFromSystem

  trait Setup {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    val mockPdsConnector: PdsAuthCheckerConnector =
      mock[PdsAuthCheckerConnector]
    val mockConverterService: ConverterService = mock[ConverterService]
    val controllerComponents: ControllerComponents =
      Helpers.stubControllerComponents()
    val controller = new AuthorisationsController(
      controllerComponents,
      mockAuthConnector,
      mockPdsConnector,
      mockConverterService
    )

  }

  val response =
    PdsAuthCheckerResponse(
      LocalDate.of(2024, 1, 1),
      "UKIM",
      Seq(
        PdsAuthCheckerResult(Eori("GB123456123456"), true, 0),
        PdsAuthCheckerResult(Eori("XI123123123123"), false, 2)
      )
    )

  val converted =
    UKIMAuthCheckerResponse(
      LocalDate.of(2024, 1, 1),
      Seq(
        UKIMAuthCheckerResult(Eori("GB123456123456"), true),
        UKIMAuthCheckerResult(Eori("XI123123123123"), false)
      )
    )

  "AuthorisationsController" should {

    "return OK when authorised and response received from PdsAuthChecker" in new Setup {
      when(
        mockAuthConnector
          .authorise(any[Predicate](), any[Retrieval[Unit]]())(any(), any())
      )
        .thenReturn(Future.successful(()))
      when(mockPdsConnector.check(any())(any(), any()))
        .thenReturn(Future.successful(Right(response)))
      when(mockConverterService.convert(any[PdsAuthCheckerResponse]))
        .thenReturn(converted)

      val request = FakeRequest().withBody(Json.toJson(AuthorisationRequest(Seq(Eori("test-eori")), None)))

      val result: Future[Result] = controller.authorisations()(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(converted)
    }

    "return Unauthorized when there is no active session" in new Setup {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new NoActiveSession("No active session") {}))

      val request = FakeRequest().withBody(Json.toJson(AuthorisationRequest(Seq(Eori("test-eori")), None)))

      val result = controller.authorisations()(request)

      status(result) shouldBe UNAUTHORIZED
      contentAsString(result) shouldBe Json
        .toJson(
          ErrorMessage(
            "MISSING_CREDENTIALS",
            "Authentication information is not provided"
          )
        )
        .toString
    }

    "return Forbidden when user is not authorised" in new Setup {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new AuthorisationException("Forbidden") {}))

      val request = FakeRequest().withBody(Json.toJson(AuthorisationRequest(Seq(Eori("test-eori")), None)))

      val result = controller.authorisations()(request)

      status(result) shouldBe FORBIDDEN
      contentAsJson(result) shouldBe Json.toJson(ErrorMessage("FORBIDDEN", "You are not allowed to access this resource"))
    }

    "return BadRequest when empty body provided" in new Setup {
      when(mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(any(), any()))
        .thenReturn(Future.successful(()))

      val request = FakeRequest().withBody(Json.toJson(""))

      val result = controller.authorisations()(request)

      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe Json.toJson(ErrorMessage("INVALID_PAYLOAD","Valid Payload Required"))
    }

    "return BadRequest when unsupported json provided" in new Setup {
      when(mockAuthConnector.authorise(any[Predicate](), any[Retrieval[Unit]]())(any(), any()))
        .thenReturn(Future.successful(()))

      val request = FakeRequest().withBody(Json.obj("bees" -> 5))
      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe Json.toJson(ErrorMessage("INVALID_PAYLOAD","Valid Payload Required"))
    }

    "return Bad Request when incorrectly formatted request is submitted (backend)" in new Setup {
      val errorResponse =
        ValidationErrorResponse(
          AuthorisedBadRequestCode.InvalidFormat,
          "Input format for request data",
          Seq(
            EoriValidationError(
              "GB12000000000111",
              "Invalid Format: Too many digits"
            )
          )
        )
      when(
        mockAuthConnector
          .authorise(any[Predicate](), any[Retrieval[Unit]]())(any(), any())
      )
        .thenReturn(Future.successful(()))
      when(mockPdsConnector.check(any())(any(), any()))
        .thenReturn(Future.successful(Left(errorResponse)))
      val request = FakeRequest().withBody(
        Json.toJson(AuthorisationRequest(Seq(Eori("test-eori")), None))
      )

      val result = controller.authorisations()(request)
      status(result) shouldBe BAD_REQUEST
      contentAsJson(result) shouldBe
        Json
          .obj(
            "code" -> "INVALID_FORMAT",
            "message" -> "Input format for request data",
            "validationErrors" -> Json.arr(
              Json.obj(
                "eori" -> "GB12000000000111",
                "validationError" -> "Invalid Format: Too many digits"
              )
            )
          )
    }
  }
}
