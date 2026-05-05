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

package uk.gov.hmrc.iossnetpregistration.controllers

import jakarta.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.iossnetpregistration.controllers.actions.AuthenticatedControllerComponents
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.{PendingRegistrationRequest, SavedPendingRegistration}
import uk.gov.hmrc.iossnetpregistration.services.SavePendingRegistrationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class PendingRegistrationController @Inject()(
                                               cc: AuthenticatedControllerComponents,
                                               savePendingRegistrationService: SavePendingRegistrationService
                                             )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def post(): Action[PendingRegistrationRequest] = cc.identify(parse.json[PendingRegistrationRequest]).async {
    implicit request =>
      savePendingRegistrationService.savePendingRegistration(request.body).map {
        savedPendingRegistration => Created(Json.toJson(savedPendingRegistration))
      }
  }

  def get(journeyIdOrUrlCode: String): Action[AnyContent] = cc.anyLoggedInUserAction.async {
    implicit request =>
      savePendingRegistrationService.getPendingRegistration(journeyIdOrUrlCode).map {
        case Some(savedPendingRegistration) =>
          Ok(Json.toJson(savedPendingRegistration))
        case None =>
          logger.warn("A problem occurred when trying to retrieve the pending registration record with the given journeyId")
          InternalServerError
      }
  }

  def validate(uniqueUrlCode: String, uniqueActivationCode: String): Action[AnyContent] = cc.anyLoggedInUserAction.async {
    implicit request =>
      savePendingRegistrationService.validateClientActivationCode(uniqueUrlCode, uniqueActivationCode).map {
        case Some(boolean: Boolean) => Ok(Json.toJson(boolean))
        case None =>
          logger.warn("A problem occurred when trying to retrieve the pending registration to validate the activation code")
          InternalServerError
      }
  }

  def getByIntermediaryNumber(intermediaryNumber: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      savePendingRegistrationService.getPendingRegistrationsByIntermediaryNumber(intermediaryNumber)
        .map { registrations =>
          Ok(Json.toJson(registrations))
        }
        .recover {
          case ex: Throwable =>
            logger.error(s"Error retrieving pending registrations for intermediary $intermediaryNumber", ex)
            InternalServerError(Json.obj(
              "message" -> "An unexpected error occurred. Please try again later."
            ))
        }
    }

  def getByCustomerIdentification(idType: String, idValue: String): Action[AnyContent] = {
    cc.identify.async { implicit request =>
      savePendingRegistrationService
        .getPendingRegistrationsByCustomerIdentification(idType, idValue)
        .map { registrations =>
          Ok(Json.toJson(registrations))
        }
        .recover {
          case ex: Throwable =>
            logger.error(s"Error retrieving pending registrations for idType: $idType", ex)
            InternalServerError(Json.obj(
              "message" -> "An unexpected error occurred. Please try again later."
            ))
        }
    }
  }

  def getCount(intermediaryNumber: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      savePendingRegistrationService.getCount(intermediaryNumber)
        .map { count =>
          Ok(Json.toJson(count))
        }
        .recover {
          case ex: Throwable =>
            logger.error(s"Error retrieving a number pending registrations for intermediary $intermediaryNumber", ex)
            InternalServerError(Json.obj(
              "message" -> "An unexpected error occurred. Please try again later."
            ))
        }
  }
  
  def delete(journeyId: String): Action[AnyContent] = cc.clientIdentify.async {
    implicit request =>
      savePendingRegistrationService.deletePendingRegistration(journeyId).map { isDeleted =>
        Ok(Json.toJson(isDeleted))
      }.recover {
        case e =>
          logger.error(s"Error occurred while deleting $journeyId pending registration ${e.getMessage}", e)
          throw e
      }
  }

  def updateClientEmailAddress(journeyId: String, newEmailAddress: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      savePendingRegistrationService.updateClientEmailAddress(journeyId, newEmailAddress). map {
        case Some(updatedPendingRegistration) =>
          Ok(Json.toJson(updatedPendingRegistration))

        case None =>
          logger.warn(s"No record found for journeyId: $journeyId")
          NotFound
      }
  }
}
