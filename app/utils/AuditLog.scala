/*
 * Copyright 2019 HM Revenue & Customs
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

package utils

import config.TaxSummariesAuditConnector
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by gurpreet on 14/04/16.
  */
trait AuditLog {

  val auditConnector : AuditConnector

  def createAuditEvent(transactionName:String, auditSource: String, auditType: String, path: String = "N/A", tags: Map[String, String], details: Map[String, String])(implicit hc: HeaderCarrier): Unit = {
    val event = DataEvent(
      auditSource = auditSource,
      auditType = auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, path) ++ tags,
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails(details.toSeq: _*))
    Logger.debug(s"Auditing DataEvent:  $event")

    try {
      val res = Await.result[AuditResult](auditConnector.sendEvent(event),10 seconds)
    } catch {
      case ex: Throwable => {
        Logger.warn("[AuditLog][createAuditEvent] , Problem occurred while creating audit event for nino " + s"${event.detail.get("nino")}")
      }
    }
  }
}
object AuditLog extends AuditLog{
  override lazy val auditConnector = TaxSummariesAuditConnector
}

