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

package paye.models

import paye.services.SensitiveFormatService.SensitiveJsObject
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, JsObject, JsPath, Json, OWrites, Reads}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class PayeAtsMiddleTierMongo(
  _id: String,
  data: JsObject,
  expiresAt: Instant
)

object PayeAtsMiddleTierMongo extends MongoJavatimeFormats.Implicits {

  implicit val format: Format[PayeAtsMiddleTierMongo] =
    Json.format[PayeAtsMiddleTierMongo]

  def formatSensitive(
    formatSensitiveObject: Format[SensitiveJsObject]
  ): Format[PayeAtsMiddleTierMongo] = {

    val formatInstant: Format[Instant] =
      MongoJavatimeFormats.instantFormat

    val reads: Reads[PayeAtsMiddleTierMongo] =
      (
        (JsPath \ "_id").read[String] and
          (JsPath \ "data").read[SensitiveJsObject](formatSensitiveObject) and
          (JsPath \ "expiresAt").read[Instant](formatInstant)
      ) { (id, sensitiveData, expiresAt) =>
        PayeAtsMiddleTierMongo(
          _id = id,
          data = sensitiveData.decryptedValue,
          expiresAt = expiresAt
        )
      }

    val writes: OWrites[PayeAtsMiddleTierMongo] =
      (
        (JsPath \ "_id").write[String] and
          (JsPath \ "data").write[SensitiveJsObject](formatSensitiveObject) and
          (JsPath \ "expiresAt").write[Instant](formatInstant)
      ) { mongo =>
        (
          mongo._id,
          SensitiveJsObject(mongo.data),
          mongo.expiresAt
        )
      }

    Format(reads, writes)
  }
}
