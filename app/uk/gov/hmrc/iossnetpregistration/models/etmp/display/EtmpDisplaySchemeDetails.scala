/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.etmp.display

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.{EtmpPreviousEuRegistrationDetails, EtmpWebsite}

case class EtmpDisplaySchemeDetails(
                                     commencementDate: String,
                                     euRegistrationDetails: Seq[EtmpDisplayEuRegistrationDetails],
                                     previousEURegistrationDetails: Seq[EtmpPreviousEuRegistrationDetails],
                                     contactName: String,
                                     businessTelephoneNumber: String,
                                     businessEmailId: String,
                                     unusableStatus: Boolean,
                                     nonCompliantReturns: Option[String],
                                     nonCompliantPayments: Option[String],
                                     websites: Seq[EtmpWebsite]
                                   )

object EtmpDisplaySchemeDetails {

  private def fromDisplayRegistrationPayload(
                                              commencementDate: String,
                                              euRegistrationDetails: Option[Seq[EtmpDisplayEuRegistrationDetails]],
                                              previousEURegistrationDetails: Option[Seq[EtmpPreviousEuRegistrationDetails]],
                                              contactNameOrBusinessAddress: String,
                                              businessTelephoneNumber: String,
                                              businessEmailAddress: String,
                                              unusableStatus: Boolean,
                                              nonCompliantReturns: Option[String],
                                              nonCompliantPayments: Option[String],
                                              websites: Seq[EtmpWebsite]
                                            ): EtmpDisplaySchemeDetails =
    EtmpDisplaySchemeDetails(
      commencementDate = commencementDate,
      euRegistrationDetails = euRegistrationDetails.fold(Seq.empty[EtmpDisplayEuRegistrationDetails])(a => a),
      previousEURegistrationDetails = previousEURegistrationDetails.fold(Seq.empty[EtmpPreviousEuRegistrationDetails])(a => a),
      contactName = contactNameOrBusinessAddress,
      businessTelephoneNumber = businessTelephoneNumber,
      businessEmailId = businessEmailAddress,
      unusableStatus = unusableStatus,
      nonCompliantReturns = nonCompliantReturns,
      nonCompliantPayments = nonCompliantPayments,
      websites = websites
    )

  val displaySchemeDetailsReads: Reads[EtmpDisplaySchemeDetails] = {
    (
      (__ \ "commencementDate").read[String] and
        (__ \ "euRegistrationDetails").readNullable[Seq[EtmpDisplayEuRegistrationDetails]] and
        (__ \ "previousEURegistrationDetails").readNullable[Seq[EtmpPreviousEuRegistrationDetails]] and
        (__ \ "contactDetails" \ "contactNameOrBusinessAddress").read[String] and
        (__ \ "contactDetails" \ "businessTelephoneNumber").read[String] and
        (__ \ "contactDetails" \ "businessEmailAddress").read[String] and
        (__ \ "contactDetails" \ "unusableStatus").read[Boolean] and
        (__ \ "nonCompliantReturns").readNullable[String] and
        (__ \ "nonCompliantPayments").readNullable[String] and
        (__ \ "websites").readNullable[Seq[EtmpWebsite]].map(_.getOrElse(List.empty))
      )(EtmpDisplaySchemeDetails.fromDisplayRegistrationPayload _)
  }

  implicit val format: OFormat[EtmpDisplaySchemeDetails] = Json.format[EtmpDisplaySchemeDetails]
}
