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

package audit

import config.ApplicationConfig
import models.Audit
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AtsAudit @Inject()(auditConnector: AuditConnector, applicationConfig: ApplicationConfig) extends Logging {

  def doAudit(audit: Audit)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Unit = {
    val auditDetails = audit.auditDetails

    logger.debug(s"Auditing DataEvent HeaderCarrier: $headerCarrier")
    logger.debug(s"Auditing DataEvent Details: $auditDetails")
    logger.debug(s"Auditing DataEvent eventTypeMessage: ${audit.eventTypeMessage}")

    auditConnector.sendEvent(
      DataEvent(
        applicationConfig.appName,
        audit.eventTypeMessage,
        tags = headerCarrier.toAuditTags(),
        detail = auditDetails)
    )
  }
}
