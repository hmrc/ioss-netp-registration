/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import play.api.Configuration

import javax.inject.Inject

class GetVatInfoConfig @Inject()(config: Configuration) {

  val baseUrl: Service = config.get[Service]("microservice.services.get-vat-info")
  val authorizationToken: String = config.get[String]("microservice.services.get-vat-info.authorizationToken")
  val environment: String = config.get[String]("microservice.services.get-vat-info.environment")
}
