/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.iossnetpregistration.connectors.GetVatInfoConnector
import uk.gov.hmrc.iossnetpregistration.controllers.actions.AuthenticatedControllerComponents
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.responses.NotFound as DesNotFound
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VatInfoController @Inject()(
                                   cc: AuthenticatedControllerComponents,
                                   getVatInfoConnector: GetVatInfoConnector
                                 )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def get(vrn: String): Action[AnyContent] = cc.identify.async {
    implicit request =>
      getVatInfoConnector.getVatCustomerDetails(Vrn(vrn)).map {
        case Right(response) =>
          Ok(Json.toJson(response)) 
        case Left(DesNotFound) =>
          NotFound
        case _ =>
          InternalServerError
      }
  }
}
