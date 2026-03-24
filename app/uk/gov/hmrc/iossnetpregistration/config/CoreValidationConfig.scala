/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import play.api.Configuration
import play.api.http.HeaderNames.{ACCEPT, AUTHORIZATION, CONTENT_TYPE, DATE, X_FORWARDED_HOST}
import play.api.http.MimeTypes
import uk.gov.hmrc.iossnetpregistration.models.binders.Format

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class CoreValidationConfig @Inject()(config: Configuration, clock: Clock) {

  val coreValidationUrl: Service = config.get[Service]("microservice.services.core-validation")

  private val XCorrelationId = "X-Correlation-Id"
  private val authorizationToken: String = config.get[String]("microservice.services.core-validation.authorizationToken")

  def eisCoreHeaders(correlationId: String): Seq[(String, String)] = Seq(
    XCorrelationId -> correlationId,
    X_FORWARDED_HOST -> "MDTP",
    CONTENT_TYPE -> MimeTypes.JSON,
    ACCEPT -> MimeTypes.JSON,
    DATE -> Format.eisDateTimeFormatter.format(LocalDateTime.now(clock)),
    AUTHORIZATION -> s"Bearer $authorizationToken"
  )
}
