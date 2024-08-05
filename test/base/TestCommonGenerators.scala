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

package base

import models.{
  AuthorisationRequest,
  DatedAuthorisationRequest,
  Eori,
  PdsAuthCheckerResponse,
  PdsAuthCheckerResult
}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

import java.time.{LocalDate, LocalDateTime}
import java.time.temporal.ChronoUnit
trait TestCommonGenerators {
  lazy val eoriGen: Gen[Eori] = Gen.alphaNumStr.map(Eori(_))
  lazy val eorisGen: Gen[Seq[Eori]] = Gen.listOfN(3000, eoriGen)

  lazy val authorisationRequestGen: Gen[AuthorisationRequest] = for {
    eoris <- eorisGen
    now = LocalDate.now()
    date <- Gen.option(
      Gen.choose(now.minus(1, ChronoUnit.YEARS), now.plus(3, ChronoUnit.MONTHS))
    )
  } yield AuthorisationRequest(eoris, Some(date.toString))

  lazy val datedAuthorisationRequest: Gen[DatedAuthorisationRequest] = for {
    eoris <- eorisGen
    now = LocalDate.now()
    date <- Gen.option(
      Gen.choose(now.minus(1, ChronoUnit.YEARS), now.plus(3, ChronoUnit.MONTHS))
    )
  } yield DatedAuthorisationRequest(eoris, date.toString)

  implicit lazy val arbitraryAuthorisationRequest
      : Arbitrary[AuthorisationRequest] = Arbitrary(authorisationRequestGen)

  implicit lazy val arbitraryPdsAuthCheckerResult
      : Arbitrary[PdsAuthCheckerResult] = Arbitrary {
    for {
      eori <- eoriGen
      code <- Gen.oneOf(0, 1, 2)
      valid = code == 0
    } yield PdsAuthCheckerResult(eori, valid, code)
  }

  implicit lazy val arbitraryPdsAuthCheckerResponse
      : Arbitrary[PdsAuthCheckerResponse] = Arbitrary {
    for {
      results <- arbitrary[Seq[PdsAuthCheckerResult]]
      date = LocalDateTime.of(2024, 1, 1, 0, 0)
      authType = "UKIM"
    } yield PdsAuthCheckerResponse(date, authType, results)
  }
}
