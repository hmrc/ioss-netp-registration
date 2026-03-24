/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import play.api.Configuration
import sttp.model.HeaderNames

import javax.inject.Inject

case class EtmpDisplayRegistrationConfig @Inject (
                                                   config: Configuration,
                                                   genericConfig: EisGenericConfig
                                                 ) {

  val baseUrl: Service = config.get[Service]("microservice.services.display-registration")
  private val authorizationToken: String = config.get[String]("microservice.services.display-registration.authorizationToken")

  def eisEtmpGetHeaders(correlationId: String): Seq[(String, String)] =
    genericConfig.eisEtmpGenericHeaders(correlationId) ++ Seq(HeaderNames.Authorization -> s"Bearer $authorizationToken")

}
