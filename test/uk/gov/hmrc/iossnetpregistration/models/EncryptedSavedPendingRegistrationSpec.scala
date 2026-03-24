package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.{JsError, JsObject, JsSuccess, Json}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class EncryptedSavedPendingRegistrationSpec extends BaseSpec {

  private val savedPendingRegistrationAnswers: EncryptedSavedPendingRegistration =
    arbitraryEncryptedPendingRegistrationAnswers.arbitrary.sample.value

  "SavedPendingRegistrationAnswers" - {

    "must serialise/deserialise to and from SavedPendingRegistrationAnswers" in {

      val json = Json.obj(
        "journeyId" -> savedPendingRegistrationAnswers.journeyId,
        "uniqueUrlCode" -> savedPendingRegistrationAnswers.uniqueUrlCode,
        "data" -> savedPendingRegistrationAnswers.data,
        "lastUpdated" -> Json.obj(
          "$date" -> Json.obj(
            "$numberLong" -> savedPendingRegistrationAnswers.lastUpdated.toEpochMilli.toString
          )
        ),
        "uniqueActivationCode" -> savedPendingRegistrationAnswers.uniqueActivationCode,
        "intermediaryDetails" -> savedPendingRegistrationAnswers.intermediaryDetails
      )

      val expectedResult: EncryptedSavedPendingRegistration = EncryptedSavedPendingRegistration(
        journeyId = savedPendingRegistrationAnswers.journeyId,
        uniqueUrlCode = savedPendingRegistrationAnswers.uniqueUrlCode,
        data = savedPendingRegistrationAnswers.data,
        lastUpdated = savedPendingRegistrationAnswers.lastUpdated,
        uniqueActivationCode = savedPendingRegistrationAnswers.uniqueActivationCode,
        intermediaryDetails = savedPendingRegistrationAnswers.intermediaryDetails
      )

      json.validate[EncryptedSavedPendingRegistration] `mustBe` JsSuccess(expectedResult)
      Json.toJson(expectedResult) `mustBe` json
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EncryptedSavedPendingRegistration] `mustBe` a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "journeyId" -> 1234567890,
        "uniqueUrlCode" -> savedPendingRegistrationAnswers.uniqueUrlCode,
        "data" -> savedPendingRegistrationAnswers.data,
        "lastUpdated" -> savedPendingRegistrationAnswers.lastUpdated,
        "uniqueActivationCode" -> savedPendingRegistrationAnswers.uniqueActivationCode,
        "intermediaryDetails" -> savedPendingRegistrationAnswers.intermediaryDetails
      )

      json.validate[EncryptedSavedPendingRegistration] `mustBe` a[JsError]
    }
  }
}
