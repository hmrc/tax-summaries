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

package sa.transformers

import common.utils.BaseSpec
import play.api.libs.json.Json
import sa.models.AtsMiddleTierTaxpayerData
import sa.utils.AtsJsonDataUpdate

import scala.io.Source
import scala.util.Using

class ATSTaxpayerDataTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  "The taxpayers name" must {

    "be parsed form the income values for utr" in {

      val sampleJson = Using(Source.fromURL(getClass.getResource("/common/taxpayer/sa_taxpayer-valid.json"))) {
        source =>
          source.mkString
      }.getOrElse(throw new RuntimeException("Unable to read the JSON file"))

      val parsedJson = Json.parse(sampleJson)

      val returnValue: AtsMiddleTierTaxpayerData = ATSTaxpayerDataTransformer(parsedJson).atsTaxpayerDataDTO

      val parsedPayload = returnValue.taxpayer_name.get
      val testPayload   =
        Map("title" -> "Miss", "forename" -> "Jane", "surname" -> "Fisher")
      testPayload mustEqual parsedPayload
    }
  }
}
