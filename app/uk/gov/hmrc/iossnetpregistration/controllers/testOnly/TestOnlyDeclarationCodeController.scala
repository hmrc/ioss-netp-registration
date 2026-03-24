/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers.testOnly

import jakarta.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.services.test.TestOnlyActivationCodeService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class TestOnlyDeclarationCodeController @Inject()(
                                                   cc: ControllerComponents,
                                                   declarationCodeService: TestOnlyActivationCodeService
                                                 )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def getDeclarationCodeTest(uniqueUrlCode: String): Action[AnyContent] = cc.actionBuilder.async {
    implicit request =>
      declarationCodeService.getPendingRegistration(uniqueUrlCode).map {
        case Some(savedPendingRegistration) =>
          val json = Json.obj(
            "urlCode" -> uniqueUrlCode,
            "clientDeclarationCode" -> savedPendingRegistration.uniqueActivationCode,
            "savedPendingregistration" -> savedPendingRegistration
          )
          Ok(json)
        case None =>
          logger.warn("TestOnlyRoute: A problem occurred when trying to retrieve the declaration code")
          InternalServerError
      }
  }
}
