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
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo
import uk.gov.hmrc.iossnetpregistration.models.{DesAddress, IntermediaryDetails, PendingRegistrationRequest, UserAnswers}
import uk.gov.hmrc.iossnetpregistration.services.SavePendingRegistrationService
import uk.gov.hmrc.iossnetpregistration.services.test.TestOnlyActivationCodeService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{Instant, LocalDate}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class TestOnlyPendingRegistrationController @Inject()(
                                                   cc: ControllerComponents,
                                                   pendingRegistrationService: SavePendingRegistrationService
                                                 )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def createPending(amount: Int, intermediaryNumber: String): Action[AnyContent] = cc.actionBuilder.async {
    implicit request =>
      val pendingRegistrations = generatePendingRegistrations(amount, intermediaryNumber)

      Future.sequence(pendingRegistrations.map { (_, pendingRegistration) =>
        pendingRegistrationService.savePendingRegistration(pendingRegistration)
      }).map(_ => Ok(Json.toJson(pendingRegistrations.map(_._1))))
  }

  private def generatePendingRegistrations(amount: Int, intermediaryNumber: String): Seq[(String, PendingRegistrationRequest)] = {
    (1 to amount).map { index =>
      val randomVrn = s"44${new Random().between(1000001, 9999999).toString.padTo(7, "1")}"
      (randomVrn, PendingRegistrationRequest(
        userAnswers = UserAnswers(
          id = s"id-$randomVrn",
          journeyId = UUID.randomUUID().toString,
          data = Json.parse(s"""{
                   |    "businessBasedInUK": true,
                   |    "clientHasVatNumber": true,
                   |    "clientVatNumber": "$randomVrn",
                   |    "hasTradingName": false,
                   |    "businessContactDetails": {
                   |      "fullName": "fullName",
                   |      "telephoneNumber": "555999111",
                   |      "emailAddress": "test@test.com"
                   |    },
                   |    "hasFixedEstablishmentInTheEu": false,
                   |    "previouslyRegistered": false,
                   |    "websites": [
                   |      {
                   |        "site": "www.website.com"
                   |      }
                   |    ],
                   |    "intermediaryDetails": {
                   |      "intermediaryNumber": "$intermediaryNumber",
                   |      "intermediaryName": "Intermediary Name"
                   |    }
                   |  }
                   |""".stripMargin).as[JsObject],
          vatInfo = Some(VatCustomerInfo(
            registrationDate = Some(LocalDate.now()),
            desAddress = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
            organisationName = Some("Company name"),
            singleMarketIndicator = true,
            individualName = None,
            deregistrationDecisionDate = None
          )),
          lastUpdated = Instant.now()
        ),
        intermediaryDetails = IntermediaryDetails(intermediaryNumber, "Perf test automated Int")
      ))
    }
  }

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
