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

package sa.repositories

import common.config.ApplicationConfig
import uk.gov.hmrc.mongo.{CurrentTimestampSupport, MongoComponent}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class TaxSummariesSessionCacheRepository @Inject() (appConfig: ApplicationConfig, mongoComponent: MongoComponent)(
  implicit ec: ExecutionContext
) extends SessionCacheRepository(
      mongoComponent = mongoComponent,
      collectionName = "sessions",
      ttl = appConfig.sessionCacheTtl minutes,
      timestampSupport = new CurrentTimestampSupport()
    )
