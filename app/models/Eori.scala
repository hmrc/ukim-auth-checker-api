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

import play.api.libs.json.{Format, Json, Writes}

import scala.util.matching.Regex

case class Eori(value: String) extends AnyVal

object Eori {
  val Regex: Regex = "^(GB|XI)\\d{12}$".r
  implicit lazy val format: Format[Eori] = Json.valueFormat[Eori]

  implicit val writes: Writes[Eori] = implicitly[Writes[String]].contramap(_.value)
}