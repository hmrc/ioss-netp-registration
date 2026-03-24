/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.iossnetpregistration.connectors.ValidateCoreRegistrationConnector
import uk.gov.hmrc.iossnetpregistration.controllers.actions.AuthenticatedControllerComponents
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.core.CoreRegistrationRequest
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ValidateCoreRegistrationController @Inject()(
                                                    cc: AuthenticatedControllerComponents,
                                                    validateCoreRegistrationConnector: ValidateCoreRegistrationConnector
                                                  )
                                                  (implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  def post: Action[CoreRegistrationRequest] = cc.identify(parse.json[CoreRegistrationRequest]).async {
    implicit request =>

      validateCoreRegistrationConnector.validateCoreRegistration(request.body).map {
        case Left(value) => InternalServerError(Json.toJson(value.body))
        case Right(value) =>
          logger.info(s"Received ${Json.toJson(value)} from core validation endpoint")
          Ok(Json.toJson(value))

      }
  }

}