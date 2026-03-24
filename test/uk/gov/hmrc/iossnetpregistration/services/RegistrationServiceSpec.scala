package uk.gov.hmrc.iossnetpregistration.services

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.connectors.{GetVatInfoConnector, RegistrationConnector}
import uk.gov.hmrc.iossnetpregistration.models.*
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo
import uk.gov.hmrc.iossnetpregistration.models.etmp.EtmpIdType.{UTR, VRN}
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.{EtmpDisplayCustomerIdentification, EtmpDisplayRegistration, RegistrationWrapper}
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.AmendRegistrationResponse
import uk.gov.hmrc.iossnetpregistration.models.etmp.responses.EtmpEnrolmentResponse
import uk.gov.hmrc.iossnetpregistration.models.responses.{EtmpException, NotFound, ServerError}
import uk.gov.hmrc.iossnetpregistration.testutils.RegistrationData.{etmpAmendRegistrationRequest, etmpRegistrationRequest}
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec extends BaseSpec with BeforeAndAfterEach {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val mockVatInfoConnector: GetVatInfoConnector = mock[GetVatInfoConnector]
  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val registrationService = new RegistrationService(mockRegistrationConnector, mockVatInfoConnector)

  override def beforeEach(): Unit = {
    reset(mockRegistrationConnector)
    reset(mockVatInfoConnector)
  }

  ".createRegistration" - {

    "must create registration request and return a successful ETMP enrolment response" in {

      val etmpEnrolmentResponse =
        EtmpEnrolmentResponse(
          processingDateTime = LocalDateTime.now(stubClock),
          formBundleNumber = "123456789",
          iossReference = "test",
          businessPartner = "test businessPartner"
        )

      when(mockRegistrationConnector.createRegistration(etmpRegistrationRequest)) thenReturn Right(etmpEnrolmentResponse).toFuture

      val app = applicationBuilder()
        .build()

      running(app) {

        registrationService.createRegistration(etmpRegistrationRequest).futureValue mustBe Right(etmpEnrolmentResponse)
        verify(mockRegistrationConnector, times(1)).createRegistration(eqTo(etmpRegistrationRequest))
      }
    }
  }

  ".amendRegistration" - {
    "must amend registration request and return a successful ETMP amend response" in {
      val amendRegistrationResponse = AmendRegistrationResponse(
        processingDateTime = LocalDateTime.now(stubClock),
        formBundleNumber = "123456789",
        iossReference = "test",
        businessPartner = "test businesspartner"
      )
      
      when(mockRegistrationConnector.amendRegistration(etmpAmendRegistrationRequest))  thenReturn Right(amendRegistrationResponse).toFuture

      val app = applicationBuilder()
        .build()

      running(app) {
        registrationService.amendRegistration(etmpAmendRegistrationRequest).futureValue mustBe amendRegistrationResponse
        verify(mockRegistrationConnector, times(1)).amendRegistration(eqTo(etmpAmendRegistrationRequest))
      }
    }

    "must throw EtmpException when connector returns an error" in {
      when(mockRegistrationConnector.amendRegistration(etmpAmendRegistrationRequest)) thenReturn Left(NotFound).toFuture

      val app = applicationBuilder()
        .build()

      running(app) {
        val exception = registrationService.amendRegistration(etmpAmendRegistrationRequest).failed.futureValue
        exception mustBe a[EtmpException]
        verify(mockRegistrationConnector, times(1)).amendRegistration(eqTo(etmpAmendRegistrationRequest))
      }
    }
  }

  ".getRegistration" - {

    val vatCustomerInfo: VatCustomerInfo = arbitraryVatCustomerInfo.arbitrary.sample.value
    val etmpDisplayRegistrationWithoutVRN: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(customerIdentification = EtmpDisplayCustomerIdentification(UTR, "UTR_NUM_1"))
    val etmpDisplayRegistrationWithVRN: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value.copy(customerIdentification = EtmpDisplayCustomerIdentification(VRN, vrn.vrn))
    val registrationWrapperWithVat: RegistrationWrapper = RegistrationWrapper(Some(vatCustomerInfo), etmpDisplayRegistrationWithVRN)
    val registrationWrapperWithoutVat: RegistrationWrapper = RegistrationWrapper(None, etmpDisplayRegistrationWithoutVRN)

    "when a NETP has a VRN must return a Registration Wrapper payload when both VatCustomerInfo and EtmpDisplayRegistration are successfully retrieved from ETMP" in {

      when(mockRegistrationConnector.getRegistration(any())).thenReturn(Right(etmpDisplayRegistrationWithVRN).toFuture)
      when(mockVatInfoConnector.getVatCustomerDetails(any())(any())).thenReturn(Right(vatCustomerInfo).toFuture)

      val result = registrationService.getRegistration(iossNumber).futureValue

      result `mustBe` registrationWrapperWithVat
      verify(mockRegistrationConnector, times(1)).getRegistration(eqTo(iossNumber))
      verify(mockVatInfoConnector, times(1)).getVatCustomerDetails(eqTo(vrn))(any())
    }

    "when a NETP does NOT have a VRN must return a Registration Wrapper payload with No VatCustomerInfo and EtmpDisplayRegistration successfully retrieved from ETMP" in {

      when(mockRegistrationConnector.getRegistration(any())).thenReturn(Right(etmpDisplayRegistrationWithoutVRN).toFuture)

      val result = registrationService.getRegistration(iossNumber).futureValue

      result `mustBe` registrationWrapperWithoutVat
      verify(mockRegistrationConnector, times(1)).getRegistration(eqTo(iossNumber))
      verify(mockVatInfoConnector, times(0)).getVatCustomerDetails(any())(any())
    }

    "must throw an ETMPException when vatCustomerInfo cannot be retrieved from ETMP and getRegistration succeeded" in {

      when(mockRegistrationConnector.getRegistration(any())).thenReturn(Right(etmpDisplayRegistrationWithVRN).toFuture)
      when(mockVatInfoConnector.getVatCustomerDetails(any())(any())).thenReturn(Left(ServerError).toFuture)

      val errorMessage: String = s"There was an error retrieving vatCustomerInfo from ETMP " +
        s"with errors: ${ServerError.body}."

      val result = registrationService.getRegistration(iossNumber).failed

      whenReady(result) { exp =>
        exp `mustBe` a[EtmpException]
        exp.getMessage `mustBe` errorMessage
      }
      verify(mockRegistrationConnector, times(1)).getRegistration(eqTo(iossNumber))
      verify(mockVatInfoConnector, times(1)).getVatCustomerDetails(eqTo(vrn))(any())
    }

    "must throw an ETMPException when etmpDisplayRegistration cannot be retrieved from ETMP and not call getVatCustomerDetails" in {

      when(mockRegistrationConnector.getRegistration(any())).thenReturn(Left(NotFound).toFuture)

      val errorMessage: String = s"There was an error retrieving etmpDisplayRegistration from ETMP with errors: ${NotFound.body}."

      val result = registrationService.getRegistration(iossNumber).failed

      whenReady(result) { exp =>
        exp `mustBe` a[EtmpException]
        exp.getMessage `mustBe` errorMessage
      }
      verify(mockRegistrationConnector, times(1)).getRegistration(eqTo(iossNumber))
      verify(mockVatInfoConnector, times(0)).getVatCustomerDetails(any())(any())
    }
  }

}
