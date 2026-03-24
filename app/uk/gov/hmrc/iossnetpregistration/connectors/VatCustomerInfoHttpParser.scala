/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.connectors

import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.iossnetpregistration.models.des.VatCustomerInfo
import uk.gov.hmrc.iossnetpregistration.models.responses.ErrorResponse

object VatCustomerInfoHttpParser extends BaseHttpParser {

  override val serviceName: String = "DES"

  type VatCustomerInfoResponse = Either[ErrorResponse, VatCustomerInfo]

  implicit object VatCustomerInfoReads extends HttpReads[VatCustomerInfoResponse] {
    override def read(method: String, url: String, response: HttpResponse): VatCustomerInfoResponse =
      parseResponse[VatCustomerInfo](response)(VatCustomerInfo.desReads)
  }
}

