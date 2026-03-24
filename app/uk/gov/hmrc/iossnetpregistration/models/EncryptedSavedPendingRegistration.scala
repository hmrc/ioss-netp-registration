/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.*
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class EncryptedSavedPendingRegistration(
                                              journeyId: String,
                                              uniqueUrlCode: String,
                                              data: String,
                                              lastUpdated: Instant = Instant.now,
                                              uniqueActivationCode: String,
                                              intermediaryDetails: IntermediaryDetails
                                            )

object EncryptedSavedPendingRegistration {

  val reads: Reads[EncryptedSavedPendingRegistration] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "journeyId").read[String] and
        (__ \ "uniqueUrlCode").read[String] and
        (__ \ "data").read[String] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat) and
        (__ \ "uniqueActivationCode").read[String] and
        (__ \ "intermediaryDetails").read[IntermediaryDetails]
      )(EncryptedSavedPendingRegistration.apply _)
  }

  val writes: OWrites[EncryptedSavedPendingRegistration] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "journeyId").write[String] and
        (__ \ "uniqueUrlCode").write[String] and
        (__ \ "data").write[String] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat) and
        (__ \ "uniqueActivationCode").write[String] and
        (__ \ "intermediaryDetails").write[IntermediaryDetails]
      )(encryptedSavedPendingRegistration => Tuple.fromProductTyped(encryptedSavedPendingRegistration))
  }

  implicit lazy val format: OFormat[EncryptedSavedPendingRegistration] = OFormat(reads, writes)
}
