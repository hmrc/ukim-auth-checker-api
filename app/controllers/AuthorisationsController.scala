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

package uk.gov.hmrc.ukimauthcheckerapi.controllers

import models.{AuthorisationRequest, DatedAuthorisationRequest, ErrorMessage}
import connectors.PdsAuthCheckerConnector
import play.api.mvc.{Action, ControllerComponents}
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import services.ConverterService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.auth.core._

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AuthorisationsController @Inject() (
    cc: ControllerComponents,
    val authConnector: AuthConnector,
    pdsAuthCheckerConnector: PdsAuthCheckerConnector,
    converterService: ConverterService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with AuthorisedFunctions {

  def authorisations: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      request.body.validate[AuthorisationRequest] match {
        case JsSuccess(aRequest, _) =>
          authorised() {
            val datedRequest =
              DatedAuthorisationRequest.createFromRequest(aRequest)

            pdsAuthCheckerConnector.check(datedRequest).map {
              case Right(pdsAuthCheckerResponse) =>
                Ok(
                  Json.toJson(
                    converterService.convert(
                      pdsAuthCheckerResponse,
                      date = LocalDate.parse(datedRequest.date)
                    )
                  )
                )
              case Left(validationErrorResponse) =>
                BadRequest(
                  Json.toJson(
                    validationErrorResponse
                  )
                )
            }
          } recover {
            case _: NoActiveSession =>
              Unauthorized(
                Json.toJson(
                  (ErrorMessage(
                    "MISSING_CREDENTIALS",
                    "Authentication information is not provided"
                  ))
                )
              )
            case _: AuthorisationException =>
              Forbidden(
                Json.toJson(
                  (ErrorMessage(
                    "FORBIDDEN",
                    "You are not allowed to access this resource"
                  ))
                )
              )
          }
        case JsError(_) =>
          Future.successful(
            BadRequest(
              Json.toJson(
                ErrorMessage("INVALID_PAYLOAD", "Valid Payload Required")
              )
            )
          )
      }

  }
}
