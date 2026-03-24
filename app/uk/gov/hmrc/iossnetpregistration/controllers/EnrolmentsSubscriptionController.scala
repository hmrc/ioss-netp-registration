/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.controllers

import play.api.libs.json.JsValue
import play.api.mvc.Action
import uk.gov.hmrc.iossnetpregistration.controllers.actions.AuthenticatedControllerComponents
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.RegistrationStatus
import uk.gov.hmrc.iossnetpregistration.models.enrolments.EnrolmentStatus
import uk.gov.hmrc.iossnetpregistration.models.etmp.EtmpRegistrationStatus
import uk.gov.hmrc.iossnetpregistration.repositories.RegistrationStatusRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future

class EnrolmentsSubscriptionController @Inject()(
                                                  cc: AuthenticatedControllerComponents,
                                                  registrationStatusRepository: RegistrationStatusRepository,
                                                ) extends BackendController(cc) with Logging {

  def authoriseEnrolment(subscriptionId: String): Action[JsValue] =
    Action.async(parse.json) {
      implicit request =>
        val enrolmentStatus = (request.body \ "state").as[EnrolmentStatus]
        if (enrolmentStatus == EnrolmentStatus.Success) {
          logger.info(s"Enrolment complete for $subscriptionId, enrolment state = $enrolmentStatus")
          registrationStatusRepository.set(RegistrationStatus(subscriptionId,
            status = EtmpRegistrationStatus.Success))
        } else {
          logger.error(s"Enrolment failure for $subscriptionId, enrolment state = $enrolmentStatus ${request.body}")
          registrationStatusRepository.set(RegistrationStatus(subscriptionId,
            status = EtmpRegistrationStatus.Error))
        }
        Future.successful(NoContent)
    }

}
