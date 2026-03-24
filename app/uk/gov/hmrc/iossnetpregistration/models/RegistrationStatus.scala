/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.EtmpRegistrationStatus
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class RegistrationStatus(
                               subscriptionId: String,
                               status: EtmpRegistrationStatus,
                               lastUpdated: Instant = Instant.now
                             )

object RegistrationStatus {

  val reads: Reads[RegistrationStatus] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "subscriptionId").read[String] and
        (__ \ "status").read[EtmpRegistrationStatus] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(RegistrationStatus.apply _)
  }

  val writes: OWrites[RegistrationStatus] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "subscriptionId").write[String] and
        (__ \ "status").write[EtmpRegistrationStatus] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(registrationStatus => Tuple.fromProductTyped(registrationStatus))
  }

  implicit val format: OFormat[RegistrationStatus] = OFormat(reads, writes)

}
