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

package uk.gov.hmrc.iossnetpregistration.controllers.actions

import com.google.inject.Inject
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossnetpregistration.base.BaseSpec
import uk.gov.hmrc.iossnetpregistration.config.AppConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends BaseSpec with MockitoSugar {

  class Harness(identifierAction: IdentifierAction) {
    def get(): Action[AnyContent] = identifierAction { _ => Results.Ok }
  }

  "Auth Action" - {

    "when the user hasn't logged in" - {

      "must return 401 unauthorised status" in {

        val application = applicationBuilder().build()


        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new MissingBearerToken),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.get()(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "the user's session has expired" - {

      "must return 401 unauthorised status" in {

        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new BearerTokenExpired),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.get()(fakeRequest)

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must return 401 unauthorised status" in {

        val application = applicationBuilder().build()


        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientEnrolments),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.get()(FakeRequest())

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {

      "must return 401 unauthorised status" in {

        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.get()(FakeRequest())

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "the user used an unaccepted auth provider" - {

      "must return 401 unauthorised status" in {

        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.get()(FakeRequest())

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must return 401 unauthorised status" in {

        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.get()(FakeRequest())

          status(result) mustBe UNAUTHORIZED
        }
      }
    }

    "the user has an unsupported credential role" - {

      "must return 401 unauthorised status" in {

        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new UnsupportedCredentialRole),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.get()(FakeRequest())

          status(result) mustBe UNAUTHORIZED
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
