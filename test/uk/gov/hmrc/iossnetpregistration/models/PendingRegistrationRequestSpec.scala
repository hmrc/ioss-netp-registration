package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class PendingRegistrationRequestSpec extends BaseSpec {

  private val savedPendingRegistration: SavedPendingRegistration  =
    arbitrarySavedPendingRegistration.arbitrary.sample.value

  "PendingRegistrationRequest" - {

    "must serialise/deserialise to and from PendingRegistrationRequest" in {

      val json = Json.obj(
        "userAnswers" -> savedPendingRegistration.userAnswers,
        "intermediaryDetails" -> savedPendingRegistration.intermediaryDetails
      )

      val expectedResult: PendingRegistrationRequest = PendingRegistrationRequest(
        userAnswers = savedPendingRegistration.userAnswers,
        intermediaryDetails = savedPendingRegistration.intermediaryDetails
      )

      json.validate[PendingRegistrationRequest] `mustBe` JsSuccess(expectedResult)
      Json.toJson(expectedResult) `mustBe` json
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PendingRegistrationRequest] `mustBe` a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "userAnswers" -> 1234567890,
        "intermediaryDetails" -> savedPendingRegistration.intermediaryDetails
      )

      json.validate[PendingRegistrationRequest] `mustBe` a[JsError]
    }
  }
}
