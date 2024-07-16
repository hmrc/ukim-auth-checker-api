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

package models.errors

import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed abstract class ValidationError

case class EoriValidationError(rawEori: String, errorMessage: String)
  extends ValidationError

case class DateValidationError(rawDateIsoString: String, errorMessage: String)
  extends ValidationError

object ValidationError {
  implicit val writes: Writes[ValidationError] = Writes[ValidationError] {
    case e: EoriValidationError => Json.toJson(e)(EoriValidationError.writes)
    case d: DateValidationError => Json.toJson(d)(DateValidationError.writes)
  }

  implicit val reads: Reads[ValidationError] = Reads[ValidationError] {
    case json: JsObject =>
      (json \ "eori").validate[String].map(_ => json.as[EoriValidationError]) orElse
        (json \ "date").validate[String].map(_ => json.as[DateValidationError]) match {
        case JsSuccess(value, _) => JsSuccess(value)
        case JsError(_) => JsError("Unknown ValidationError type")
      }
    case _ => JsError("ValidationError should be a JSON object")
  }
}

object EoriValidationError {
  implicit val writes: Writes[EoriValidationError] = (
    (__ \ "eori").write[String] and
      (__ \ "validationError").write[String]
    )(eve => (eve.rawEori, eve.errorMessage))

  implicit val reads: Reads[EoriValidationError] = (
    (__ \ "eori").read[String] and
      (__ \ "validationError").read[String]
    )(EoriValidationError.apply _)
}

object DateValidationError {
  implicit val writes: Writes[DateValidationError] = (
    (__ \ "date").write[String] and
      (__ \ "validationError").write[String]
    )(dve => (dve.rawDateIsoString, dve.errorMessage))

  implicit val reads: Reads[DateValidationError] = (
    (__ \ "date").read[String] and
      (__ \ "validationError").read[String]
    )(DateValidationError.apply _)
}
