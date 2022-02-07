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

import models.paye.PayeAtsMiddleTier
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import play.api.libs.json.{JsObject, Json, Reads, __}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.JsonOps
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Repository @Inject()(mongoComponent: MongoComponent)
    extends PlayMongoRepository[PayeAtsMiddleTier](
      collectionName = "tax-summaries",
      mongoComponent = mongoComponent,
      domainFormat = PayeAtsMiddleTier.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("expiresAt"),
          IndexOptions()
            .name("expires-at-index")
            .expireAfter(0, TimeUnit.SECONDS)
        )
      )
    ) {

//  private def collection: Future[JSONCollection] =
//    mongo.database.map(_.collection[JSONCollection](collectionName))

//  private val lastUpdatedIndex = Index(
//    key = Seq("expiresAt" -> IndexType.Ascending),
//    name = Some("expires-at-index"),
//    options = BSONDocument("expireAfterSeconds" -> 0))

  def buildId(nino: String, taxYear: Int): String = s"$nino::$taxYear"

  def filterById(nino: String, taxYear: Int): Bson = Filters.equal("_id", buildId(nino, taxYear))

//  val started = Future
//    .sequence {
//      Seq(
//        collection.map(c => c.indexesManager.ensure(lastUpdatedIndex))
//      )
//    }
//    .map(_ => ())

  private def calculateExpiryTime() = Timestamp.valueOf(LocalDateTime.now.plusMinutes(15)).toInstant

  def get(nino: String, taxYear: Int): Future[Option[PayeAtsMiddleTier]] = {

    println("Inside repo get....." + nino + " " + taxYear)

//    val modifier = Json.obj(
//      "$set" -> Json.obj(
//        "expiresAt" -> Json.obj("$date" -> calculateExpiryTime())
//      )
//    )

    val modifier = Updates.set("expiresAt", calculateExpiryTime())

//    implicit val readFromMongoDocument: Reads[PayeAtsMiddleTier] =
//      (__ \ "data").lazyRead(PayeAtsMiddleTier.format)

    collection.findOneAndUpdate(filterById(nino, taxYear), modifier).toFutureOption()
//
//    result
//    collection.flatMap { coll =>
//      coll
//        .findAndUpdate(
//          selector = selector,
//          update = modifier,
//          fetchNewObject = false,
//          upsert = false,
//          sort = None,
//          fields = None,
//          bypassDocumentValidation = false,
//          writeConcern = WriteConcern.Default,
//          maxTime = None,
//          collation = None,
//          arrayFilters = Seq.empty
//        )
//        .map(_.result[PayeAtsMiddleTier])
//    }
  }

  def set(nino: String, taxYear: Int, data: PayeAtsMiddleTier): Future[Boolean] = {

    println("Inside repo set....." + nino + " " + taxYear)

//    val selector = Json.obj(
//      "_id" -> buildId(nino, taxYear)
//    )
//
//    val modifierOld: JsObject = Json.obj(
//      "$set" -> Json.obj(
//        "_id"       -> buildId(nino, taxYear),
//        "data"      -> data,
//        "expiresAt" -> Json.obj("$date" -> calculateExpiryTime())
//      )
//    )

//    collection.flatMap {
//      _.update(ordered = false).one(selector, modifier, upsert = true, multi = false).map { result =>
//        result.ok
//      }
//    }

//    collection
//      .replaceOne(
//        filter = filterById(nino, taxYear),
//        replacement = data,
//        options = ReplaceOptions().upsert(true)
//      )
//      .toFuture
//      .map(result => result.wasAcknowledged())

    val modifier: Bson = Updates.combine(
      Updates.set("_id", buildId(nino, taxYear)),
      Updates.set("data", data),
      Updates.set("expiresAt", calculateExpiryTime()))

    collection
      .findOneAndUpdate(
        filter = filterById(nino, taxYear),
        update = modifier,
        options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture
      .map(_ => true)

  }
}
