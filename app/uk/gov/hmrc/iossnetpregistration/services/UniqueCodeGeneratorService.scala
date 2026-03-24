/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.services

import uk.gov.hmrc.iossnetpregistration.services.UniqueCodeGeneratorService.codeSize

import scala.util.Random
import scala.util.matching.Regex

class UniqueCodeGeneratorService {

  def generateUniqueCode(): String = {
    Random.alphanumeric
      .filterNot(_.isDigit)
      .filterNot(_.isLower)
      .filterNot(Set('A', 'E', 'I', 'O', 'U'))
      .take(codeSize)
      .mkString
  }
}

object UniqueCodeGeneratorService {
  val codeSize: Int = 6

  private val codePattern: Regex = ("\\p{Upper}" * codeSize).r

  def validate(code: String): Boolean = codePattern.matches(code)
}
