/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossnetpregistration.config

import play.api.{ConfigLoader, Configuration}

final case class Service(host: String, port: String, protocol: String, basePath: String) {

  def baseUrl: String = s"$protocol://$host:$port/$basePath"

  override def toString: String = baseUrl
}

object Service {

  implicit lazy val configLoader: ConfigLoader[Service] = ConfigLoader {
    config =>
      prefix =>

        val service  = Configuration(config).get[Configuration](prefix)
        val host     = service.get[String]("host")
        val port     = service.get[String]("port")
        val protocol = service.get[String]("protocol")
        val basePath = service.get[String]("basePath")

        Service(host, port, protocol, basePath)
  }
}
