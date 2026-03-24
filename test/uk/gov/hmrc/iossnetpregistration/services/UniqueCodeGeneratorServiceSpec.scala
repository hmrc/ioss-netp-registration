package uk.gov.hmrc.iossnetpregistration.services

import uk.gov.hmrc.iossnetpregistration.base.BaseSpec

class UniqueCodeGeneratorServiceSpec extends BaseSpec {

  private val testRegex: String = "[^a-zAEIOU]{6}$"

  "UniqueCodeGeneratorService" - {

    "must generate a random 6 letter code containing no vowels" in {

      val service = UniqueCodeGeneratorService()

      val result = service.generateUniqueCode()

      result.toUpperCase must fullyMatch regex testRegex
    }
  }
}
