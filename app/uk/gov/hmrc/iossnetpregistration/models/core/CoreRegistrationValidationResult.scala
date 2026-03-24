/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.core

import play.api.libs.json.{Json, OFormat}

import java.time.format.DateTimeFormatter

case class CoreRegistrationValidationResult(
                                             searchId: String,
                                             searchIdIntermediary: Option[String],
                                             searchIdIssuedBy: String,
                                             traderFound: Boolean,
                                             matches: Seq[Match]
                                           )

object CoreRegistrationValidationResult {

  implicit val format: OFormat[CoreRegistrationValidationResult] = Json.format[CoreRegistrationValidationResult]

}


case class Match(
                  traderId: String,
                  intermediary: Option[String],
                  memberState: String,
                  exclusionStatusCode: Option[Int],
                  exclusionDecisionDate: Option[String],
                  exclusionEffectiveDate: Option[String],
                  nonCompliantReturns: Option[Int],
                  nonCompliantPayments: Option[Int]
                )

object Match {

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy MM dd")

  implicit val format: OFormat[Match] = Json.format[Match]

}