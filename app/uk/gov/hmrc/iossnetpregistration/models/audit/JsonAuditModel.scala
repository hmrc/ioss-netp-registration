/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.audit

import play.api.libs.json.JsValue

trait JsonAuditModel {
  val auditType: String
  val transactionName: String
  val detail: JsValue
}