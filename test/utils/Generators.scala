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

package utils

import errors.AtsError
import models.paye.PayeAtsMiddleTier
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.oneOf
import services.GoodsAndServices

object Generators {
  def keyValueGen[T, U](keys: List[T], valGen: Gen[U]): Gen[Map[T, U]] =
    Gen
      .listOf(for {
        key   <- Gen.oneOf(keys)
        value <- valGen
      } yield (key, value))
      .map(Map.apply(_: _*))

  val genGbpAmount: Gen[Amount] = arbitrary[Double].map(Amount(_, "GBP"))

  val genLiabilityMap: Gen[Map[LiabilityKey, Amount]] =
    keyValueGen(LiabilityKey.allItems, genGbpAmount)

  val genRateMap: Gen[Map[RateKey, ApiRate]] =
    keyValueGen(RateKey.allItems, arbitrary[String].map(ApiRate.apply))

  val genDataHolder: Gen[DataHolder] = for {
    payload <- Gen.option(genLiabilityMap)
    rates   <- Gen.option(genRateMap)
    status  <- Gen.option(oneOf(List(UK(), Welsh(), Scottish())))
  } yield DataHolder(payload, rates, status)

  val genSpendData: Gen[SpendData] = for {
    a <- genGbpAmount
    p <- arbitrary[Double]
  } yield SpendData(a, p)

  val genSpendDataMap: Gen[Map[GoodsAndServices, SpendData]] =
    keyValueGen(GoodsAndServices.allItems, genSpendData)

  val genGovernmentSpending: Gen[GovernmentSpendingOutputWrapper] = for {
    taxYear <- arbitrary[Int]
    spend   <- genSpendDataMap
    total   <- arbitrary[Double].map(Amount(_, "GBP"))
    errors  <- Gen.option(arbitrary[String].map(AtsError.apply))
  } yield GovernmentSpendingOutputWrapper(taxYear, spend, total, errors)

  val genPayeAsMiddleTier: Gen[PayeAtsMiddleTier] = for {
    taxYear     <- arbitrary[Int]
    nino        <- arbitrary[String]
    incomeTax   <- Gen.option(genDataHolder)
    summary     <- Gen.option(genDataHolder)
    income      <- Gen.option(genDataHolder)
    allowance   <- Gen.option(genDataHolder)
    govSpending <- Gen.option(genGovernmentSpending)
  } yield PayeAtsMiddleTier(taxYear, nino, incomeTax, summary, income, allowance, govSpending)
}
