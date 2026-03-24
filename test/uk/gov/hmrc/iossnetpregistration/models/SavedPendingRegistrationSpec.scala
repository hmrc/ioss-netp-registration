package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class SavedPendingRegistrationSpec extends BaseSpec {

  private val savedPendingRegistration: SavedPendingRegistration = arbitrarySavedPendingRegistration.arbitrary.sample.value

  "SavedPendingRegistration" - {

    "must serialise/deserialise to and from a SavedPendingRegistration object" in {

      val json = Json.obj(
        "journeyId" -> savedPendingRegistration.journeyId,
        "uniqueUrlCode" -> savedPendingRegistration.uniqueUrlCode,
        "userAnswers" -> savedPendingRegistration.userAnswers,
        "lastUpdated" -> savedPendingRegistration.lastUpdated,
        "uniqueActivationCode" -> savedPendingRegistration.uniqueActivationCode,
        "intermediaryDetails" -> savedPendingRegistration.intermediaryDetails
      )

      val expectedResult: SavedPendingRegistration = SavedPendingRegistration(
        journeyId = savedPendingRegistration.journeyId,
        uniqueUrlCode = savedPendingRegistration.uniqueUrlCode,
        userAnswers = savedPendingRegistration.userAnswers,
        lastUpdated = savedPendingRegistration.lastUpdated,
        uniqueActivationCode = savedPendingRegistration.uniqueActivationCode,
        intermediaryDetails = savedPendingRegistration.intermediaryDetails
      )

      json.validate[SavedPendingRegistration] mustBe JsSuccess(expectedResult)
      Json.toJson(expectedResult) mustBe json
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[SavedPendingRegistration] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "journeyId" -> savedPendingRegistration.journeyId,
        "uniqueUrlCode" -> 12345,
        "userAnswers" -> savedPendingRegistration.userAnswers,
        "lastUpdated" -> savedPendingRegistration.lastUpdated,
        "uniqueActivationCode" -> savedPendingRegistration.uniqueActivationCode,
        "intermediaryDetails" -> savedPendingRegistration.intermediaryDetails
      )

      json.validate[SavedPendingRegistration] mustBe a[JsError]
    }
  }
}
