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

import config.ApplicationConfig
import models.paye.PayeAtsMiddleTierMongo
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Repository @Inject()(config: ApplicationConfig, mongoComponent: MongoComponent)
    extends PlayMongoRepository[PayeAtsMiddleTierMongo](
      collectionName = "tax-summaries",
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

  def filterById(nino: String, taxYear: Int): Bson = Filters.equal("_id", s"$nino::$taxYear")

  def get(nino: String, taxYear: Int): Future[Option[PayeAtsMiddleTierMongo]] = {

    val modifier = Updates.set("expiresAt", config.calculateExpiryTime())

    collection.findOneAndUpdate(filterById(nino, taxYear), modifier).toFutureOption()

  }

  def set(dataMongo: PayeAtsMiddleTierMongo): Future[Boolean] =
    collection
      .replaceOne(
        filter = filterById(dataMongo.data.nino, dataMongo.data.taxYear),
        replacement = dataMongo,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture
      .map(result => result.wasAcknowledged())

}
