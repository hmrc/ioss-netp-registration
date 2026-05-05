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

import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ActionBuilder, AnyContent, DefaultActionBuilder, MessagesActionBuilder, MessagesControllerComponents, PlayBodyParsers}
import uk.gov.hmrc.iossnetpregistration.models.requests.{AnyLoggedInUserRequest, IdentifierRequest}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

trait AuthenticatedControllerComponents extends MessagesControllerComponents {
  
  def actionBuilder: DefaultActionBuilder

  def anyLoggedInUserAction: AnyLoggedInUserAction
  
  def identifyAction: IdentifierAction
  
  def clientIdentify: ClientIdentifierAction
  
  def identify: ActionBuilder[IdentifierRequest, AnyContent] =
    actionBuilder andThen
      identifyAction

  def loggedInAction: ActionBuilder[AnyLoggedInUserRequest, AnyContent] =
    actionBuilder andThen
      anyLoggedInUserAction

  def authAction: AuthAction

  def auth(): ActionBuilder[AuthorisedRequest, AnyContent] =
    actionBuilder andThen
      authAction
  
}

case class DefaultAuthenticatedControllerComponents @Inject()(
                                                                 messagesActionBuilder: MessagesActionBuilder,
                                                                 actionBuilder: DefaultActionBuilder,
                                                                 parsers: PlayBodyParsers,
                                                                 messagesApi: MessagesApi,
                                                                 langs: Langs,
                                                                 fileMimeTypes: FileMimeTypes,
                                                                 executionContext: ExecutionContext,
                                                                 anyLoggedInUserAction: AnyLoggedInUserAction,
                                                                 identifyAction: IdentifierAction,
                                                                 clientIdentify: ClientIdentifierAction,
                                                                 authAction: AuthAction
                                                               ) extends AuthenticatedControllerComponents
