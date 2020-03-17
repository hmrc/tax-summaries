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
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.{Collation, WriteConcern}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import reactivemongo.play.json.collection._

import scala.concurrent.duration.FiniteDuration

class Repository @Inject()(mongo: ReactiveMongoApi) {
  val logger = LoggerFactory.getLogger("application." + getClass.getCanonicalName)

  private val collectionName: String = "tax-summaries"

  private def collection: Future[JSONCollection] =
    mongo.database.map(_.collection[JSONCollection](collectionName))

  private val lastUpdatedIndex = Index(key = Seq("expiresAt" -> IndexType.Ascending), name = Some("expires-at-index"))

  def buildId(nino: String, taxYear: Int): String = s"$nino::$taxYear"

  val started = Future
    .sequence {
      Seq(
        collection.map(c => c.indexesManager.ensure(lastUpdatedIndex))
      )
    }
    .map(_ => ())

  def get(nino: String, taxYear: Int): Future[Option[PayeAtsMiddleTier]] = {
    val selector = Json.obj(
      "_id" -> buildId(nino, taxYear)
    )

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "expiresAt" -> Json.obj("$date" -> Timestamp.valueOf(LocalDateTime.now.plusMinutes(15)))
      )
    )

    collection.flatMap {
      _.findAndUpdate(selector, modifier).map(_.result[PayeAtsMiddleTier])
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
        "expiresAt" -> Json.obj("$date" -> Timestamp.valueOf(LocalDateTime.now.plusMinutes(15)))
      )
    )

    collection.flatMap {
      _.update(ordered = false).one(selector, modifier, upsert = true, multi = false).map { result =>
        result.ok
      }
    }
  }
}
