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

import play.api.libs.json.{JsError, JsString, JsSuccess, Json, Reads, Writes}

case class ValidationErrorResponse(
                                    code: AuthorisedBadRequestCode,
                                    message: String,
                                    validationErrors: Seq[ValidationError]
                                  )

sealed trait AuthorisedBadRequestCode

object AuthorisedBadRequestCode {
  private case object InvalidFormat extends AuthorisedBadRequestCode

  implicit val writes: Writes[AuthorisedBadRequestCode] =
    implicitly[Writes[String]].contramap { case InvalidFormat =>
      "INVALID_FORMAT"
    }

  implicit val reads: Reads[AuthorisedBadRequestCode] = Reads {
    case JsString("INVALID_FORMAT") => JsSuccess(InvalidFormat)
    case _ => JsError("Unknown authorised bad request code")
  }
}

object ValidationErrorResponse {
  implicit val reads: Reads[ValidationErrorResponse] = Json.reads[ValidationErrorResponse]
  implicit val writes: Writes[ValidationErrorResponse] = Json.writes[ValidationErrorResponse]
}

