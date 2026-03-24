/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.audit

import uk.gov.hmrc.iossnetpregistration.models.{Enumerable, WithName}

sealed trait EtmpRegistrationAuditType {
  val auditType: String
  val transactionName: String
}

object EtmpRegistrationAuditType extends Enumerable.Implicits {
  case object CreateRegistration extends WithName("CreateRegistration") with EtmpRegistrationAuditType {
    override val auditType: String = "EtmpRegistration"
    override val transactionName: String = "etmp-registration"
  }
  
  case object AmendRegistration extends WithName("AmendRegistration") with EtmpRegistrationAuditType {
    override val auditType: String = "EtmpAmendRegistration"
    override val transactionName: String = "etmp-amend-registration"
  }
}
