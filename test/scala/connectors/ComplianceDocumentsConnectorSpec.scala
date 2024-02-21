/*
 * Copyright 2023 HM Revenue & Customs
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

package scala.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import connectors.ComplianceDocumentsConnector
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.Application
import play.api.http.ContentTypes
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class ComplianceDocumentsConnectorSpec extends ConnectorSpec {
  implicit val defaultPatience = PatienceConfig //PatienceConfig(timeout =  Span(30, Seconds), interval = Span(5, Millis))
  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure("integration-framework.base-url" -> s"http://localhost:${server.port}", "auditing.enabled" -> false)
    .build()

  protected implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  val correlationId: String = "some-correlation-id"
  val authId: String = "Bearer some-token"
  val documentId: String = "4321"

  protected def connector = app.injector.instanceOf[ComplianceDocumentsConnector]

  "The Compliance Documents connector" should {
    "return a 202 when given Json" in {
      server.stubFor(post(urlEqualTo(s"/organisations/document/$documentId"))
        .withHeader(CONTENT_TYPE, matching(ContentTypes.JSON))
        .withHeader("CorrelationId", equalTo(correlationId))
        .withHeader("Authorization", equalTo(authId))
        .withHeader("Environment", equalTo("local"))
        .willReturn(
          aResponse()
            .withStatus(ACCEPTED)
            .withBody("Success!".stripMargin)
            .withHeader("contentType", "application/json")
        )
      )

      whenReady(connector.vatRepayment(Json.obj("Case" -> "CSC-12394712"), correlationId, documentId)) {
        response =>
          response.get.status mustBe ACCEPTED
      }
    }

    "return a 400 when given invalid input" in {
      server.stubFor(post(urlEqualTo(s"/organisations/document/$documentId"))
        .withHeader(CONTENT_TYPE, matching(ContentTypes.JSON))
        .withHeader("CorrelationId", equalTo(correlationId))
        .withHeader("Authorization", equalTo(authId))
        .withHeader("Environment", equalTo("local"))
        .willReturn(
          aResponse()
            .withStatus(BAD_REQUEST)
            .withBody("Error!".stripMargin)
            .withHeader("contentType", "application/json")
        )
      )

      whenReady(connector.vatRepayment(Json.obj("a" -> 1), correlationId, documentId)) {
        response =>
          response.isDefined mustBe false
      }
    }

    "return a 404 when attempting to connect to non-existent endpoint" in {
      server.stubFor(post(urlEqualTo(s"/organisations/document/$documentId"))
        .withHeader(CONTENT_TYPE, matching(ContentTypes.JSON))
        .withHeader("CorrelationId", equalTo(correlationId))
        .withHeader("Authorization", equalTo(authId))
        .withHeader("Environment", equalTo("local"))
        .willReturn(
          notFound()
        )
      )

      whenReady(connector.vatRepayment(Json.obj("a" -> 1), correlationId, documentId)) {
        response =>
          response.isDefined mustBe false
      }
    }

    "return a 401 when attempting to connect without authentication" in {
      server.stubFor(post(urlEqualTo(s"/organisations/document/$documentId"))
        .withHeader(CONTENT_TYPE, matching(ContentTypes.JSON))
        .withHeader("CorrelationId", equalTo(correlationId))
        .withHeader("Authorization", equalTo(authId))
        .withHeader("Environment", equalTo("local"))
        .willReturn(
          unauthorized()
        )
      )

      whenReady(connector.vatRepayment(Json.obj("a" -> 1), correlationId, documentId)) {
        response =>
          response.isDefined mustBe false
      }

    }
    "return a Left when call fails" in {
      def exception = aResponse.withFault(Fault.CONNECTION_RESET_BY_PEER)

      server.stubFor(post(urlEqualTo(s"/organisations/document/$documentId"))
        .withHeader(CONTENT_TYPE, matching(ContentTypes.JSON))
        .withHeader("CorrelationId", equalTo(correlationId))
        .withHeader("Authorization", equalTo(authId))
        .withHeader("Environment", equalTo("local"))
        .willReturn(exception)
      )

      whenReady(connector.vatRepayment(Json.obj("a" -> 1), correlationId, documentId)) {
        response =>
          response.isDefined mustBe false
      }

    }
  }
}
