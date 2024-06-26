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

import models.{AuthorisationRequest, ErrorMessage}
import connectors.PdsAuthCheckerConnector
import play.api.mvc.{Action, ControllerComponents}
import play.api.libs.json.{Json, OFormat}
import services.ConverterService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.auth.core._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AuthorisationsController @Inject()(
  cc: ControllerComponents,
  val authConnector: AuthConnector,
  pdsAuthCheckerConnector: PdsAuthCheckerConnector,
  converterService: ConverterService
) (implicit ec: ExecutionContext) extends BackendController(cc) with AuthorisedFunctions  {

  def authorisations: Action[AuthorisationRequest] = Action.async(parse.json[AuthorisationRequest]) { implicit request =>
    authorised() {
      pdsAuthCheckerConnector.check(request.body).map {
        res =>
          Ok(Json.toJson(converterService.convert(res)))
      }
    } recover {
      case ex: NoActiveSession =>
        Unauthorized(Json.toJson((ErrorMessage("MISSING_CREDENTIALS", "Authentication information is not provided"))))
      case ex: AuthorisationException =>
        Forbidden(Json.toJson((ErrorMessage("FORBIDDEN", "You are not allowed to access this resource"))))
    }
  }
}