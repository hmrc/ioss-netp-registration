package uk.gov.hmrc.iossnetpregistration.testutils

import org.scalacheck.Arbitrary.arbitrary
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.models.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.*

import java.time.{LocalDate, LocalDateTime}

object RegistrationData extends BaseSpec {

  val etmpEuRegistrationDetails: EtmpEuRegistrationDetails = EtmpEuRegistrationDetails(
    countryOfRegistration = arbitrary[Country].sample.value.code,
    traderId = arbitraryVatNumberTraderId.arbitrary.sample.value,
    tradingName = arbitraryEtmpTradingName.arbitrary.sample.value.tradingName,
    fixedEstablishmentAddressLine1 = arbitrary[String].sample.value,
    fixedEstablishmentAddressLine2 = Some(arbitrary[String].sample.value),
    townOrCity = arbitrary[String].sample.value,
    regionOrState = Some(arbitrary[String].sample.value),
    postcode = Some(arbitrary[String].sample.value)
  )

  val etmpEuPreviousRegistrationDetails: EtmpPreviousEuRegistrationDetails = EtmpPreviousEuRegistrationDetails(
    issuedBy = arbitrary[Country].sample.value.code,
    registrationNumber = arbitrary[String].sample.value,
    schemeType = arbitrary[SchemeType].sample.value,
    intermediaryNumber = Some(arbitrary[String].sample.value)
  )

  val etmpSchemeDetails: EtmpSchemeDetails = EtmpSchemeDetails(
    commencementDate = LocalDate.now.format(dateFormatter),
    euRegistrationDetails = Seq(etmpEuRegistrationDetails),
    previousEURegistrationDetails = Seq(etmpEuPreviousRegistrationDetails),
    websites = None,
    contactName = arbitrary[String].sample.value,
    businessTelephoneNumber = arbitrary[String].sample.value,
    businessEmailId = arbitrary[String].sample.value,
    nonCompliantReturns = Some(arbitrary[String].sample.value),
    nonCompliantPayments = Some(arbitrary[String].sample.value)
  )

  val etmpBankDetails: EtmpBankDetails = EtmpBankDetails(
    accountName = arbitrary[String].sample.value,
    bic = Some(arbitrary[Bic].sample.value),
    iban = arbitrary[Iban].sample.value
  )

  val etmpRegistrationRequest: EtmpRegistrationRequest = EtmpRegistrationRequest(
    administration = arbitrary[EtmpAdministration].sample.value,
    customerIdentification = arbitrary[EtmpCustomerIdentification].sample.value,
    tradingNames = Seq(arbitrary[EtmpTradingName].sample.value),
    schemeDetails = etmpSchemeDetails,
    bankDetails = Some(etmpBankDetails),
    intermediaryDetails = Some(arbitrary[EtmpIntermediaryDetails].sample.value),
    otherAddress = Some(arbitrary[EtmpOtherAddress].sample.value)
  )

  val etmpAmendRegistrationRequest: EtmpAmendRegistrationRequest = EtmpAmendRegistrationRequest(
    administration = arbitrary[EtmpAdministration].sample.value,
    changeLog = arbitrary[EtmpAmendRegistrationChangeLog].sample.value,
    exclusionDetails = None,
    customerIdentification = arbitrary[EtmpAmendCustomerIdentification].sample.value,
    tradingNames = Seq(arbitrary[EtmpTradingName].sample.value),
    otherAddress = arbitrary[EtmpOtherAddress].sample,
    schemeDetails = etmpSchemeDetails
  )

  val adminUse: EtmpAdminUse = EtmpAdminUse(Some(LocalDateTime.now))

  val invalidRegistration = """{"invalidName":"invalid"}"""
}
