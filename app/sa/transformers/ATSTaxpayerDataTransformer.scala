/*
 * Copyright 2026 HM Revenue & Customs
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

package sa.transformers

import play.api.libs.json.JsValue

case class ATSTaxpayerDataTransformer(rawJsonFromStub: JsValue) {

  def atsTaxpayerDataDTO: Option[Map[String, String]] = createTaxpayerDetailsBreakdown

  private def createTaxpayerDetailsBreakdown: Option[Map[String, String]] = {
    val taxpayerData = Map(
      "title"    -> getTaxpayerNameData("title"),
      "forename" -> getTaxpayerNameData("forename"),
      "surname"  -> getTaxpayerNameData("surname")
    ).collect { case (k, Some(v)) =>
      k -> v
    }

    if (taxpayerData.isEmpty) None else Some(taxpayerData)
  }

  private def getTaxpayerNameData(key: String): Option[String] =
    (rawJsonFromStub \ "name" \ key).asOpt[String]
}
