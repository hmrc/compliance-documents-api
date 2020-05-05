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
import config.AppConfig
import connectors.ComplianceDocumentsConnector
import controllers.actions.{AuthenticateApplicationAction, ValidateCorrelationIdHeaderAction}
import controllers.routes
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse

import scala.exampleData.VatDocumentExample._
import scala.concurrent.Future

class VatRepaymentApiControllerSpec extends WordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {

  val mockAuthApp: AuthenticateApplicationAction = mock[AuthenticateApplicationAction]

  Mockito.when(mockAuthApp.andThen[Request](any())).thenAnswer(
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

        Mockito.when(connector.vatRepayment(any(), eqTo(correlationId), eqTo(documentId.toLong))(any(), any()))
          .thenReturn(Future.successful(Right(HttpResponse(ACCEPTED, Some(Json.parse(getExample("ef"))),
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

        Mockito.when(connector.vatRepayment(any(), eqTo(correlationId), eqTo(documentId.toLong))(any(), any()))
          .thenReturn(Future.successful(Right(HttpResponse(ACCEPTED, Some(Json.parse(getExample("nReg"))),
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

        Mockito.when(connector.vatRepayment(any(), eqTo(correlationId), eqTo(documentId.toLong))(any(), any()))
          .thenReturn(Future.successful(Right(HttpResponse(ACCEPTED, Some(Json.parse(getExample("pReg"))),
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
              |{"code":"JSON_VALIDATION_ERROR","message":"The provided JSON was unable to be validated.","errors":[{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/classIndex"}]}
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
              |{"message":"Unable to process request.","errors":[{"code":"INVALID_CORRELATIONID","message":"Submission has not passed validation. Invalid Header CorrelationId."}]}
              |""".stripMargin
          )
        }
      }
      "return BadRequest if not given a correlationId" in {

        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url)
          .withJsonBody(Json.parse(getExample("pReg")))).map { result =>
          status(result) shouldBe Status.BAD_REQUEST
          contentAsJson(result) shouldBe Json.parse(
            """{"message":"Unable to process request.","errors":[{"code":"INVALID_CORRELATIONID",
              |"message":"Submission has not passed validation. Invalid Header CorrelationId."}]}""".stripMargin
          )
        }
      }

      "return InternalServerError if the connector is unsuccessful in communicating with IF" in {
        Mockito.when(connector.vatRepayment(any(), eqTo(correlationId), eqTo(documentId.toLong))(any(), any()))
          .thenReturn(Future.successful(Left(())))

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
