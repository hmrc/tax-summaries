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

package paye.repositories

import common.config.ApplicationConfig
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import paye.models.PayeAtsMiddleTierMongo
import play.api.libs.json.JsObject
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NpsCacheRepository @Inject() (config: ApplicationConfig, mongoComponent: MongoComponent)(implicit
  ec: ExecutionContext
) extends PlayMongoRepository[PayeAtsMiddleTierMongo](
      collectionName = "paye-cache",
      mongoComponent = mongoComponent,
      domainFormat = PayeAtsMiddleTierMongo.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("expiresAt"),
          IndexOptions()
            .name("expires-at-index")
            .expireAfter(0, TimeUnit.SECONDS)
        )
      )
    ) {

  private def filterById(id: String): Bson = Filters.equal("_id", id)

  def get(nino: String, taxYear: Int): Future[Option[PayeAtsMiddleTierMongo]] = {
    // id is s"$nino$taxYear"
    val modifier = Updates.set("expiresAt", config.calculateExpiryTime())
    collection.findOneAndUpdate(filterById(s"$nino$taxYear"), modifier).toFutureOption()

  }

  def set(nino: String, taxYear: Int, data: JsObject): Future[Boolean] = {
    val mongoData = PayeAtsMiddleTierMongo(s"$nino$taxYear", data, config.calculateExpiryTime())
    collection
      .replaceOne(
        filter = filterById(mongoData._id),
        replacement = mongoData,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(result => result.wasAcknowledged())
  }
}
