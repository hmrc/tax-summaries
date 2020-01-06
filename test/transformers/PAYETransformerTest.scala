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

package transformers

import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json._
import transformers.PAYETransformer._
import uk.gov.hmrc.play.test.UnitSpec

import scala.io.Source

class PAYETransformerTest extends UnitSpec with PAYETransformer with GuiceOneAppPerTest {

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
        appendAttribute(__ \ 'income_data \ 'payload, middleTierAmountJson("test1", 2.50)) andThen
          appendAttribute(__ \ 'income_data \ 'payload, middleTierAmountJson("test2", 5.50))

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
          |      "benefits_from_employment" : {
          |        "amount" : 200,
          |        "currency" : "GBP"
          |      },
          |      "taxable_state_benefits": {
          |        "amount": 500,
          |        "currency": "GBP"
          |      }
          |    }
          |  }
          |}
          |
        """.stripMargin

      val transformedJson = middleTierJson(nino, 2018).transformTotalIncome(payeJson).omitEmpty
      transformedJson should be(Json.parse(expectedIncomeDataJson))
    }

    "transform 'Summmary' section" in {
      val expectedSummaryDataJson =
        """{
          |  "taxYear" : 2018,
          |  "nino" : "AB654321B",
          |  "lnks" : [ {
          |    "rel" : "self",
          |    "href" : "https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/AB654321B/2018"
          |  } ],
          |  "summary_data" : {
          |    "payload" : {
          |      "total_income_before_tax" : {
          |        "amount" : 28000,
          |        "currency" : "GBP"
          |      },
          |      "total_tax_free_amount" : {
          |        "amount" : 25500,
          |        "currency" : "GBP"
          |      },
          |      "total_income_tax_and_nics" : {
          |        "amount" : 4200,
          |        "currency" : "GBP"
          |      },
          |      "income_after_tax_and_nics" : {
          |        "amount" : 5000,
          |        "currency" : "GBP"
          |      },
          |      "total_income_tax" : {
          |        "amount" : 4000,
          |        "currency" : "GBP"
          |      },
          |      "employee_nic_amount" : {
          |        "amount" : 200,
          |        "currency" : "GBP"
          |      },
          |      "employer_nic_amount" : {
          |        "amount" : 100,
          |        "currency" : "GBP"
          |      },
          |      "nics_and_tax_rate" : {
          |        "amount" : 25,
          |        "currency" : "GBP"
          |      }
          |    }
          |  }
          |}
        """.stripMargin
      val transformedJson = middleTierJson(nino, 2018).transformSummary(payeJson).omitEmpty
      transformedJson should be(Json.parse(expectedSummaryDataJson))
    }

    "transform 'Allowances' section" in {
      val expectedAllowancesJson =
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
          |      "personal_tax_free_amount" : {
          |        "amount" : 12500,
          |        "currency" : "GBP"
          |      },
          |      "marriage_allowance_transferred_amount" : {
          |        "amount" : 1250,
          |        "currency" : "GBP"
          |      },
          |      "other_allowances_amount" : {
          |        "amount" : 6000,
          |        "currency" : "GBP"
          |      },
          |      "you_pay_tax_on" : {
          |        "amount" : 15000,
          |        "currency" : "GBP"
          |      },
          |     "total_tax_free_amount" : {
          |        "amount" : 25500,
          |        "currency" : "GBP"
          |      }
          |    }
          |  }
          |}
        """.stripMargin
      val transformedJson = middleTierJson(nino, 2018).transformAllowances(payeJson).omitEmpty
      transformedJson should be(Json.parse(expectedAllowancesJson))
    }

    "transform 'income_tax' section" in {
      val expectedIncomeTaxJson =
        """{
          |  "nino" : "AB654321B",
          |  "lnks" : [ {
          |    "rel" : "self",
          |    "href" : "https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/AB654321B/2018"
          |  } ],
          |  "taxYear" : 2018,
          |  "income_tax" : {
          |    "payload" : {
          |      "higher_rate_income_tax_amount" : {
          |        "amount" : 2000,
          |        "currency" : "GBP"
          |      },
          |      "ordinary_rate_amount" : {
          |        "amount" : 200,
          |        "currency" : "GBP"
          |      },
          |      "basic_rate_income_tax" : {
          |        "amount" : 10000,
          |        "currency" : "GBP"
          |      },
          |      "total_income_tax" : {
          |        "amount" : 4000,
          |        "currency" : "GBP"
          |      },
          |      "upper_rate" : {
          |        "amount" : 2000,
          |        "currency" : "GBP"
          |      },
          |      "married_couples_allowance_adjustment" : {
          |        "amount" : 500,
          |        "currency" : "GBP"
          |      },
          |      "less_tax_adjustment_previous_year" : {
          |        "amount" : 200,
          |        "currency" : "GBP"
          |      },
          |      "higher_rate_income_tax" : {
          |        "amount" : 10000,
          |        "currency" : "GBP"
          |      },
          |      "ordinary_rate" : {
          |        "amount" : 2000,
          |        "currency" : "GBP"
          |      },
          |      "marriage_allowance_received_amount" : {
          |        "amount" : 1250,
          |        "currency" : "GBP"
          |      },
          |      "tax_underpaid_previous_year" : {
          |        "amount" : 200,
          |        "currency" : "GBP"
          |      },
          |      "upper_rate_amount" : {
          |        "amount" : 200,
          |        "currency" : "GBP"
          |      },
          |      "basic_rate_income_tax_amount" : {
          |        "amount" : 2000,
          |        "currency" : "GBP"
          |      }
          |    },
          |    "rates" : {
          |      "basic_rate_income_tax_rate" : {
          |        "percent" : "20.0%"
          |      },
          |      "higher_rate_income_tax_rate" : {
          |        "percent" : "40.0%"
          |      },
          |      "upper_rate_rate" : {
          |        "percent" : "32.5%"
          |      },
          |      "ordinary_rate_tax_rate" : {
          |        "percent" : "7.5%"
          |      }
          |    }
          |  }
          |}
        """.stripMargin
      val transformedJson = middleTierJson(nino, 2018).transformIncomeTax(payeJson).omitEmpty
      transformedJson should be(Json.parse(expectedIncomeTaxJson))
    }

    "transform 'gov_spending' section" in {
      val expectedGovSpendingJson =
        """
          |{
          |  "taxYear" : 2018,
          |  "nino" : "AB654321B",
          |  "lnks" : [ {
          |    "rel" : "self",
          |    "href" : "https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/AB654321B/2018"
          |  } ],
          |  "gov_spending" : {
          |    "taxYear" : 2018,
          |    "govSpendAmountData" : {
          |      "PublicOrderAndSafety" : {
          |        "amount" : {
          |          "amount" : 180.6,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 4.3
          |      },
          |      "Environment" : {
          |        "amount" : {
          |          "amount" : 67.2,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 1.6
          |      },
          |      "OverseasAid" : {
          |        "amount" : {
          |          "amount" : 50.4,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 1.2
          |      },
          |      "BusinessAndIndustry" : {
          |        "amount" : {
          |          "amount" : 121.8,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 2.9
          |      },
          |      "NationalDebtInterest" : {
          |        "amount" : {
          |          "amount" : 256.2,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 6.1
          |      },
          |      "Defence" : {
          |        "amount" : {
          |          "amount" : 222.6,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 5.3
          |      },
          |      "Health" : {
          |        "amount" : {
          |          "amount" : 835.8,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 19.9
          |      },
          |      "Culture" : {
          |        "amount" : {
          |          "amount" : 67.2,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 1.6
          |      },
          |      "UkContributionToEuBudget" : {
          |        "amount" : {
          |          "amount" : 29.4,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 0.7
          |      },
          |      "HousingAndUtilities" : {
          |        "amount" : {
          |          "amount" : 67.2,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 1.6
          |      },
          |      "Transport" : {
          |        "amount" : {
          |          "amount" : 180.6,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 4.3
          |      },
          |      "Welfare" : {
          |        "amount" : {
          |          "amount" : 999.6,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 23.8
          |      },
          |      "GovernmentAdministration" : {
          |        "amount" : {
          |          "amount" : 88.2,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 2.1
          |      },
          |      "Education" : {
          |        "amount" : {
          |          "amount" : 504,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 12
          |      },
          |      "StatePensions" : {
          |        "amount" : {
          |          "amount" : 537.6,
          |          "currency" : "GBP"
          |        },
          |        "percentage" : 12.8
          |      }
          |    },
          |    "totalAmount" : {
          |      "amount" : 4200,
          |      "currency" : "GBP"
          |    }
          |  }
          |}
        """.stripMargin
      val transformedJson = middleTierJson(nino, 2018).transformGovSpendingData(payeJson).omitEmpty
      transformedJson should be(Json.parse(expectedGovSpendingJson))
    }
  }
}
