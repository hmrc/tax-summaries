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

package models.paye

import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import utils.{BaseSpec, Generators}

class PayeAtsMiddleTierTest extends BaseSpec with ScalaCheckPropertyChecks {

  "PayeAtsMiddleTier should round trip through Json " in {
    forAll(Generators.genPayeAsMiddleTier) { data =>
      val json = Json.toJson(data)
      val obj = json.as[PayeAtsMiddleTier]

      obj shouldBe data
    }
  }
}
