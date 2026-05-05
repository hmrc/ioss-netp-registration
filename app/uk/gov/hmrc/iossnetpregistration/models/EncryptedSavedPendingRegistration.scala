/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
