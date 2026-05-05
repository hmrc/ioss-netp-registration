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

package uk.gov.hmrc.iossnetpregistration.config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class AppConfig @Inject()(config: Configuration) {

  val appName: String = config.get[String]("appName")

  val pendingRegistrationStatusTtl: Long = config.get[Long]("mongodb.timeToLiveInDays")
  val encryptionKey: String = config.get[String]("mongodb.encryption.key")

  val registrationStatusTtl: Long = config.get[Long]("mongodb.timeToLiveInHours")

  val maxRetryCount: Int = config.get[Int]("features.maxRetryCount")
  val delay: Int = config.get[Int]("features.delay")
  
  val intermediaryEnrolmentName: String = config.get[String]("features.enrolments.keys.intermediary.name")
  val intermediaryEnrolmentKey: String = config.get[String]("features.enrolments.keys.intermediary.key")
  val netpEnrolmentName: String = config.get[String]("features.enrolments.keys.netp.name")
  val netpEnrolmentKey: String = config.get[String]("features.enrolments.keys.netp.key")

  val saveForLaterTtl: Long = config.get[Long]("mongodb.savedRepositoryTimeToLiveInDays")
}
