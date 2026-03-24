/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import com.google.inject.AbstractModule
import uk.gov.hmrc.iossnetpregistration.controllers.actions.{AnyLoggedInUserAction, AnyLoggedInUserActionImpl, AuthAction, AuthActionImpl, AuthenticatedControllerComponents, AuthenticatedIdentifierAction, ClientIdentifierAction, ClientIdentifierActionImpl, DefaultAuthenticatedControllerComponents, IdentifierAction}

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {

    bind(classOf[AppConfig]).asEagerSingleton()

    bind(classOf[AuthenticatedControllerComponents]).to(classOf[DefaultAuthenticatedControllerComponents]).asEagerSingleton()
    bind(classOf[IdentifierAction]).to(classOf[AuthenticatedIdentifierAction]).asEagerSingleton()
    bind(classOf[ClientIdentifierAction]).to(classOf[ClientIdentifierActionImpl]).asEagerSingleton()
    bind(classOf[AnyLoggedInUserAction]).to(classOf[AnyLoggedInUserActionImpl]).asEagerSingleton()
    bind(classOf[AuthAction]).to(classOf[AuthActionImpl]).asEagerSingleton()

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
    
  }
}
