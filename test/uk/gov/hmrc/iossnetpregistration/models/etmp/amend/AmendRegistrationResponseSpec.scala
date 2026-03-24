/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.amend

import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

import java.time.LocalDateTime

class AmendRegistrationResponseSpec extends BaseSpec with Matchers {

  private val processingDateTime = LocalDateTime.of(2023, 12, 15, 10, 30, 45)
  private val formBundleNumber = "123456789"
  private val intermediary = "IN900123456"
  private val businessPartner = "Test Business Partner"

  "AmendRegistrationResponse" - {

    "must deserialise/serialise to and from AmendRegistrationResponse" in {

      val json = Json.obj(
        "processingDateTime" -> processingDateTime,
        "formBundleNumber" -> formBundleNumber,
        "iossReference" -> intermediary,
        "businessPartner" -> businessPartner
      )

      val expectedResult = AmendRegistrationResponse(
        processingDateTime = processingDateTime,
        formBundleNumber = formBundleNumber,
        iossReference = intermediary,
        businessPartner = businessPartner
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[AmendRegistrationResponse] mustBe JsSuccess(expectedResult)
    }
  }
}
