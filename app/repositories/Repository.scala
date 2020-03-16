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

import models.paye.PayeAtsMiddleTier
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Repository {
  def mongo: ReactiveMongoApi

  private val collectionName: String = "tax-summaries"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val lastUpdatedIndex = Index(BSONSerializationPack)(
    key = Seq("expiresAt" -> IndexType.Ascending),
    name = Some("expires-at-index"),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    expireAfterSeconds = Some(0),
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None,
    version = None,
    partialFilter = None,
    options = BSONDocument.empty
  )

  private val internalAuthIdIndex = Index(BSONSerializationPack)(
    key = Seq("internalId" -> IndexType.Ascending),
    name = Some("internal-auth-id-index"),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    expireAfterSeconds = None,
    storageEngine = None,
    weights = None,
    defaultLanguage = None,
    languageOverride = None,
    textIndexVersion = None,
    sphereIndexVersion = None,
    bits = None,
    min = None,
    max = None,
    bucketSize = None,
    collation = None,
    wildcardProjection = None,
    version = None,
    partialFilter = None,
    options = BSONDocument.empty
  )

  val started = Future
    .sequence {
      Seq(
        collection.map(c => c.indexesManager.ensure(lastUpdatedIndex)),
        collection.map(_.indexesManager.ensure(internalAuthIdIndex))
      )
    }
    .map(_ => ())

  def get(internalId: String): Future[Option[PayeAtsMiddleTier]] = {

    val selector = Json.obj(
      "internalId" -> internalId
    )

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "expiresAt" -> Json.obj("$date" -> Timestamp.valueOf(LocalDateTime.now.plusMinutes(15)))
      )
    )

    collection.flatMap {
      _.findAndUpdate(selector, modifier, fetchNewObject = false, upsert = false).map(_.result[PayeAtsMiddleTier])
    }
  }

  def set(internalId: String, data: PayeAtsMiddleTier): Future[Boolean] = {

    val selector = Json.obj(
      "internalId" -> internalId
    )

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "internalId" -> internalId,
        "data"       -> data,
        "expiresAt"  -> Json.obj("$date" -> Timestamp.valueOf(LocalDateTime.now.plusMinutes(15)))
      )
    )

    collection.flatMap {
      _.update(ordered = false).one(selector, modifier, upsert = true, multi = false).map { result =>
        result.ok
      }
    }
  }
}

object Repository extends Repository {
  override val mongo: ReactiveMongoApi = ???
}
