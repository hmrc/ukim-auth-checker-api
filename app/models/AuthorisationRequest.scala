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

package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class AuthorisationRequest(eoris: List[String], date: Option[LocalDate])

object AuthorisationRequest {

  implicit val reads: Reads[AuthorisationRequest] = (
    (__ \ "eoris").read[List[String]] and
      (__ \ "date").readNullable[String].map(_.map(LocalDate.parse))
    )(AuthorisationRequest.apply _)

  implicit val writes: Writes[AuthorisationRequest] = (
    (__ \ "eoris").write[List[String]] and
      (__ \ "date").writeNullable[String].contramap[Option[LocalDate]] {
        case Some(date) => Some(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        case None => None
      }
    )(unlift(AuthorisationRequest.unapply))
}