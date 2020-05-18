/*
 * Copyright 2020 HM Revenue & Customs
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

package scala.controllers

import java.util.UUID

import akka.stream.Materializer
import connectors.ComplianceDocumentsConnector
import controllers.actions.{AuthenticateApplicationAction, ValidateCorrelationIdHeaderAction}
import controllers.routes
import org.mockito.invocation.InvocationOnMock
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.exampleData.VatDocumentExample._

class VatRepaymentApiControllerSpec extends WordSpec with Matchers with MockitoSugar with ArgumentMatchersSugar with GuiceOneAppPerSuite {

  val mockAuthApp: AuthenticateApplicationAction = mock[AuthenticateApplicationAction]

  when(mockAuthApp.andThen[Request](any)).thenAnswer(
    (invocation: InvocationOnMock) => invocation.getArguments()(0).asInstanceOf[ValidateCorrelationIdHeaderAction]
  )

  private val connector: ComplianceDocumentsConnector = mock[ComplianceDocumentsConnector]
  override lazy val app: Application = {
    import play.api.inject._

    new GuiceApplicationBuilder()
      .overrides(
        bind[ComplianceDocumentsConnector].toInstance(connector),
        bind[AuthenticateApplicationAction].toInstance(mockAuthApp)
      ).build()
  }

  implicit lazy val materializer: Materializer = app.materializer

  val correlationId: String = UUID.randomUUID().toString
  val documentId: String = "532493"
  "The Vat Repayment Api Controller" when {
    "calling the getResponse route" should {
      "return Accepted if given a valid Json body - EF" in {

        when(connector.vatRepayment(any, eqTo(correlationId), eqTo(documentId.toLong))(any, any))
          .thenReturn(Future.successful(Some(HttpResponse(ACCEPTED, Some(Json.parse(getExample("ef"))),
            Map("Content-Type" -> Seq("application/json"), "header" -> Seq("`123")))
          )))

        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withHeaders("CorrelationId" -> correlationId)
          .withJsonBody(Json.parse(getExample("ef")))).map { result =>
          status(result) shouldBe Status.ACCEPTED
          contentAsString(result) shouldBe ""
        }

      }
      "return Accepted if given a valid Json body - nReg" in {

        when(connector.vatRepayment(any, eqTo(correlationId), eqTo(documentId.toLong))(any, any))
          .thenReturn(Future.successful(Some(HttpResponse(ACCEPTED, Some(Json.parse(getExample("nReg"))),
            Map("Content-Type" -> Seq("application/json"), "header" -> Seq("`123")))
          )))

        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withHeaders("CorrelationId" -> correlationId)
          .withJsonBody(Json.parse(getExample("pReg")))).map { result =>
          status(result) shouldBe Status.ACCEPTED
          contentAsString(result) shouldBe ""
        }

      }
      "return Accepted if given a valid Json body - pReg" in {

        when(connector.vatRepayment(any, eqTo(correlationId), eqTo(documentId.toLong))(any, any))
          .thenReturn(Future.successful(Some(HttpResponse(ACCEPTED, Some(Json.parse(getExample("pReg"))),
            Map("Content-Type" -> Seq("application/json"), "header" -> Seq("`123")))
          )))

        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withHeaders("CorrelationId" -> correlationId)
          .withJsonBody(Json.parse(getExample("pReg")))).map { result =>
          status(result) shouldBe Status.ACCEPTED
          contentAsString(result) shouldBe ""
        }

      }
      "return BadRequest if not given a Json body" in {
        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withHeaders("CorrelationId" -> correlationId)
          .withBody("This is not Json!")).map { result =>
          status(result) shouldBe Status.BAD_REQUEST
          contentAsJson(result) shouldBe Json.parse(
            """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentBinary"},{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentMetadata"}]}
""".stripMargin
          )
        }
      }
      "return BadRequest if given an invalid Json body with an unexpected field present" in {
        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withHeaders("CorrelationId" -> correlationId)
          .withBody(Json.parse(getExample("invalidAddedField")))).map { result =>
          status(result) shouldBe Status.BAD_REQUEST
          contentAsJson(result) shouldBe Json.parse(
            """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"UNEXPECTED_FIELD","message":"Unexpected field found","path":"/documentMetadata/wrong"}]}
              |""".stripMargin
          )
        }
      }
      "return BadRequest if given an invalid correlationId" in {

        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withHeaders("CorrelationId" -> "@£$*&")
          .withJsonBody(Json.parse(getExample("nReg")))).map { result =>
          status(result) shouldBe Status.BAD_REQUEST
          contentAsJson(result) shouldBe Json.parse(
            """
              |{"code":"INVALID_CORRELATION_ID","message":"Submission has not passed validation. Invalid header CorrelationId."}
              |""".stripMargin
          )
        }
      }
      "return BadRequest if not given a correlationId" in {

        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withJsonBody(Json.parse(getExample("pReg")))).map { result =>
          status(result) shouldBe Status.BAD_REQUEST
          contentAsJson(result) shouldBe Json.parse(
            """{"code":"MISSING_CORRELATION_ID","message":"Submission has not passed validation. Missing header CorrelationId."}
              |""".stripMargin
          )
        }
      }

      "return InternalServerError if the connector is unsuccessful in communicating with IF" in {
        when(connector.vatRepayment(any, eqTo(correlationId), eqTo(documentId.toLong))(any, any))
          .thenReturn(Future.successful(None))

        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withHeaders("CorrelationId" -> correlationId)
          .withJsonBody(Json.parse(getExample("ef")))).map { result =>
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
          contentAsJson(result) shouldBe Json.obj(
            "code" -> "INTERNAL_SERVER_ERROR",
            "message" -> "Internal server error"
          )
        }

      }
    }
  }
}
