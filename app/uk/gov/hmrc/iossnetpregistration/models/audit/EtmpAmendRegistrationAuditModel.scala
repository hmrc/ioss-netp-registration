/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.models.audit

import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.iossnetpregistration.models.etmp.amend.{AmendRegistrationResponse, EtmpAmendRegistrationRequest}
import uk.gov.hmrc.iossnetpregistration.models.etmp.responses.EtmpEnrolmentResponse
import uk.gov.hmrc.iossnetpregistration.models.requests.ClientIdentifierRequest

case class EtmpAmendRegistrationAuditModel(
                                          etmpRegistrationAuditType: EtmpRegistrationAuditType,
                                          userId: String,
                                          userAgent: String,
                                          etmpRegistrationRequest: EtmpAmendRegistrationRequest,
                                          etmpEnrolmentResponse: Option[EtmpEnrolmentResponse],
                                          etmpAmendResponse: Option[AmendRegistrationResponse],
                                          errorResponse: Option[String],
                                          submissionResult: SubmissionResult
                                          ) extends JsonAuditModel {

  override val auditType: String = etmpRegistrationAuditType.auditType

  override val transactionName: String = etmpRegistrationAuditType.transactionName

  private val etmpEnrolmentResponseObj: JsObject =
    if(etmpEnrolmentResponse.isDefined) {
      Json.obj("etmpEnrolmentResponse" -> etmpEnrolmentResponse)
    } else {
      Json.obj()
    }

  private val etmpAmendResponseObj: JsObject =
    if(etmpAmendResponse.isDefined) {
      Json.obj("etmpAmendResponse" -> etmpAmendResponse)
    } else {
      Json.obj()
    }

  private val errorResponseObj: JsObject =
    if(errorResponse.isDefined) {
      Json.obj("errorResponse" -> errorResponse)
    } else {
      Json.obj()
    }

  override val detail: JsValue = Json.obj(
    "userId" -> userId,
    "browserUserAgent" -> userAgent,
    "etmpRegistrationRequest" -> Json.toJson(etmpRegistrationRequest),
    "submissionResult" -> Json.toJson(submissionResult)
  ) ++ etmpEnrolmentResponseObj ++
    etmpAmendResponseObj ++
    errorResponseObj
}

object EtmpAmendRegistrationAuditModel {

  def build(
             etmpRegistrationAuditType: EtmpRegistrationAuditType,
             etmpRegistrationRequest: EtmpAmendRegistrationRequest,
             etmpEnrolmentResponse: Option[EtmpEnrolmentResponse],
             etmpAmendResponse: Option[AmendRegistrationResponse],
             errorResponse: Option[String],
             submissionResult: SubmissionResult
           )(implicit request: ClientIdentifierRequest[_]): EtmpAmendRegistrationAuditModel =
    EtmpAmendRegistrationAuditModel(
      etmpRegistrationAuditType = etmpRegistrationAuditType,
      userId = request.userId,
      userAgent = request.headers.get("user-agent").getOrElse(""),
      etmpRegistrationRequest = etmpRegistrationRequest,
      etmpEnrolmentResponse = etmpEnrolmentResponse,
      etmpAmendResponse = etmpAmendResponse,
      errorResponse = errorResponse,
      submissionResult = submissionResult
    )
}