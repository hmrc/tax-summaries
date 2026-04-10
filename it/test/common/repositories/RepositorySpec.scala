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

package common.repositories

import common.utils.IntegrationSpec
import paye.models.{PayeAtsMiddleTier, PayeAtsMiddleTierMongo}
import paye.repositories.NpsCacheRepository
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.PlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant}

class RepositorySpec extends IntegrationSpec with PlayMongoRepositorySupport[PayeAtsMiddleTierMongo] {

  server.start()

  override def beforeEach(): Unit = {
    super.beforeEach()
    prepareDatabase()
  }

  val repository: PlayMongoRepository[PayeAtsMiddleTierMongo] = app.injector.instanceOf[NpsCacheRepository]
  val serviceRepo: NpsCacheRepository                         = repository.asInstanceOf[NpsCacheRepository]

  "a repository" must {
    "must be able to store and retrieve a payload" in {
      val taxYear: Int      = 2024
      val minuteOffset: Int = 15

      val data = PayeAtsMiddleTier(taxYear, "NINONINO", None, None, None, None, None, includeBRDMessage = false)

      /*
        Upgrades to HMRC Mongo seem to have changed the JSON writes for JavaTime types to be truncated at milliseconds
        rather than nano seconds. I've chosen to change the tests to reflect this rather than explicitly declaring a
        JSON format implicit to include nanoseconds.
       */

      val dataObject = Json.toJson(data).as[JsObject]

      val storedOk = serviceRepo.set("NINONINO", taxYear, dataObject)
      storedOk.futureValue mustBe true

      val retrieved = serviceRepo
        .get("NINONINO", taxYear)
        .map(
          _.getOrElse(fail("The record was not found in the database"))
        )
        .futureValue
      val dataMongo = PayeAtsMiddleTierMongo(
        _id = s"NINONINO$taxYear",
        data = dataObject,
        expiresAt = Instant.now().plus(Duration.ofMinutes(minuteOffset)).truncatedTo(ChronoUnit.MINUTES)
      )
      retrieved._id mustBe dataMongo._id
      retrieved.data mustBe dataMongo.data
      retrieved.expiresAt.truncatedTo(ChronoUnit.MINUTES) mustBe dataMongo.expiresAt
    }
  }
}
