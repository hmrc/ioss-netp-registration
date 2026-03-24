/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.logging.Logging
import uk.gov.hmrc.iossnetpregistration.models.SavedUserAnswers
import uk.gov.hmrc.iossnetpregistration.models.requests.SaveForLaterRequest
import uk.gov.hmrc.iossnetpregistration.repositories.SaveForLaterRepository

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.Future

class SaveForLaterService @Inject()(
                                     saveForLaterRepository: SaveForLaterRepository,
                                     clock: Clock
                                   ) extends Logging {


  def saveUserAnswers(saveForLaterRequest: SaveForLaterRequest): Future[SavedUserAnswers] = {
    val savedUserAnswers: SavedUserAnswers = SavedUserAnswers(
      journeyId = saveForLaterRequest.journeyId,
      data = saveForLaterRequest.data,
      intermediaryNumber = saveForLaterRequest.intermediaryNumber,
      lastUpdated = Instant.now(clock)
    )

    saveForLaterRepository.set(savedUserAnswers)
  }

  def getSavedUserAnswers(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[SavedUserAnswers]] = {
    saveForLaterRepository.get(journeyId)
  }

  def getSavedUserAnswersSelection(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[Seq[SavedUserAnswers]] = {
    saveForLaterRepository.getSelection(intermediaryNumber)
  }

  def getCount(intermediaryNumber: String): Future[Long] = {
    saveForLaterRepository.count(intermediaryNumber)
  }
  
  def deleteSavedUserAnswers(journeyId: String): Future[Boolean] = {
    saveForLaterRepository.clear(journeyId)
  }
}

