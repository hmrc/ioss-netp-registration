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

package uk.gov.hmrc.iossnetpregistration.services

import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.connectors.RegistrationHttpParser.CreateEtmpRegistrationResponse
import uk.gov.hmrc.iossnetpregistration.connectors.VatCustomerInfoHttpParser.VatCustomerInfoResponse
import uk.gov.hmrc.iossnetpregistration.connectors.{GetVatInfoConnector, RegistrationConnector}
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.{AmendRegistrationResponse, EtmpAmendRegistrationRequest}
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.{EtmpDisplayRegistration, RegistrationWrapper}
import uk.gov.hmrc.iossnetpregistration.models.etmp.{EtmpIdType, EtmpRegistrationRequest}
import uk.gov.hmrc.iossnetpregistration.models.responses.{ErrorResponse, EtmpException}
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationService @Inject()(
                                     registrationConnector: RegistrationConnector,
                                     vatInfoConnector: GetVatInfoConnector
                                   )(implicit ec: ExecutionContext) extends Logging {

  def createRegistration(etmpRegistrationRequest: EtmpRegistrationRequest): Future[CreateEtmpRegistrationResponse] =
    registrationConnector.createRegistration(etmpRegistrationRequest)

  def getRegistration(iossNumber: String)(implicit hc: HeaderCarrier): Future[RegistrationWrapper] = {

    for {
      etmpDisplayRegistrationResponse <- registrationConnector.getRegistration(iossNumber)
      registrationWrapper <- etmpDisplayRegistrationResponse match
        case Left(etmpDisplayRegistrationError) =>
          val errorMessage = s"There was an error retrieving etmpDisplayRegistration from ETMP " +
            s"with errors: ${etmpDisplayRegistrationError.body}."
          logger.error(errorMessage)
          throw EtmpException(errorMessage)

        case Right(etmpDisplayRegistration) =>
          if (etmpDisplayRegistration.customerIdentification.idType == EtmpIdType.VRN) {
            val vatCustomerInfoFutureResponse: Future[VatCustomerInfoResponse] = vatInfoConnector.getVatCustomerDetails(Vrn(etmpDisplayRegistration.customerIdentification.idValue))
            vatCustomerInfoFutureResponse.flatMap {
              case Left(vatCustomerInfoError) =>
                val errorMessage = s"There was an error retrieving vatCustomerInfo from ETMP " +
                  s"with errors: ${vatCustomerInfoError.body}."
                logger.error(errorMessage)
                throw EtmpException(errorMessage)

              case Right(vatCustomerInfo) => RegistrationWrapper(Some(vatCustomerInfo), etmpDisplayRegistration).toFuture
            }
          } else {
            RegistrationWrapper(None, etmpDisplayRegistration).toFuture
          }

    } yield registrationWrapper


  }

  def amendRegistration(etmpAmendRegistrationRequest: EtmpAmendRegistrationRequest): Future[AmendRegistrationResponse] = {
    registrationConnector.amendRegistration(etmpAmendRegistrationRequest).flatMap {
      case Right(amendRegistrationResponse: AmendRegistrationResponse) =>
        logger.info(s"Successfully sent amend registration to ETMP at ${amendRegistrationResponse.processingDateTime} for IOSS number ${amendRegistrationResponse.iossReference}")
        Future.successful(amendRegistrationResponse)
      case Left(error) =>
        logger.error(s"An error occurred while amending registration ${error.getClass} ${error.body}")
        Future.failed(EtmpException(s"There was an error amending Registration from ETMP: ${error.getClass} ${error.body}"))
    }
  }
}