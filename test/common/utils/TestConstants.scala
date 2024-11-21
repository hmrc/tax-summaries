/*
 * Copyright 2023 HM Revenue & Customs
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

package common.utils

import uk.gov.hmrc.domain.{Generator, SaUtrGenerator}

import scala.util.Random

object TestConstants {

  // We only want one test nino and utr throughout, therefore assign a value in the object declaration
  lazy val testUtr: String            = new SaUtrGenerator().nextSaUtr.utr
  lazy val testNino: String           = new Generator().nextNino.nino
  lazy val testUar: String            = "V" + genRandNumString(4) + "H"
  lazy val testInvalidUtr: String     = genRandNumString(4)
  lazy val testKey: String            = genRandNumString(22)
  lazy val testOid: String            = genRandNumString(12)
  lazy val testNonMatchingUtr: String = new SaUtrGenerator().nextSaUtr.utr

  def genRandNumString(length: Int): String = Random.nextInt(9).toString * length

}
