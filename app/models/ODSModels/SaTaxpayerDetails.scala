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

package models.ODSModels

import models.AtsMiddleTierTaxpayerData
import play.api.libs.json.{Json, OFormat}

case class SaTaxpayerDetails(name: Name, address: Address, contact: Contact) {

  def atsTaxpayerDataDTO: AtsMiddleTierTaxpayerData = AtsMiddleTierTaxpayerData(Some(getNameAsMap), None)

  private def getNameAsMap: Map[String, String] =
    Map("title" -> name.title, "forename" -> name.forename, "surname" -> name.surname)
}

object SaTaxpayerDetails {
  implicit val formats: OFormat[SaTaxpayerDetails] = Json.format[SaTaxpayerDetails]
}

case class Name(
  title: String,
  forename: String,
  secondForename: Option[String],
  surname: String,
  honours: Option[String])

object Name {
  implicit val formats: OFormat[Name] = Json.format[Name]
}

case class Address(
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  addressLine4: Option[String],
  addressLine5: Option[String],
  postcode: String,
  foreignCountry: Option[String],
  returnedLetter: Boolean,
  additionalDeliveryInformation: Option[String])

object Address {
  implicit val formats: OFormat[Address] = Json.format[Address]
}

case class Contact(telephone: Telephone, email: Email)

object Contact {
  implicit val formats: OFormat[Contact] = Json.format[Contact]
}

case class Telephone(daytime: String, fax: Option[String], evening: String, mobile: String)

object Telephone {
  implicit val formats: OFormat[Telephone] = Json.format[Telephone]
}

case class Email(primary: String)

object Email {
  implicit val formats: OFormat[Email] = Json.format[Email]
}
