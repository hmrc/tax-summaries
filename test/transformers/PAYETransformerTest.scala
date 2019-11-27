/*
 * Copyright 2019 HM Revenue & Customs
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

package transformers

import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec
import PAYETransformer._
import scala.io.Source

class PAYETransformerTest extends UnitSpec with PAYETransformer {

  val nino = "AB654321B"
  val payeJson = Json.parse(Source.fromURL(getClass.getResource("/nino_2018.json")).mkString).as[JsObject]

  "The middle tier json transformer" should {
    "show the header" in {
      val expectedHeaderJson =
        """
          |{
          |  "taxYear" : 2018,
          |  "nino" : "AB654321B",
          |  "lnks" : [ {
          |    "rel" : "self",
          |    "href" : "https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/AB654321B/2018"
          |  } ]
          |}
        """.stripMargin

      middleTierJson(nino, 2018).omitEmpty should be(Json.parse(expectedHeaderJson))
    }

    "transform test attributes" in {
      val expectedTestAttributesJson =
        """
          |{
          |  "taxYear" : 2018,
          |  "nino" : "AB654321B",
          |  "lnks" : [ {
          |    "rel" : "self",
          |    "href" : "https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/AB654321B/2018"
          |  } ],
          |  "income_data" : {
          |    "payload" : {
          |      "test1" : {
          |        "amount" : 2.5,
          |        "currency" : "GBP"
          |      },
          |      "test2" : {
          |        "amount" : 5.5,
          |        "currency" : "GBP"
          |      }
          |    }
          |  }
          |}
          |
    """.stripMargin

      val jsonTransformer =
        appendAttribute(__ \ 'income_data \ 'payload, middleTierAttributeJson("test1", 2.50)) andThen
          appendAttribute(__ \ 'income_data \ 'payload, middleTierAttributeJson("test2", 5.50))

      val transformedJson = middleTierJson(nino, 2018).transform(jsonTransformer).asOpt.map(_.omitEmpty)
      transformedJson should be(Some(Json.parse(expectedTestAttributesJson)))
    }

    "transform 'Your income and taxes' section" in {
      val expectedIncomeDataJson =
        """
          |{
          |  "taxYear" : 2018,
          |  "nino" : "AB654321B",
          |  "lnks" : [ {
          |    "rel" : "self",
          |    "href" : "https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/AB654321B/2018"
          |  } ],
          |  "allowance_data" : {
          |    "payload" : {
          |      "total_tax_free_amount" : {
          |        "amount" : 25500,
          |        "currency" : "GBP"
          |      },
          |      "other_allowances_amount" : {
          |        "amount" : 6000,
          |        "currency" : "GBP"
          |      }
          |    }
          |  },
          |  "income_data" : {
          |    "payload" : {
          |      "income_from_employment" : {
          |        "amount" : 25000,
          |        "currency" : "GBP"
          |      },
          |      "state_pension" : {
          |        "amount" : 1000,
          |        "currency" : "GBP"
          |      },
          |      "other_pension_income" : {
          |        "amount" : 500,
          |        "currency" : "GBP"
          |      },
          |      "other_income" : {
          |        "amount" : 3000,
          |        "currency" : "GBP"
          |      },
          |      "total_income_before_tax" : {
          |        "amount" : 28000,
          |        "currency" : "GBP"
          |      },
          |      "taxable_state_benefits" : {
          |        "amount" : 200,
          |        "currency" : "GBP"
          |      }
          |    }
          |  }
          |}
        """.stripMargin

      val transformedJson = middleTierJson(nino, 2018).transformTotalIncome(payeJson)
      transformedJson should be(Json.parse(expectedIncomeDataJson))
    }
  }
}
