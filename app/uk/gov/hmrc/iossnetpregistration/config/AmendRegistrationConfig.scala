/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import play.api.Configuration
import play.api.http.HeaderNames

import javax.inject.Inject

case class AmendRegistrationConfig @Inject()(
                                            config: Configuration,
                                            genericConfig: EisGenericConfig
                                            ) {
  val baseUrl: Service = config.get[Service]("microservice.services.amend-registration")

  private val authorizationToken: String = config.get[String]("microservice.services.amend-registration.authorizationToken")

  def eisEtmpAmendHeaders(correlationId: String): Seq[(String, String)] = genericConfig.eisEtmpGenericHeaders(correlationId) ++ Seq(
    HeaderNames.AUTHORIZATION -> s"Bearer $authorizationToken"
  )
}
