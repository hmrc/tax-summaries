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

package repositories

import java.sql.Timestamp
import java.time.LocalDateTime

import javax.inject.Inject
import models.paye.PayeAtsMiddleTier
import play.api.libs.json.{Json, Reads, __}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.{JSONCollection, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Repository @Inject()(mongo: ReactiveMongoApi) {
  private val collectionName: String = "tax-summaries"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val lastUpdatedIndex = Index(
    key = Seq("expiresAt" -> IndexType.Ascending),
    name = Some("expires-at-index"),
    options = BSONDocument("expireAfterSeconds" -> 0))

  def buildId(nino: String, taxYear: Int): String = s"$nino::$taxYear"

  val started = Future
    .sequence {
      Seq(
        collection.map(c => c.indexesManager.ensure(lastUpdatedIndex))
      )
    }
    .map(_ => ())

  private def calculateExpiryTime() = Timestamp.valueOf(LocalDateTime.now.plusMinutes(15))

  def get(nino: String, taxYear: Int): Future[Option[PayeAtsMiddleTier]] = {
    val selector = Json.obj(
      "_id" -> buildId(nino, taxYear)
    )

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "expiresAt" -> Json.obj("$date" -> calculateExpiryTime())
      )
    )

    implicit val readFromMongoDocument: Reads[PayeAtsMiddleTier] =
      (__ \ "data").lazyRead(PayeAtsMiddleTier.format)

    collection.flatMap { coll =>
      coll
        .findAndUpdate(
          selector = selector,
          update = modifier,
          fetchNewObject = false,
          upsert = false,
          sort = None,
          fields = None,
          bypassDocumentValidation = false,
          writeConcern = WriteConcern.Default,
          maxTime = None,
          collation = None,
          arrayFilters = Seq.empty
        )
        .map(_.result[PayeAtsMiddleTier])
    }
  }

  def set(nino: String, taxYear: Int, data: PayeAtsMiddleTier): Future[Boolean] = {

    val selector = Json.obj(
      "_id" -> buildId(nino, taxYear)
    )

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "_id"       -> buildId(nino, taxYear),
        "data"      -> data,
        "expiresAt" -> Json.obj("$date" -> calculateExpiryTime())
      )
    )

    collection.flatMap {
      _.update(ordered = false).one(selector, modifier, upsert = true, multi = false).map { result =>
        result.ok
      }
    }
  }
}
