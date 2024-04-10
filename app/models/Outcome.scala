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

import play.api.libs.json.Writes

sealed trait Outcome

object Outcome {

  case class InvalidFormat(value: String) extends Outcome

  sealed trait Success extends Outcome {
    val value: Eori
  }

  object Success {
    implicit val formats: Writes[Outcome.Success] = {
      case ua: Unauthorised => implicitly[Writes[Eori]].writes(ua.value)
      case a: Authorised => implicitly[Writes[Eori]].writes(a.value)
    }
  }

  // not specced out yet
  case class Unauthorised(value: Eori) extends Success

  case class Authorised(value: Eori) extends Success
}
