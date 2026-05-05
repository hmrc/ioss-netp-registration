/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.iossnetpregistration.controllers.actions

import com.google.inject.Inject
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.*
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.requests.AnyLoggedInUserRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait AnyLoggedInUserAction extends ActionBuilder[AnyLoggedInUserRequest, AnyContent] with ActionFunction[Request, AnyLoggedInUserRequest]

class AnyLoggedInUserActionImpl @Inject()(
                                           override val authConnector: AuthConnector,
                                           val parser: BodyParsers.Default
                                         )
                                         (implicit val executionContext: ExecutionContext) extends AnyLoggedInUserAction with AuthorisedFunctions with Logging {


  override def invokeBlock[A](request: Request[A], block: AnyLoggedInUserRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised().retrieve(Retrievals.internalId and Retrievals.allEnrolments) {
      case Some(internalId) ~ enrolments =>
        block(AnyLoggedInUserRequest(request, internalId, enrolments))
      case _ =>
        throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover {
      case e: NoActiveSession =>
        Unauthorized(e.getMessage)
      case e: AuthorisationException =>
        Unauthorized(e.getMessage)
    }
  }

}

