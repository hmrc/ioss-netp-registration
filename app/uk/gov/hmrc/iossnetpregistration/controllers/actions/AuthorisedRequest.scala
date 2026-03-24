/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers.actions

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.domain.Vrn

case class AuthorisedRequest[A](request: Request[A], credentials: Credentials, userId: String, vrn: Option[Vrn], enrolments: Enrolments) extends WrappedRequest[A](request)

case class AuthorisedMandatoryVrnRequest[A](request: Request[A], credentials: Credentials, userId: String, vrn: Option[Vrn]) extends WrappedRequest[A](request)
