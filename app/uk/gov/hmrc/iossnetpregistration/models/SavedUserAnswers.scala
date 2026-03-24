/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models

import play.api.libs.json.*
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class SavedUserAnswers(
                             journeyId: String,
                             data: JsObject,
                             intermediaryNumber: String,
                             lastUpdated: Instant
                           )

object SavedUserAnswers {

  implicit val format: OFormat[SavedUserAnswers] = Json.format[SavedUserAnswers]
}

case class EncryptedSavedUserAnswers(
                                      journeyId: String,
                                      data: String,
                                      intermediaryNumber: String,
                                      lastUpdated: Instant
                                    )

object EncryptedSavedUserAnswers {

  val reads: Reads[EncryptedSavedUserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "journeyId").read[String] and
        (__ \ "data").read[String] and
        (__ \ "intermediaryNumber").read[String] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(EncryptedSavedUserAnswers.apply _)
  }

  val writes: OWrites[EncryptedSavedUserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "journeyId").write[String] and
        (__ \ "data").write[String] and
        (__ \ "intermediaryNumber").write[String] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(encryptedSavedUserAnswers => Tuple.fromProductTyped(encryptedSavedUserAnswers))
  }

  implicit val format: OFormat[EncryptedSavedUserAnswers] = OFormat(reads, writes)
}

