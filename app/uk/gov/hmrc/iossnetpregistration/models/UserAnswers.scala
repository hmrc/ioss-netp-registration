/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class UserAnswers(
                              id: String,
                              journeyId: String,
                              data: JsObject,
                              vatInfo: Option[VatCustomerInfo],
                              lastUpdated: Instant
                            )

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    (
      (__ \ "_id").read[String] and
        (__ \ "journeyId").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "vatInfo").readNullable[VatCustomerInfo] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    (
      (__ \ "_id").write[String] and
        (__ \ "journeyId").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "vatInfo").writeNullable[VatCustomerInfo] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(userAnswers => Tuple.fromProductTyped(userAnswers))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
