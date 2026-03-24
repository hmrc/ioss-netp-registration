/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import play.api.Configuration

import javax.inject.Inject

class TaxEnrolmentsConfig @Inject()(config: Configuration) {

  val baseUrl: Service = config.get[Service]("microservice.services.enrolments")
  val callbackBaseUrl: String = config.get[String]("microservice.services.enrolments.callbackBaseUrl")
  val iossNetpEnrolmentName: String = config.get[String]("features.enrolments.keys.netp.name")
}
