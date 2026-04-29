/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.requests

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.Enrolments

case class AnyLoggedInUserRequest[A] (
                                  request: Request[A],
                                  userId: String,
                                  enrolments: Enrolments
                                ) extends WrappedRequest[A](request)