/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.ComplianceDocumentsConnector
import controllers.actions.{AuthenticateApplicationAction, RequestWithCorrelationId, ValidateCorrelationIdHeaderAction}
import controllers.{VatRepaymentApiController, routes}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
//import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, BodyParsers, ControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ValidationService
import uk.gov.hmrc.http.HttpResponse
import utils.LoggerHelper
import org.mockito.ArgumentMatchers._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{times, verify}
import controllers.actions.AuthenticateApplicationAction
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration

import java.util.UUID
import scala.concurrent.Future
import scala.exampleData.VatDocumentExample._

class VatRepaymentApiControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {
  //  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = None)

  class Setup(validationErrors: Option[JsValue] = None, serverError: Boolean = false,
              validBody: Option[JsValue]) {
    val requestBody: JsValue = validBody.getOrElse(Json.obj())
    val correlationId: String = UUID.randomUUID().toString
    implicit val ee: ExecutionContext = stubControllerComponents().executionContext
    val mockAuthApp: AuthenticateApplicationAction = mock[AuthenticateApplicationAction]
    val mockValidation: ValidationService = mock[ValidationService]
    val connector: ComplianceDocumentsConnector = mock[ComplianceDocumentsConnector]
    val mockCc: ControllerComponents = stubControllerComponents()

    object StubbedCorrelationIdAction extends ValidateCorrelationIdHeaderAction(
      new BodyParsers.Default(stubControllerComponents().parsers)
    )(
      stubControllerComponents().executionContext
    ) {
      override def invokeBlock[A](request: Request[A], block: RequestWithCorrelationId[A] => Future[Result]): Future[Result] = {
        block(RequestWithCorrelationId(request, correlationId))
      }
    }

    //    (mockAuthApp.andThen[Request] _).expects(*).returns {
    //      StubbedCorrelationIdAction
    //    }


    when(mockAuthApp.andThen(ArgumentMatchers.any())).thenReturn{
      StubbedCorrelationIdAction
    }

    val testController: VatRepaymentApiController = new VatRepaymentApiController(mockValidation, connector, StubbedCorrelationIdAction, mockAuthApp, mockCc)
    val documentId: String = "532493"
    val connectorResponse: Option[HttpResponse] = if (serverError) {
      Option.empty[HttpResponse]
    } else {
      Some(HttpResponse(ACCEPTED, "",
        Map("Content-Type" -> Seq("application/json"), "header" -> Seq("`123")))
      )
    }
    //    if (validBody.isDefined) {
    //      (connector.vatRepayment(_: JsValue, _: String, _: String)(_: ExecutionContext, _: HeaderCarrier))
    //        .expects(validBody.get, correlationId, documentId, *, *)
    //        .returns(
    //          Future.successful(
    //            connectorResponse
    //          )
    //        )
    //    }

    if (validBody.isDefined) {
      when(
        connector.vatRepayment(
          ArgumentMatchers.eq(validBody.get),
          ArgumentMatchers.eq(correlationId),
          ArgumentMatchers.eq(documentId)
        )(ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(Future.successful(connectorResponse))
    }
    when(mockValidation.validate(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(validationErrors)
  }


  "The Vat Repayment Api Controller" when {
    "calling the getResponse route" should {
      "return Accepted if given a valid Json validBody" in new Setup(validBody = Some(Json.parse(getExample("ef")))) {

        LoggerHelper.logProcess("VatRepaymentApiControllerSpec", "postRepaymentData",
          "Valid request with body received", Some(correlationId), Some(requestBody))

        val result: Future[Result] = testController.postRepaymentData(documentId)
          .apply(
            FakeRequest(
              POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url
            )
              .withHeaders("CorrelationId" -> correlationId)
              .withJsonBody(requestBody)
          )
        status(result) shouldBe ACCEPTED
        contentAsString(result) shouldBe ""
      }

      "return BadRequest if not given a validBody" in new Setup(
        Some(Json.parse(
          """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentBinary"},{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentMetadata"}]}
""".stripMargin
        )), validBody = None
      ) {
        LoggerHelper.logProcess("VatRepaymentApiControllerSpec", "postRepaymentData", "Invalid request body received", Some(correlationId), None)

        val result = testController.postRepaymentData(documentId)(
          FakeRequest(
            POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url
          )
            .withHeaders("CorrelationId" -> correlationId)
        )
        status(result) shouldBe 400
        contentAsJson(result) shouldBe Json.parse(
          """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentBinary"},{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentMetadata"}]}
""".stripMargin
        )

      }
      "return BadRequest if given an invalid Json validBody" in new Setup(
        Some(Json.parse(
          """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"UNEXPECTED_FIELD","message":"Unexpected field found","path":"/documentMetadata/wrong"}]}
            |""".stripMargin
        )), validBody = None
      ) {
        LoggerHelper.logProcess("VatRepaymentApiControllerSpec", "postRepaymentData", "Invalid JSON structure received", Some(correlationId), None)

        val result: Future[Result] = testController.postRepaymentData(documentId)
          .apply(
            FakeRequest(
              POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url
            )
              .withHeaders("CorrelationId" -> correlationId)
              .withJsonBody(Json.parse(getExample("invalidAddedField"))
              ))
        status(result) shouldBe 400
        contentAsJson(result) shouldBe Json.parse(
          """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"UNEXPECTED_FIELD","message":"Unexpected field found","path":"/documentMetadata/wrong"}]}
            |""".stripMargin
        )

      }

      "return InternalServerError if the connector is unsuccessful in communicating with IF" in new Setup(serverError = true,
        validBody = Some(Json.parse(getExample("nReg")))) {
        LoggerHelper.logProcess("VatRepaymentApiControllerSpec", "postRepaymentData", "Connector communication failed", Some(correlationId), Some(requestBody))

        val result = testController.postRepaymentData(documentId)
          .apply(
            FakeRequest(
              POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url
            )
              .withHeaders("CorrelationId" -> correlationId)
              .withJsonBody(requestBody)
          )
        status(result) shouldBe 500
        contentAsJson(result) shouldBe Json.obj(
          "code" -> "INTERNAL_SERVER_ERROR",
          "message" -> "Internal server error"
        )
      }
    }
  }
}
