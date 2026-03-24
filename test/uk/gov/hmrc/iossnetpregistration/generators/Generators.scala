/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.generators

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.iossnetpregistration.models.*
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo
import uk.gov.hmrc.iossnetpregistration.models.etmp.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.*
import uk.gov.hmrc.iossnetpregistration.models.requests.SaveForLaterRequest
import uk.gov.hmrc.iossnetpregistration.services.UniqueCodeGeneratorService

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, LocalDateTime}

trait Generators {

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] = {
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county <- Gen.option(arbitrary[String])
        postCode <- arbitrary[String]
      } yield UkAddress(line1, line2, townOrCity, county, postCode)
    }
  }

  implicit val arbitraryAddress: Arbitrary[Address] = {
    Arbitrary {
      Gen.oneOf(
        arbitrary[UkAddress],
        arbitrary[InternationalAddress],
        arbitrary[DesAddress]
      )
    }
  }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] = {
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        stateOrRegion <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        country <- arbitrary[Country]
      } yield InternationalAddress(line1, line2, townOrCity, stateOrRegion, postCode, country)
    }
  }

  implicit lazy val arbitraryDesAddress: Arbitrary[DesAddress] = {
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        line3 <- Gen.option(arbitrary[String])
        line4 <- Gen.option(arbitrary[String])
        line5 <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        countryCode <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
      } yield DesAddress(line1, line2, line3, line4, line5, postCode, countryCode)
    }
  }

  implicit lazy val arbitraryCountry: Arbitrary[Country] = {
    Arbitrary {
      for {
        char1 <- Gen.alphaUpperChar
        char2 <- Gen.alphaUpperChar
        name <- arbitrary[String]
      } yield Country(s"$char1$char2", name)
    }
  }

  implicit lazy val arbitraryVrn: Arbitrary[Vrn] = {
    Arbitrary {
      for {
        chars <- Gen.listOfN(9, Gen.numChar)
      } yield Vrn(chars.mkString(""))
    }
  }

  implicit val arbitraryVatCustomerInfo: Arbitrary[VatCustomerInfo] = {
    Arbitrary {
      for {
        desAddress <- arbitraryDesAddress.arbitrary
        registrationDate <- arbitrary[LocalDate]
        organisationName <- arbitrary[String]
        individualName <- arbitrary[String]
        singleMarketIndicator <- arbitrary[Boolean]
        deregistrationDecisionDate <- arbitrary[LocalDate]
      }
      yield
        VatCustomerInfo(
          desAddress = desAddress,
          registrationDate = Some(registrationDate),
          organisationName = Some(organisationName),
          individualName = Some(individualName),
          singleMarketIndicator = singleMarketIndicator,
          deregistrationDecisionDate = Some(deregistrationDecisionDate)
        )
    }
  }

  implicit lazy val arbitraryUserAnswers: Arbitrary[UserAnswers] = {
    Arbitrary {
      for {
        id <- arbitrary[String]
        journeyId <- arbitrary[String]
        data = JsObject(Seq("test" -> Json.toJson("test")))
        vatInfo <- arbitraryVatCustomerInfo.arbitrary
        lastUpdated = Instant.now().truncatedTo(ChronoUnit.MILLIS)
      } yield {
        UserAnswers(
          id = id,
          journeyId = journeyId,
          data = data,
          vatInfo = Some(vatInfo),
          lastUpdated = lastUpdated
        )
      }
    }
  }
  
  implicit lazy val arbitraryIntermediaryDetails: Arbitrary[IntermediaryDetails] = {
    Arbitrary {
      for {
        intermediaryNumber <- arbitrary[String]
        intermediaryName <- arbitrary[String]
      } yield {
        IntermediaryDetails(
          intermediaryNumber = intermediaryNumber,
          intermediaryName = intermediaryName
        )
      }
    }
  }


  implicit lazy val arbitrarySavedPendingRegistration: Arbitrary[SavedPendingRegistration] = {
    Arbitrary {
      for {
        userAnswers <- arbitraryUserAnswers.arbitrary
        uniqueUrlCode = UniqueCodeGeneratorService().generateUniqueCode()
        uniqueActivationCode = UniqueCodeGeneratorService().generateUniqueCode()
        intermediaryNumber <- arbitrary[String]
        intermediaryName <- arbitrary[String]
      } yield {
        SavedPendingRegistration(
          journeyId = userAnswers.journeyId,
          uniqueUrlCode = uniqueUrlCode,
          userAnswers = userAnswers,
          lastUpdated = userAnswers.lastUpdated,
          uniqueActivationCode = uniqueActivationCode,
          intermediaryDetails = IntermediaryDetails(intermediaryNumber, intermediaryName)
        )
      }
    }
  }

  implicit lazy val arbitraryEncryptedPendingRegistrationAnswers: Arbitrary[EncryptedSavedPendingRegistration] = {
    Arbitrary {
      for {
        data <- arbitrarySavedPendingRegistration.arbitrary
      } yield
        EncryptedSavedPendingRegistration(
          journeyId = data.journeyId,
          uniqueUrlCode = data.uniqueUrlCode,
          data = data.toString,
          lastUpdated = data.lastUpdated,
          uniqueActivationCode = data.uniqueActivationCode,
          intermediaryDetails = data.intermediaryDetails
        )
    }
  }

  implicit lazy val arbitraryVatNumberTraderId: Arbitrary[VatNumberTraderId] =
    Arbitrary {
      for {
        vatNumber <- Gen.alphaNumStr
      } yield VatNumberTraderId(vatNumber)
    }

  implicit lazy val arbitraryTaxRefTraderID: Arbitrary[TaxRefTraderID] =
    Arbitrary {
      for {
        taxReferenceNumber <- Gen.alphaNumStr
      } yield TaxRefTraderID(taxReferenceNumber)
    }

  implicit lazy val arbitraryEtmpTradingName: Arbitrary[EtmpTradingName] =
    Arbitrary {
      for {
        tradingName <- Gen.alphaStr
      } yield EtmpTradingName(tradingName)
    }

  implicit lazy val arbitraryCustomerIdentification: Arbitrary[EtmpCustomerIdentification] =
    Arbitrary {
      for {
        vrn <- arbitraryVrn.arbitrary
        etmpIdType <- Gen.oneOf(EtmpIdType.values)
        intermediaryDetails <- arbitraryIntermediaryDetails.arbitrary
      } yield EtmpCustomerIdentification(etmpIdType, vrn.vrn, intermediaryDetails.intermediaryNumber)
    }

  implicit lazy val arbitraryEtmpCustomerIdentification: Arbitrary[EtmpDisplayCustomerIdentification] =
    Arbitrary {
      for {
        etmpIdType <- Gen.oneOf(EtmpIdType.values)
        vrn <- Gen.alphaStr
      } yield EtmpDisplayCustomerIdentification(etmpIdType, vrn)
    }

  implicit lazy val arbitraryEtmpAdministration: Arbitrary[EtmpAdministration] =
    Arbitrary {
      for {
        messageType <- Gen.oneOf(EtmpMessageType.values)
      } yield EtmpAdministration(messageType, "IOSS")
    }

  implicit lazy val arbitrarySchemeType: Arbitrary[SchemeType] =
    Arbitrary {
      Gen.oneOf(SchemeType.values)
    }

  implicit lazy val arbitraryWebsite: Arbitrary[EtmpWebsite] =
    Arbitrary {
      for {
        websiteAddress <- Gen.alphaStr
      } yield EtmpWebsite(websiteAddress)
    }

  implicit lazy val arbitraryEtmpOtherIossIntermediaryRegistrations: Arbitrary[EtmpOtherIossIntermediaryRegistrations] =
    Arbitrary {
      for {
        countryCode <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
        intermediaryNumber <- Gen.listOfN(12, Gen.alphaChar).map(_.mkString)
      } yield EtmpOtherIossIntermediaryRegistrations(countryCode, intermediaryNumber)
    }

  implicit lazy val arbitraryEtmpIntermediaryDetails: Arbitrary[EtmpIntermediaryDetails] =
    Arbitrary {
      for {
        amountOfOtherRegistrations <- Gen.chooseNum(1, 5)
        otherRegistrationDetails <- Gen.listOfN(amountOfOtherRegistrations, arbitraryEtmpOtherIossIntermediaryRegistrations.arbitrary)
      } yield EtmpIntermediaryDetails(otherRegistrationDetails)
    }

  implicit lazy val arbitraryEtmpOtherAddress: Arbitrary[EtmpOtherAddress] =
    Arbitrary {
      for {
        issuedBy <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
        tradingName <- Gen.listOfN(20, Gen.alphaChar).map(_.mkString)
        addressLine1 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        addressLine2 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        townOrCity <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        regionOrState <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        postcode <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
      } yield EtmpOtherAddress(
        issuedBy,
        Some(tradingName),
        addressLine1,
        Some(addressLine2),
        townOrCity,
        Some(regionOrState),
        Some(postcode)
      )
    }

  implicit lazy val arbitraryBic: Arbitrary[Bic] = {
    val asciiCodeForA = 65
    val asciiCodeForN = 78
    val asciiCodeForP = 80
    val asciiCodeForZ = 90

    Arbitrary {
      for {
        firstChars <- Gen.listOfN(6, Gen.alphaUpperChar).map(_.mkString)
        char7 <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9)
        )
        lastChars <- Gen.option(Gen.listOfN(3, Gen.oneOf(Gen.alphaUpperChar, Gen.numChar)).map(_.mkString))
      } yield Bic(s"$firstChars$char7$char8${lastChars.getOrElse("")}").get
    }
  }

  implicit lazy val arbitraryIban: Arbitrary[Iban] =
    Arbitrary {
      Gen.oneOf(
        "GB94BARC10201530093459",
        "GB33BUKB20201555555555",
        "DE29100100100987654321",
        "GB24BKEN10000031510604",
        "GB27BOFI90212729823529",
        "GB17BOFS80055100813796",
        "GB92BARC20005275849855",
        "GB66CITI18500812098709",
        "GB15CLYD82663220400952",
        "GB26MIDL40051512345674",
        "GB76LOYD30949301273801",
        "GB25NWBK60080600724890",
        "GB60NAIA07011610909132",
        "GB29RBOS83040210126939",
        "GB79ABBY09012603367219",
        "GB21SCBL60910417068859",
        "GB42CPBK08005470328725"
      ).map(v => Iban(v).toOption.get)
    }

  implicit lazy val genIntermediaryNumber: Gen[String] = {
    for {
      intermediaryNumber <- Gen.listOfN(12, Gen.alphaChar).map(_.mkString)
    } yield intermediaryNumber
  }

  implicit lazy val genVatNumber: Gen[String] = {
    for {
      vatNumber <- Gen.alphaNumStr
    } yield vatNumber
  }

  implicit lazy val genTaxReference: Gen[String] = {
    for {
      taxReferenceNumber <- Gen.alphaNumStr
    } yield taxReferenceNumber
  }

  implicit lazy val arbitraryEtmpAdminUse: Arbitrary[EtmpAdminUse] = {
    Arbitrary {
      for {
        changeDate <- Gen.option(arbitrary[LocalDateTime])
      } yield EtmpAdminUse(changeDate = changeDate)
    }
  }

  implicit lazy val arbitraryEtmpDisplayEuRegistrationDetails: Arbitrary[EtmpDisplayEuRegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        vatNumber <- genVatNumber
        taxIdentificationNumber <- genTaxReference
        fixedEstablishmentTradingName <- arbitraryEtmpTradingName.arbitrary.map(_.tradingName)
        fixedEstablishmentAddressDetails <- arbitraryInternationalAddress.arbitrary
      } yield {
        EtmpDisplayEuRegistrationDetails(
          issuedBy = issuedBy,
          vatNumber = Some(vatNumber),
          taxIdentificationNumber = Some(taxIdentificationNumber),
          fixedEstablishmentTradingName = fixedEstablishmentTradingName,
          fixedEstablishmentAddressLine1 = fixedEstablishmentAddressDetails.line1,
          fixedEstablishmentAddressLine2 = fixedEstablishmentAddressDetails.line2,
          townOrCity = fixedEstablishmentAddressDetails.townOrCity,
          regionOrState = fixedEstablishmentAddressDetails.stateOrRegion,
          postcode = fixedEstablishmentAddressDetails.postCode
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpExclusion: Arbitrary[EtmpExclusion] = {
    Arbitrary {
      for {
        exclusionReason <- Gen.oneOf(EtmpExclusionReason.values)
        effectiveDate <- arbitrary[LocalDate]
        decisionDate <- arbitrary[LocalDate]
        quarantine <- arbitrary[Boolean]
      } yield {
        EtmpExclusion(
          exclusionReason = exclusionReason,
          effectiveDate = effectiveDate,
          decisionDate = decisionDate,
          quarantine = quarantine
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpPreviousEuRegistrationDetails: Arbitrary[EtmpPreviousEuRegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        registrationNumber <- arbitrary[String]
        schemeType <- Gen.oneOf(SchemeType.values)
        intermediaryNumber <- genIntermediaryNumber
      } yield {
        EtmpPreviousEuRegistrationDetails(
          issuedBy = issuedBy,
          registrationNumber = registrationNumber,
          schemeType = schemeType,
          intermediaryNumber = Some(intermediaryNumber)
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpDisplaySchemeDetails: Arbitrary[EtmpDisplaySchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[LocalDate].map(_.toString)
        euRegistrationDetails <- Gen.listOfN(3, arbitraryEtmpDisplayEuRegistrationDetails.arbitrary)
        contactName <- Gen.alphaStr
        businessTelephoneNumber <- Gen.alphaNumStr
        businessEmailId <- Gen.alphaStr
        unusableStatus <- arbitrary[Boolean]
        nonCompliant <- Gen.oneOf("1", "2")
        previousEURegistrationDetails <- Gen.listOfN(3, arbitraryEtmpPreviousEuRegistrationDetails.arbitrary)
        websites <- Gen.listOfN(3, arbitraryWebsite.arbitrary)
      } yield {
        EtmpDisplaySchemeDetails(
          commencementDate = commencementDate,
          euRegistrationDetails = euRegistrationDetails,
          contactName = contactName,
          businessTelephoneNumber = businessTelephoneNumber,
          businessEmailId = businessEmailId,
          unusableStatus = unusableStatus,
          nonCompliantReturns = Some(nonCompliant),
          nonCompliantPayments = Some(nonCompliant),
          previousEURegistrationDetails = previousEURegistrationDetails,
          websites = websites
        )
      }
    }
  }


  implicit lazy val arbitraryEtmpBankDetails: Arbitrary[EtmpBankDetails] = {
    Arbitrary {
      for {
        accountName <- arbitrary[String]
        bic <- arbitraryBic.arbitrary
        iban <- arbitraryIban.arbitrary
      } yield {
        EtmpBankDetails(
          accountName = accountName,
          bic = Some(bic),
          iban = iban
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpDisplayRegistration: Arbitrary[EtmpDisplayRegistration] =
    Arbitrary {
      for {
        customerIdentification <- arbitraryEtmpCustomerIdentification.arbitrary
        tradingNames <- Gen.listOfN(3, arbitraryEtmpTradingName.arbitrary)
        otherAddress <- arbitraryEtmpOtherAddress.arbitrary
        schemeDetails <- arbitraryEtmpDisplaySchemeDetails.arbitrary
        exclusions <- Gen.listOfN(1, arbitraryEtmpExclusion.arbitrary)
        adminUse <- arbitraryEtmpAdminUse.arbitrary
      } yield {
        EtmpDisplayRegistration(
          customerIdentification = customerIdentification,
          tradingNames = tradingNames,
          otherAddress = Some(otherAddress),
          schemeDetails = schemeDetails,
          exclusions = exclusions,
          adminUse = adminUse
        )
      }
    }


  implicit lazy val arbitraryRegistrationWrapper: Arbitrary[RegistrationWrapper] =
    Arbitrary {
      for {
        vatCustomerInfo <- arbitraryVatCustomerInfo.arbitrary
        etmpDisplayRegistration <- arbitraryEtmpDisplayRegistration.arbitrary
      } yield {
        RegistrationWrapper(Some(vatCustomerInfo), etmpDisplayRegistration)
      }
    }

  implicit lazy val arbitrarySavedUserAnswers: Arbitrary[SavedUserAnswers] = {
    Arbitrary {
      for {
        journeyId <- arbitrary[String]
        data <- Gen.const(Json.obj("test" -> "value", "businessName" -> "Generated Business"))
        intermediaryNumber <- arbitrary[String]
        lastUpdated = Instant.now().truncatedTo(ChronoUnit.MILLIS)
      } yield {
        SavedUserAnswers(
          journeyId = journeyId,
          data = data,
          intermediaryNumber = intermediaryNumber,
          lastUpdated = lastUpdated
        )
      }
    }
  }

  implicit lazy val arbitraryEncryptedSavedUserAnswers: Arbitrary[EncryptedSavedUserAnswers] = {
    Arbitrary {
      for {
        journeyId <- arbitrary[String]
        data <- arbitrary[String]
        intermediaryNumber <- arbitrary[String]
        lastUpdated = Instant.now().truncatedTo(ChronoUnit.MILLIS)
      } yield {
        EncryptedSavedUserAnswers(
          journeyId = journeyId,
          data = data,
          intermediaryNumber = intermediaryNumber,
          lastUpdated = lastUpdated
        )
      }
    }
  }
  implicit lazy val arbitrarySaveForLaterRequest: Arbitrary[SaveForLaterRequest] = {
    Arbitrary {
      for {
        journeyId <- arbitrary[String]
        data <- Gen.const(Json.obj("businessName" -> "Generated Business", "field" -> "value"))
        intermediaryNumber <- arbitrary[String]
      } yield {
        SaveForLaterRequest(
          journeyId = journeyId,
          data = data,
          intermediaryNumber = intermediaryNumber
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpAmendRegistrationChangeLog: Arbitrary[EtmpAmendRegistrationChangeLog] =
    Arbitrary {
      for {
        tradingNames <- arbitrary[Boolean]
        fixedEstablishments <- arbitrary[Boolean]
        contactDetails <- arbitrary[Boolean]
        bankDetails <- arbitrary[Boolean]
        reRegistration <- arbitrary[Boolean]
        otherAddress <- arbitrary[Boolean]
      } yield EtmpAmendRegistrationChangeLog(
        tradingNames = tradingNames,
        fixedEstablishments = fixedEstablishments,
        contactDetails = contactDetails,
        bankDetails = bankDetails,
        reRegistration = reRegistration,
        otherAddress = otherAddress
      )
    }

  implicit lazy val arbitraryEtmpAmendCustomerIdentification: Arbitrary[EtmpAmendCustomerIdentification] =
    Arbitrary {
      for {
        iossNumber <- Gen.alphaNumStr.suchThat(_.nonEmpty)
        foreignTaxReference <- Gen.option(Gen.alphaNumStr.suchThat(_.nonEmpty))
      } yield EtmpAmendCustomerIdentification(
        iossNumber = s"IN$iossNumber",
        foreignTaxReference = foreignTaxReference
      )
    }


}
