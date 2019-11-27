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

trait PAYETransformer {
  def middleTierAttributeJson(name: String, value: Double, currency: String = "GBP"): JsObject = Json.obj(
    name -> Json.obj(
      "amount"   -> value,
      "currency" -> currency
    )
  )

  def appendAttribute(path: JsPath, anObject: JsObject) =
    path.json.update((__).read[JsObject].map { o =>
      o ++ anObject
    })

  def pickAmount(path: JsPath, json: JsValue): Option[Double] =
    json.transform(path.json.pick[JsNumber]).asOpt.map(n => n.as[Double])
}

object PAYETransformer {
  def middleTierJson(nino: String, year: Int): JsObject =
    Json.obj( // add nino
      "taxYear" -> JsNumber(year),
      "nino"    -> JsString(nino),
      "lnks" -> Json.arr(
        Json.obj(
          "rel"  -> "self",
          "href" -> s"https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/$nino/$year"
        )),
      "allowance_data"     -> Json.obj("payload" -> Json.obj()),
      "capital_gains_data" -> Json.obj("payload" -> Json.obj()),
      "income_data"        -> Json.obj("payload" -> Json.obj()),
      "rates"              -> Json.obj(),
      "gov_spending"       -> Json.obj()
    )

  implicit class RichJsObject(original: JsObject) extends PAYETransformer {
    def omitEmpty: JsObject = original.value.foldLeft(original) {
      case (obj, (key, JsString(st))) if st.isEmpty                                                   => obj - key
      case (obj, (key, JsArray(arr))) if arr.isEmpty                                                  => obj - key
      case (obj, (key, JsObject(child))) if new RichJsObject(JsObject(child)).omitEmpty.value.isEmpty => obj - key
      case (obj, (_, _))                                                                              => obj
    }

    def safeTransform(jsonTransformer: Reads[JsObject]): JsObject =
      original.transform(jsonTransformer).asOpt.map(_.omitEmpty) match {
        case Some(json) => json
        case None       => original
      }
    def transformTotalIncome(source: JsObject): JsObject = {
      val income: Option[Double] = pickAmount(__ \ 'income \ 'incomeFromEmployment, source)
      val statePension: Option[Double] = pickAmount(__ \ 'income \ 'statePension, source)
      val otherPensionIncome: Option[Double] = pickAmount(__ \ 'income \ 'otherPensionIncome, source)
      val otherIncome: Option[Double] = pickAmount(__ \ 'income \ 'otherIncome, source)
      val incomeBeforeTax: Option[Double] = pickAmount(__ \ 'income \ 'incomeBeforeTax, source)
      val taxableIncome: Option[Double] = pickAmount(__ \ 'income \ 'taxableIncome, source)
      val otherAllowancesDeductionsExpenses: Option[Double] =
        pickAmount(__ \ 'income \ 'otherAllowancesDeductionsExpenses, source)
      val employmentBenefits: Option[Double] = pickAmount(__ \ 'income \ 'employmentBenefits, source)

      val jsonTransformer =
        appendAttribute(
          __ \ 'income_data \ 'payload,
          middleTierAttributeJson("income_from_employment", income.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAttributeJson("state_pension", statePension.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAttributeJson("other_pension_income", otherPensionIncome.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAttributeJson("other_income", otherIncome.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAttributeJson("total_income_before_tax", incomeBeforeTax.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'allowance_data \ 'payload,
            middleTierAttributeJson("total_tax_free_amount", taxableIncome.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'allowance_data \ 'payload,
            middleTierAttributeJson("other_allowances_amount", otherAllowancesDeductionsExpenses.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAttributeJson("taxable_state_benefits", employmentBenefits.getOrElse(0)))

      safeTransform(jsonTransformer)
    }
  }
}

/*
  "income": {
    "incomeFromEmployment": 25000.00,
    "statePension": 1000.00,
    "otherPensionIncome": 500.00,
    "otherIncome": 3000.00,
    "incomeBeforeTax": 28000.00,
    "taxableIncome": 25500.00,
    "otherAllowancesDeductionsExpenses": 6000.00,
    "employmentBenefits": 200.00
  },
 */

/*
  "income_data": {       // Your total income (1)
    "payload": {
      "benefits_from_employment": {
        "amount": 0,
        "currency": "GBP"
      },
      "income_from_employment": {
        "amount": 10500,
        "currency": "GBP"
      },
      "other_income": {
        "amount": 0,
        "currency": "GBP"
      },
      "other_pension_income": {
        "amount": 0,
        "currency": "GBP"
      },
      "self_employment_income": {      // NOT PAYE
        "amount": 1100,
        "currency": "GBP"
      },
      "state_pension": {
        "amount": 0,
        "currency": "GBP"
      },
      "taxable_state_benefits": {
        "amount": 0,
        "currency": "GBP"
      },
      "total_income_before_tax": {
        "amount": 11600,
        "currency": "GBP"
      }
    },
    "allowance_data": {  // Your tax-free amount (2)
    "payload": {
      "marriage_allowance_transferred_amount": {
        "amount": 0,
        "currency": "GBP"
      },
      "other_allowances_amount": {
        "amount": 300,
        "currency": "GBP"
      },
      "personal_tax_free_amount": {
        "amount": 9440,
        "currency": "GBP"
      },
      "total_tax_free_amount": {
        "amount": 9740,
        "currency": "GBP"
      }
    }
  }
 */
