/*
 * Copyright 2020 HM Revenue & Customs
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

import models.ODSModels.{Address, Contact, Email, Name, SaTaxpayerDetails, Telephone}
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SaTaxpayerDetailsSpec extends UnitSpec {

  val title = "Mr"
  val forename = "John"
  val surname = "Doe"

  val name = Name(title, forename, None, surname, None)
  val address = Address("123 Test Street", "Test Road", "Test Town", None, None, "TE5 1NG", None, false, None)
  val contact = Contact(
    Telephone("0123456789", None, "9876543210", "0712345678"),
    Email("test@test.com")
  )

  val modelUnderTest = SaTaxpayerDetails(name, address, contact)

  "SaTaxpayerDetails" when {

    "atsTaxpayerDataDTO is called" should {

      "return a  with the correct data" in {

        val result = modelUnderTest.atsTaxpayerDataDTO
        result shouldBe a[AtsMiddleTierTaxpayerData]
        result.taxpayer_name shouldBe Some(
          Map(
            "title"    -> title,
            "forename" -> forename,
            "surname"  -> surname
          )
        )
      }
    }

    "asked to serialise and de-serialise" should {

      "maintain the correct data" in {
        val json = Json.toJson(modelUnderTest)
        val parsed = json.as[SaTaxpayerDetails]

        parsed.name shouldBe modelUnderTest.name
        parsed.address shouldBe modelUnderTest.address
        parsed.contact shouldBe modelUnderTest.contact
      }
    }
  }
}
