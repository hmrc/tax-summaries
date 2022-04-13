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

import models.Audit
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import utils.{BaseSpec, NinoHelperSpec}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AtsAuditSpec extends NinoHelperSpec {

  implicit val ec = inject[ExecutionContext]

  "doAudit" must {
    "generate a data event" when {
      val mockAuditConnector = mock[DefaultAuditConnector]
      object TestAudit extends AtsAudit(mockAuditConnector, "ats")

      " a period is available" in {
        val authorisationVal = Authorization("sdfgsdfghdsdhf")
        val taxYear =
          s"${LocalDate.now().minusYears(1).getYear}-${LocalDate.now().getYear}"
        implicit val headerCarrier =
          HeaderCarrier(
            deviceID = Some("aDeviceid*****"),
            authorization = Some(authorisationVal),
            trueClientIp = Some("192.168.2.2")
          )

        val audit = Audit(
          "liabilityPeriodUpdateSuccessful",
          "ats",
          Map(
            "nino"          -> nino,
            "endsOn"        -> s"${LocalDate.now().getYear}-07-05",
            "startsOn"      -> s"${LocalDate.now().minusYears(1).getYear}-04-06",
            "deviceID"      -> "aDeviceid*****",
            "taxYear"       -> taxYear,
            "ipAddress"     -> "192.168.2.2",
            "Authorization" -> "sdfgsdfghdsdhf"
          )
        )

        val dataEventArgumentCaptor: ArgumentCaptor[DataEvent] =
          ArgumentCaptor.forClass(classOf[DataEvent])

        when(mockAuditConnector.sendEvent(any[DataEvent])(any(), any()))
          .thenReturn(Future.successful(AuditResult.Success))
        TestAudit.doAudit(audit)

        verify(mockAuditConnector).sendEvent(dataEventArgumentCaptor.capture())(any(), any())

        dataEventArgumentCaptor.getValue.auditSource mustBe "ats"
        dataEventArgumentCaptor.getValue.auditType mustBe "liabilityPeriodUpdateSuccessful"
        dataEventArgumentCaptor.getValue.tags mustBe Map(
          "clientIP"          -> "192.168.2.2",
          "path"              -> "N/A",
          "X-Session-ID"      -> "-",
          "X-Request-ID"      -> "-",
          "clientPort"        -> "-",
          "transactionName"   -> "ats",
          "Akamai-Reputation" -> "-",
          "deviceID"          -> "aDeviceid*****"
        )
        dataEventArgumentCaptor.getValue.detail mustBe Map(
          "nino"          -> nino,
          "endsOn"        -> s"${LocalDate.now().getYear}-07-05",
          "startsOn"      -> s"${LocalDate.now().minusYears(1).getYear}-04-06",
          "deviceID"      -> "aDeviceid*****",
          "taxYear"       -> taxYear,
          "ipAddress"     -> "192.168.2.2",
          "Authorization" -> "sdfgsdfghdsdhf"
        )
      }
    }
  }

  "doAudit" must {
    "generate a data event" when {
      val mockAuditConnector = mock[DefaultAuditConnector]
      object TestAudit extends AtsAudit(mockAuditConnector, "ats")

      "a period is not available" in {

        val authorisationVal = Authorization("sdfgsdfghdsdhf")
        implicit val headerCarrier =
          HeaderCarrier(
            deviceID = Some("aDeviceid*****"),
            authorization = Some(authorisationVal),
            trueClientIp = Some("192.168.2.2")
          )

        val dataEventArgumentCaptor: ArgumentCaptor[DataEvent] =
          ArgumentCaptor.forClass(classOf[DataEvent])

        val audit = Audit(
          "liabilityRequestSuccessful",
          "ats_LiabilityRequest",
          Map(
            "nino"          -> nino,
            "endsOn"        -> "",
            "startsOn"      -> "",
            "deviceID"      -> "aDeviceid*****",
            "taxYear"       -> "2015-2016",
            "ipAddress"     -> "192.168.2.2",
            "Authorization" -> "sdfgsdfghdsdhf"
          )
        )

        when(mockAuditConnector.sendEvent(any[DataEvent])(any(), any()))
          .thenReturn(Future.successful(AuditResult.Success))
        TestAudit.doAudit(audit)

        verify(mockAuditConnector).sendEvent(dataEventArgumentCaptor.capture())(any(), any())

        dataEventArgumentCaptor.getValue.auditSource mustBe "ats"
        dataEventArgumentCaptor.getValue.auditType mustBe "liabilityRequestSuccessful"
        dataEventArgumentCaptor.getValue.tags mustBe Map(
          "clientIP"          -> "192.168.2.2",
          "path"              -> "N/A",
          "X-Session-ID"      -> "-",
          "X-Request-ID"      -> "-",
          "clientPort"        -> "-",
          "transactionName"   -> "ats_LiabilityRequest",
          "Akamai-Reputation" -> "-",
          "deviceID"          -> "aDeviceid*****"
        )
        dataEventArgumentCaptor.getValue.detail mustBe Map(
          "nino"          -> nino,
          "endsOn"        -> "",
          "startsOn"      -> "",
          "deviceID"      -> "aDeviceid*****",
          "taxYear"       -> "2015-2016",
          "ipAddress"     -> "192.168.2.2",
          "Authorization" -> "sdfgsdfghdsdhf"
        )
      }
    }
  }
}
