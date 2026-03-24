/*
 * Copyright 2026 HM Revenue & Customs
 *
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
