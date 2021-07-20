/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.domain.Nino

import scala.util.{Failure, Success, Try}

class NinoHelperSpec extends BaseSpec with ScalaCheckPropertyChecks {

  lazy val sut = app.injector.instanceOf[NinoHelper]

  val nino = TestConstants.testNino

  def isInvalidFormat(str: String): Boolean = Try(Nino(str)) match {
    case Failure(_) => true
    case Success(_) => false
  }

  "NinoRegexHelper.findNinoIn" should {

    "return a nino" when {

      "a valid nino is given" in {
        sut.findNinoIn(nino) shouldBe Some(nino)
      }
    }

    "return None" when {

      "given string is not a valid nino" in {

        forAll { fakeNino: String =>
          whenever(isInvalidFormat(fakeNino)) {
            sut.findNinoIn(fakeNino) shouldBe None
          }
        }
      }
    }
  }
}
