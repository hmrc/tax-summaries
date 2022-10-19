/*
 * Copyright 2022 HM Revenue & Customs
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

package repositories

import models.paye.{PayeAtsMiddleTier, PayeAtsMiddleTierMongo}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.PlayMongoRepositorySupport
import utils.IntegrationSpec

import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant}
import scala.concurrent.ExecutionContext.Implicits.global

class RepositorySpec extends IntegrationSpec with PlayMongoRepositorySupport[PayeAtsMiddleTierMongo] {

  server.start()

  override def beforeEach(): Unit = {
    super.beforeEach()
    prepareDatabase()
  }

  val repository: PlayMongoRepository[PayeAtsMiddleTierMongo] = app.injector.instanceOf[Repository]
  val serviceRepo: Repository                                 = repository.asInstanceOf[Repository]

  def buildId(nino: String, taxYear: Int): String = s"$nino::$taxYear"

  "a repository" must {
    "must be able to store and retrieve a payload" in {
      val taxYear: Int      = 2018
      val minuteOffset: Int = 15

      val data = PayeAtsMiddleTier(taxYear, "NINONINO", None, None, None, None, None)

      /*
        Upgrades to HMRC Mongo seem to have changed the JSON writes for JavaTime types to be truncated at milliseconds
        rather than nano seconds. I've chosen to change the tests to reflect this rather than explicitly declaring a
        JSON format implicit to include nanoseconds.
       */

      val dataMongo = PayeAtsMiddleTierMongo(
        _id = buildId("NINONINO", taxYear),
        data = data,
        expiresAt = Instant.now().plus(Duration.ofMinutes(minuteOffset)).truncatedTo(ChronoUnit.MILLIS)
      )

      val storedOk = serviceRepo.set(dataMongo)
      storedOk.futureValue mustBe true

      val retrieved = serviceRepo
        .get("NINONINO", taxYear)
        .map(
          _.getOrElse(fail("The record was not found in the database"))
        )

      retrieved.futureValue mustBe dataMongo
    }
  }
}
