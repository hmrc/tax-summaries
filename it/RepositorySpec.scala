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

import models.paye.PayeAtsMiddleTier
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{DefaultDB, MongoConnection}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

class RepositorySpec extends FreeSpec with MustMatchers with ScalaFutures with IntegrationPatience {

  val connectionString = "mongodb://localhost:27017/tax-summaries-integration"

  def getDatabase(connection: MongoConnection): Future[DefaultDB] = {
    connection.database("tax-summaries-integration")
  }

  def getConnection(application: Application): Try[MongoConnection] = {
    val api = application.injector.instanceOf[ReactiveMongoApi]
    for {
      uri <- MongoConnection.parseURI(connectionString)
      connection <- api.driver.connection(uri, strictUri = true)
    } yield connection
  }

  def dropTheDatabase(connection: MongoConnection): Unit = {
    Await.result(getDatabase(connection).flatMap(_.drop()), Duration.Inf)
  }

  "a repository" - {
    "must be able to store and retrieve a payload" in {

      val application = appBuilder.build()

      running(application) {
        getConnection(application).map { connection =>

          dropTheDatabase(connection)

          val repository = application.injector.instanceOf[Repository]

          val data = PayeAtsMiddleTier(2018, "NINONINO", None, None, None, None, None)
          val storedOk = repository.set("NINONINO", 2018, data)
          storedOk.futureValue mustBe true

          val retrieved = repository.get("NINONINO", 2018)
            .map(_.getOrElse(fail("The record was not found in the database")))

          retrieved.futureValue mustBe data

          dropTheDatabase(connection)
        }.get
      }
    }
  }

  private lazy val appBuilder =  new GuiceApplicationBuilder().configure(Seq(
    "mongodb.uri" -> connectionString,
    "metrics.enabled" -> false,
    "auditing.enabled" -> false,
    "mongo-async-driver.akka.log-dead-letters" -> 0
  ): _*)

}
