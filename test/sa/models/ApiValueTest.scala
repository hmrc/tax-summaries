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

package sa.models

import common.models.{Amount, ApiValue, LiabilityKey}
import common.utils.BaseSpec
import common.utils.Generators.genLiabilityMap
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class ApiValueTest extends BaseSpec with ScalaCheckPropertyChecks {
  "Round trip map through Json" in
    forAll(genLiabilityMap) { map =>
      val OUT    = ApiValue.formatMap(LiabilityKey.allItems)(Json.format[Amount])
      val json   = Json.toJson(map)(OUT)
      val result = json.as[Map[LiabilityKey, Amount]](OUT)
      result mustBe map
    }
}
