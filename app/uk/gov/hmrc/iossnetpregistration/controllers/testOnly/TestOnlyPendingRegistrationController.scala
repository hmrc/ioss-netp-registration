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

package uk.gov.hmrc.iossnetpregistration.controllers.testOnly

import jakarta.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.services.SavePendingRegistrationService
import uk.gov.hmrc.iossnetpregistration.services.test.TestOnlyActivationCodeService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class TestOnlyPendingRegistrationController @Inject()(
                                                   cc: ControllerComponents,
                                                   pendingRegistrationService: SavePendingRegistrationService
                                                 )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def deletePending(): Action[AnyContent] = cc.actionBuilder.async {
    implicit request =>
      pendingRegistrationService.deleteAllPendingRegistrations().map {
        case true =>
          NoContent
        case _ =>
          logger.warn("TestOnlyRoute: A problem occurred when trying to delete all pending registrations")
          InternalServerError
      }
  }
}
