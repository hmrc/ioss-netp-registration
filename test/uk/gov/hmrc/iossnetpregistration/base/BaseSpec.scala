/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.base

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Writes}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.iossnetpregistration.controllers.actions.{AnyLoggedInUserAction, AuthAction, ClientIdentifierAction, FakeAnyLoggedIntUserAction, FakeAuthAction, FakeClientIdentifierAction, FakeIdentifierAction, IdentifierAction}
import uk.gov.hmrc.iossnetpregistration.generators.Generators
import uk.gov.hmrc.iossnetpregistration.models.DesAddress
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo
import uk.gov.hmrc.iossnetpregistration.models.etmp.*
import uk.gov.hmrc.iossnetpregistration.models.etmp.display.{EtmpDisplayCustomerIdentification, EtmpDisplayEuRegistrationDetails, EtmpDisplayRegistration, EtmpDisplaySchemeDetails}

import java.time.{Clock, LocalDate, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Locale

trait BaseSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with Generators {

  protected val vrn: Vrn = Vrn("123456789")
  protected val intermediaryNumber: String = "IN9001234567"
  protected val iossNumber: String = "IN9001234568"

  val stubClock: Clock = Clock.fixed(LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "/endpoint")

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[ClientIdentifierAction].to[FakeClientIdentifierAction],
        bind[AuthAction].to[FakeAuthAction],
        bind[AnyLoggedInUserAction].to[FakeAnyLoggedIntUserAction]
      )

  val userId: String = "12345-userId"
  val testCredentials: Credentials = Credentials(userId, "GGW")

  val vatCustomerInfo: VatCustomerInfo =
    VatCustomerInfo(
      registrationDate = Some(LocalDate.now(stubClock)),
      desAddress = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      organisationName = Some("Company name"),
      singleMarketIndicator = true,
      individualName = None,
      deregistrationDecisionDate = None
    )

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("GMT"))

  private val etmpDisplaySchemeDetailsWrites: Writes[EtmpDisplaySchemeDetails] = {
    (
      (__ \ "commencementDate").write[String] and
        (__ \ "euRegistrationDetails").write[Seq[EtmpDisplayEuRegistrationDetails]] and
        (__ \ "previousEURegistrationDetails").write[Seq[EtmpPreviousEuRegistrationDetails]] and
        (__ \ "contactDetails" \ "contactNameOrBusinessAddress").write[String] and
        (__ \ "contactDetails" \ "businessTelephoneNumber").write[String] and
        (__ \ "contactDetails" \ "businessEmailAddress").write[String] and
        (__ \ "contactDetails" \ "unusableStatus").write[Boolean] and
        (__ \ "nonCompliantReturns").writeNullable[String] and
        (__ \ "nonCompliantPayments").writeNullable[String] and
        (__ \ "websites").write[Seq[EtmpWebsite]]
      )(etmpDisplaySchemeDetails => Tuple.fromProductTyped(etmpDisplaySchemeDetails))
  }

  val etmpDisplayRegistrationWrites: Writes[EtmpDisplayRegistration] =
    (
      (__ \ "customerIdentification").write[EtmpDisplayCustomerIdentification] and
        (__ \ "tradingNames").write[Seq[EtmpTradingName]] and
        (__ \ "otherAddress").writeNullable[EtmpOtherAddress] and
        (__ \ "schemeDetails").write[EtmpDisplaySchemeDetails](etmpDisplaySchemeDetailsWrites) and
        (__ \ "exclusions").write[Seq[EtmpExclusion]] and
        (__ \ "adminUse").write[EtmpAdminUse]
      )(etmpDisplayRegistration => Tuple.fromProductTyped(etmpDisplayRegistration))
}



