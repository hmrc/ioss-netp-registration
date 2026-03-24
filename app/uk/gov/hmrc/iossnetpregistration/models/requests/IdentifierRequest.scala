/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.requests

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.domain.Vrn

case class IdentifierRequest[A] (
                                  request: Request[A],
                                  userId: String,
                                  enrolments: Enrolments,
                                  vrn: Vrn,
                                  intermediaryNumber: String
                                ) extends WrappedRequest[A](request)

case class SessionRequest[A](request: Request[A], userId: String) extends WrappedRequest[A](request)