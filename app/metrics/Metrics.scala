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

package metrics

import com.codahale.metrics.Timer
import com.codahale.metrics.Timer.Context
import com.google.inject.{ImplementedBy, Inject}
import metrics.MetricsEnumeration.MetricsEnumeration
@ImplementedBy(classOf[MetricsImpl])
trait Metrics {

  def startTimer(api: MetricsEnumeration): Timer.Context

  def incrementSuccessCounter(api: MetricsEnumeration): Unit

  def incrementFailedCounter(api: MetricsEnumeration): Unit

}

class MetricsImpl @Inject()(metrics: com.kenshoo.play.metrics.Metrics) extends Metrics {

  val timers = Map(
    MetricsEnumeration.GET_PAYE_TAX_SUMMARY -> metrics.defaultRegistry
      .timer("get-paye-tax-summary-timer"),
    MetricsEnumeration.GET_SA -> metrics.defaultRegistry
      .timer("get-sa-timer"),
    MetricsEnumeration.GET_SA_LIST -> metrics.defaultRegistry
      .timer("get-sa-list-timer"),
    MetricsEnumeration.GET_SA_TAX_PAYER_DETAILS -> metrics.defaultRegistry
      .timer("get-sa-tax-payer-details-timer")
  )

  val successCounters = Map(
    MetricsEnumeration.GET_PAYE_TAX_SUMMARY -> metrics.defaultRegistry
      .counter("get-paye-tax-summary-timer-success-counter"),
    MetricsEnumeration.GET_SA -> metrics.defaultRegistry
      .counter("get-sa-timer-success-counter"),
    MetricsEnumeration.GET_SA_LIST -> metrics.defaultRegistry
      .counter("get-sa-list-timer-success-counter"),
    MetricsEnumeration.GET_SA_TAX_PAYER_DETAILS -> metrics.defaultRegistry
      .counter("get-sa-tax-payer-details-success-counter")
  )

  val failedCounters = Map(
    MetricsEnumeration.GET_PAYE_TAX_SUMMARY -> metrics.defaultRegistry
      .counter("get-paye-tax-summary-timer-failed-counter"),
    MetricsEnumeration.GET_SA -> metrics.defaultRegistry
      .counter("get-sa-timer-failed-counter"),
    MetricsEnumeration.GET_SA_LIST -> metrics.defaultRegistry
      .counter("get-sa-list-failed-counter"),
    MetricsEnumeration.GET_SA_TAX_PAYER_DETAILS -> metrics.defaultRegistry
      .counter("get-sa-tax-payer-details-failed-counter")
  )

  override def startTimer(api: MetricsEnumeration): Context = timers(api).time()

  override def incrementSuccessCounter(api: MetricsEnumeration): Unit =
    successCounters(api).inc()

  override def incrementFailedCounter(api: MetricsEnumeration): Unit =
    failedCounters(api).inc()
}
