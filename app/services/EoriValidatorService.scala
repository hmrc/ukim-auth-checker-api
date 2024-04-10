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

package services


import models.Outcome.{Authorised, InvalidFormat}
import models.{Eori, Outcome}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
class EoriValidatorService(implicit ec: ExecutionContext) {
  def validateEoriString(preValidationEoriString: String, onDate: LocalDate): Future[Outcome] = Future {
    preValidationEoriString match {
      case Eori.Regex(_) =>
        // TODO - auth lookup
        Authorised(Eori(preValidationEoriString))
      case _ => InvalidFormat(preValidationEoriString)
    }
  }
}
