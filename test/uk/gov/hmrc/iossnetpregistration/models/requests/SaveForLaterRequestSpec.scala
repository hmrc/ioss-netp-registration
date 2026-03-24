package uk.gov.hmrc.iossnetpregistration.models.requests

import play.api.libs.json.Json
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class SaveForLaterRequestSpec extends BaseSpec {

  "SaveForLaterRequest" - {

    "must serialize to JSON correctly" in {
      val request = SaveForLaterRequest(
        journeyId = "test-journey-123",
        data = Json.obj(
          "businessName" -> "Test Business Ltd",
          "registrationDate" -> "2024-01-15",
          "address" -> Json.obj(
            "line1" -> "123 Test Street",
            "postcode" -> "AB12 3CD"
          )
        ),
        intermediaryNumber = "INT123456"
      )

      val expectedJson = Json.obj(
        "journeyId" -> "test-journey-123",
        "data" -> Json.obj(
          "businessName" -> "Test Business Ltd",
          "registrationDate" -> "2024-01-15",
          "address" -> Json.obj(
            "line1" -> "123 Test Street",
            "postcode" -> "AB12 3CD"
          )
        ),
        "intermediaryNumber" -> "INT123456"
      )

      Json.toJson(request) `mustBe` expectedJson
    }

    "must deserialize from JSON correctly" in {
      val json = Json.obj(
        "journeyId" -> "test-journey-456",
        "data" -> Json.obj(
          "companyName" -> "Another Business",
          "vatNumber" -> "GB123456789"
        ),
        "intermediaryNumber" -> "INT789012"
      )

      val expectedRequest = SaveForLaterRequest(
        journeyId = "test-journey-456",
        data = Json.obj(
          "companyName" -> "Another Business",
          "vatNumber" -> "GB123456789"
        ),
        intermediaryNumber = "INT789012"
      )

      json.as[SaveForLaterRequest] `mustBe` expectedRequest
    }

    "must round-trip correctly" in {
      val originalRequest = SaveForLaterRequest(
        journeyId = "round-trip-test",
        data = Json.obj(
          "nested" -> Json.obj(
            "field1" -> "value1",
            "field2" -> 42,
            "field3" -> true
          )
        ),
        intermediaryNumber = "INT999888"
      )

      val json = Json.toJson(originalRequest)
      val deserializedRequest = json.as[SaveForLaterRequest]

      deserializedRequest `mustBe` originalRequest
    }
  }
}