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

import base.TestCommonGenerators
import models.{PdsAuthCheckerResponse, UKIMAuthCheckerResponse, UKIMAuthCheckerResult}
import org.scalacheck.Arbitrary
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class ConverterServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with TestCommonGenerators{

  val sut = new ConverterService()



  "convert(PdsAuthCheckerResponse)" should {
    "return a UKIMAuthCheckerResponse" in {
        val input = Arbitrary.arbitrary[PdsAuthCheckerResponse].sample.get
        val expectedResult = UKIMAuthCheckerResponse(input.processingDate, input.results.map {
          r => UKIMAuthCheckerResult(r.eori, r.valid)
        })
        sut.convert(input) shouldBe expectedResult
      }
    }
}
