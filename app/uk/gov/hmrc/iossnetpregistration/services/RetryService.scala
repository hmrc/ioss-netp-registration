/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.services

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import uk.gov.hmrc.iossnetpregistration.models.etmp.EtmpRegistrationStatus
import uk.gov.hmrc.iossnetpregistration.repositories.RegistrationStatusRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.*

@Singleton
class RetryService @Inject()(
                              registrationStatusRepository: RegistrationStatusRepository,
                              actorSystem: ActorSystem
                            )(implicit ec: ExecutionContext) {

  def getEtmpRegistrationStatus(remaining: Int, delay: Int, subscriptionId: String): Future[EtmpRegistrationStatus] = {

    if (remaining > 0) {
      registrationStatusRepository.get(subscriptionId).flatMap {
        case Some(registrationStatus) => registrationStatus.status match {

          case EtmpRegistrationStatus.Success => Future(EtmpRegistrationStatus.Success)

          case EtmpRegistrationStatus.Pending => if (remaining == 1) {
            Future(EtmpRegistrationStatus.Error)
          } else {
            after(delay.milliseconds, actorSystem.scheduler) {
              getEtmpRegistrationStatus(remaining - 1, delay, subscriptionId)
            }
          }
          case _ => Future(EtmpRegistrationStatus.Error)
        }
        case _ => Future(EtmpRegistrationStatus.Error)
      }
    } else {
      Future(EtmpRegistrationStatus.Error)
    }
  }

}