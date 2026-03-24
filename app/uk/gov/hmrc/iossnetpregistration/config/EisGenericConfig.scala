/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE, DATE, X_FORWARDED_HOST}
import play.api.http.MimeTypes
import uk.gov.hmrc.iossnetpregistration.models.binders.Format.eisDateTimeFormatter

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class EisGenericConfig @Inject()(clock: Clock) {

  private val XCorrelationId = "X-Correlation-Id"

  def eisEtmpGenericHeaders(correlationId: String): Seq[(String, String)] = Seq(
    CONTENT_TYPE -> MimeTypes.JSON,
    ACCEPT -> MimeTypes.JSON,
    DATE -> eisDateTimeFormatter.format(LocalDateTime.now(clock)),
    XCorrelationId -> correlationId,
    X_FORWARDED_HOST -> "MDTP"
  )

}
