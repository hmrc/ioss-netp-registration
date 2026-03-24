/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers

import play.api.libs.json.Json
import play.api.mvc.Results.Created
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.iossnetpregistration.controllers.actions.AuthenticatedControllerComponents
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.SavedUserAnswers
import uk.gov.hmrc.iossnetpregistration.models.requests.SaveForLaterRequest
import uk.gov.hmrc.iossnetpregistration.services.SaveForLaterService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SaveForLaterController @Inject()(
                                        cc: AuthenticatedControllerComponents,
                                        saveForLaterService: SaveForLaterService
                                      )(implicit executionContext: ExecutionContext) extends BackendController(cc) with Logging {

  def post(): Action[SaveForLaterRequest] = cc.identify(parse.json[SaveForLaterRequest]).async {
    implicit request =>
      saveForLaterService.saveUserAnswers(request.body).map { savedUserAnswers =>
        Created(Json.toJson(savedUserAnswers))
      }
  }

  def get(journeyId: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      saveForLaterService.getSavedUserAnswers(journeyId).map {
        case Some(savedUserAnswers: SavedUserAnswers) =>
          Ok(Json.toJson(savedUserAnswers))

        case _ =>
          logger.warn(s"A problem occurred when trying to retrieve the savedUserAnswers with the given journeyId $journeyId")
          InternalServerError
      }
  }

  def getSelection(intermediaryNumber: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      saveForLaterService.getSavedUserAnswersSelection(intermediaryNumber).map { seqSavedUserAnswers =>
        Ok(Json.toJson(seqSavedUserAnswers))
      }
  }

  def getCount(intermediaryNumber: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      saveForLaterService.getCount(intermediaryNumber)
        .map { count =>
          Ok(Json.toJson(count))
        }
        .recover {
          case ex: Throwable =>
            logger.error(s"Error retrieving quantity of saved user answers for intermediary $intermediaryNumber", ex)
            InternalServerError(Json.obj(
              "message" -> "An unexpected error occurred. Please try again later."
            ))
        }
  }
  
  def delete(journeyId: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      saveForLaterService.deleteSavedUserAnswers(journeyId).map { isDeleted =>
        Ok(Json.toJson(isDeleted))
      }
  }

}