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

import models.Outcome.{Authorised, InvalidFormat, Unauthorised}
import models.{InvalidFormattedEoris, Outcome, ValidateEorisResponse}
import play.api.libs.json.Json
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.EoriValidatorService

import java.time.{Clock, LocalDate, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class EoriController @Inject()(cc: ControllerComponents, eoriValidatorService: EoriValidatorService, clock: Clock)(implicit ec: ExecutionContext)
  extends BackendController(cc) {

  def validateEoris(eoris: Vector[String], inputDate: Option[LocalDate] = None): Action[AnyContent]  = Action.async  { _ =>
    if (eoris.length > 3000) {
      Future.successful(BadRequest("Number of strings exceeded; submit 3000 or less"))
    } else {
      val authorisedOn: LocalDate = inputDate.getOrElse(clock.instant().atOffset(ZoneOffset.UTC).toLocalDate)

      val futureOutcomes: Future[Vector[Outcome]] = Future.traverse(eoris)(eori => eoriValidatorService.validateEoriString(eori, authorisedOn))

      futureOutcomes.map { outcomes =>
        val (invalidOutcomes: Vector[InvalidFormat], validOutcomes: Vector[Outcome.Success]) = outcomes.partitionMap {
          case authed @ Authorised(_) => Right(authed)
          case unauthed @ Unauthorised(_) => Right(unauthed)
          case invalidFormatting @ InvalidFormat(_) => Left(invalidFormatting)
        }

        if (invalidOutcomes.isEmpty) {
          import models.ValidateEorisResponse.ValidateEorisResponseWrites
          Ok(Json.toJson(ValidateEorisResponse(authorisedOn, validOutcomes)))
        } else {
          import models.InvalidFormattedEoris.InvalidFormattedEorisWrites
          BadRequest(Json.toJson(InvalidFormattedEoris(invalidOutcomes.map(_.value))))
        }
      }
    }
  }
}

