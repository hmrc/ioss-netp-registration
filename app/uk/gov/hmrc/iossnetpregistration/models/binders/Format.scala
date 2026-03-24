/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.binders

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object Format {

  val eisDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
    .withLocale(Locale.ENGLISH)
    .withZone(ZoneId.of("GMT"))

}