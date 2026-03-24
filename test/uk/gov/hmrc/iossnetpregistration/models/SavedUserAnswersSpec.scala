package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class SavedUserAnswersSpec  extends BaseSpec {

  private val savedUserAnswers: SavedUserAnswers = arbitrarySavedUserAnswers.arbitrary.sample.value

  "SavedUserAnswers" - {

    "must serialise/deserialise to and from UserAnswers" in {

        val json = Json.obj(
          "journeyId" -> savedUserAnswers.journeyId,
          "data" -> savedUserAnswers.data,
          "intermediaryNumber" -> savedUserAnswers.intermediaryNumber,
          "lastUpdated" -> savedUserAnswers.lastUpdated
        )

        val expectedAnswers: SavedUserAnswers = SavedUserAnswers(
           journeyId = savedUserAnswers.journeyId,
          data = savedUserAnswers.data,
          intermediaryNumber = savedUserAnswers.intermediaryNumber,
          lastUpdated = savedUserAnswers.lastUpdated
        )

        Json.toJson(expectedAnswers) `mustBe` json
        json.validate[SavedUserAnswers] `mustBe` JsSuccess(expectedAnswers)
      }

    "must handle missing fields during deserialisation" in {

      val json = Json.obj()

      json.validate[SavedUserAnswers] mustBe a[JsError]
    }

    "must handle invalid data during deserialisation" in {

      val json = Json.obj(
        "data" -> savedUserAnswers.data,
        "intermediaryNumber" -> savedUserAnswers.intermediaryNumber,
        "lastUpdated" -> Json.obj(
          "$date" -> Json.obj(
            "$numberLong" -> savedUserAnswers.lastUpdated.toEpochMilli.toString
          )
        )
      )


      json.validate[SavedUserAnswers] mustBe a[JsError]
    }
  }
}

