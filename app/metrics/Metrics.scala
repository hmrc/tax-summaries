package metrics

/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

import com.codahale.metrics.Timer
import com.codahale.metrics.Timer.Context
import com.google.inject.ImplementedBy
import metrics.MetricsEnumeration.MetricsEnumeration

import javax.inject.Inject

@ImplementedBy(classOf[MetricsImpl])
trait Metrics {

  def startTimer(api: MetricsEnumeration): Timer.Context

  def incrementSuccessCounter(api: MetricsEnumeration): Unit

  def incrementFailedCounter(api: MetricsEnumeration): Unit

}

class MetricsImpl @Inject()(metrics: com.kenshoo.play.metrics.Metrics)
  extends Metrics {

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
