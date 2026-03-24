package uk.gov.hmrc.iossnetpregistration.services

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.models.SavedUserAnswers
import uk.gov.hmrc.iossnetpregistration.models.requests.SaveForLaterRequest
import uk.gov.hmrc.iossnetpregistration.repositories.SaveForLaterRepository
import uk.gov.hmrc.iossnetpregistration.utils.FutureSyntax.FutureOps

import java.time.{Clock, Instant, ZoneId}
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext

class SaveForLaterServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockSaveForLaterRepository: SaveForLaterRepository = mock[SaveForLaterRepository]

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    Mockito.reset(mockSaveForLaterRepository)
  }

  def saveForLaterService(clock: Clock): SaveForLaterService = {
    new SaveForLaterService(mockSaveForLaterRepository, clock)
  }

  "SaveForLaterService" - {

    ".saveUserAnswers" - {

      "must create a SavedUserAnswers, attempt to save it to the repository, and respond with the result of saving" in {

        val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
        val stubClock = Clock.fixed(now, ZoneId.systemDefault())

        val request = arbitrary[SaveForLaterRequest].sample.value

        val expectedSavedUserAnswers = SavedUserAnswers(
          journeyId = request.journeyId,
          data = request.data,
          intermediaryNumber = request.intermediaryNumber,
          lastUpdated = now
        )

        when(mockSaveForLaterRepository.set(any())) thenReturn expectedSavedUserAnswers.toFuture

        val service = saveForLaterService(stubClock)
        val result = service.saveUserAnswers(request).futureValue

        result `mustBe` expectedSavedUserAnswers
        verify(mockSaveForLaterRepository, times(1)).set(eqTo(expectedSavedUserAnswers))
      }
    }

    ".getSavedUserAnswers" - {

      "must return a SavedUserAnswers when a record exists for the corresponding journeyId" in {

        val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
        val stubClock = Clock.fixed(now, ZoneId.systemDefault())

        val savedUserAnswers = arbitrarySavedUserAnswers.arbitrary.sample.value.copy(lastUpdated = now)

        when(mockSaveForLaterRepository.get(any())) thenReturn Some(savedUserAnswers).toFuture

        val service = saveForLaterService(stubClock)
        val result = service.getSavedUserAnswers(savedUserAnswers.journeyId).futureValue

        result `mustBe` Some(savedUserAnswers)
        verify(mockSaveForLaterRepository, times(1)).get(eqTo(savedUserAnswers.journeyId))
      }

      "must return None when no record exists for a given journeyId" in {

        val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
        val stubClock = Clock.fixed(now, ZoneId.systemDefault())

        val nonExistentJourneyId = arbitrary[String].sample.value

        when(mockSaveForLaterRepository.get(any())) thenReturn None.toFuture

        val service = saveForLaterService(stubClock)
        val result = service.getSavedUserAnswers(nonExistentJourneyId).futureValue

        result must not be defined
        verify(mockSaveForLaterRepository, times(1)).get(eqTo(nonExistentJourneyId))
      }
    }

    ".getSavedUserAnswersSelection" - {

      "must return a sequence of SavedUserAnswers for the given intermediary number" in {

        val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
        val stubClock = Clock.fixed(now, ZoneId.systemDefault())

        val intermediaryNumber = arbitrary[String].sample.value
        val savedUserAnswers1 = arbitrarySavedUserAnswers.arbitrary.sample.value.copy(
          intermediaryNumber = intermediaryNumber,
          lastUpdated = now
        )
        val savedUserAnswers2 = arbitrarySavedUserAnswers.arbitrary.sample.value.copy(
          intermediaryNumber = intermediaryNumber,
          lastUpdated = now.plusSeconds(3600)
        )

        val expectedResults = Seq(savedUserAnswers1, savedUserAnswers2)

        when(mockSaveForLaterRepository.getSelection(any())) thenReturn expectedResults.toFuture

        val service = saveForLaterService(stubClock)
        val result = service.getSavedUserAnswersSelection(intermediaryNumber).futureValue

        result `mustBe` expectedResults
        verify(mockSaveForLaterRepository, times(1)).getSelection(eqTo(intermediaryNumber))
      }

      "must return empty sequence when no results are found for the intermediary number" in {

        val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
        val stubClock = Clock.fixed(now, ZoneId.systemDefault())

        val intermediaryNumber = arbitrary[String].sample.value

        when(mockSaveForLaterRepository.getSelection(any())) thenReturn Seq.empty.toFuture

        val service = saveForLaterService(stubClock)
        val result = service.getSavedUserAnswersSelection(intermediaryNumber).futureValue

        result `mustBe` Seq.empty
        verify(mockSaveForLaterRepository, times(1)).getSelection(eqTo(intermediaryNumber))
      }
    }

    ".getCount" - {
      "should return a future long" in {
        val expectedResults = 1.toLong

        when(mockSaveForLaterRepository.count(any())) thenReturn expectedResults.toFuture

        val service = saveForLaterService(stubClock)
        val result = service.getCount(intermediaryNumber).futureValue

        result `mustBe` expectedResults
        verify(mockSaveForLaterRepository, times(1)).count(eqTo(intermediaryNumber))
      }
    }

    ".deleteSavedUserAnswers" - {

      "must delete a single SavedUserAnswers record and return true when successful" in {

        val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
        val stubClock = Clock.fixed(now, ZoneId.systemDefault())

        val journeyId = arbitrary[String].sample.value

        when(mockSaveForLaterRepository.clear(any())) thenReturn true.toFuture

        val service = saveForLaterService(stubClock)
        val result = service.deleteSavedUserAnswers(journeyId).futureValue

        result `mustBe` true
        verify(mockSaveForLaterRepository, times(1)).clear(eqTo(journeyId))
      }

      "must return false when deletion fails" in {

        val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
        val stubClock = Clock.fixed(now, ZoneId.systemDefault())

        val journeyId = arbitrary[String].sample.value

        when(mockSaveForLaterRepository.clear(any())) thenReturn false.toFuture

        val service = saveForLaterService(stubClock)
        val result = service.deleteSavedUserAnswers(journeyId).futureValue

        result `mustBe` false
        verify(mockSaveForLaterRepository, times(1)).clear(eqTo(journeyId))
      }
    }
  }
}