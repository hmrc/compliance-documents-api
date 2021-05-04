/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.ComplianceDocumentsConnector
import controllers.actions.{AuthenticateApplicationAction, RequestWithCorrelationId, ValidateCorrelationIdHeaderAction}
import controllers.{VatRepaymentApiController, routes}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{BodyParsers, ControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ValidationService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.exampleData.VatDocumentExample._

class VatRepaymentApiControllerSpec extends AnyWordSpec with Matchers with MockFactory {
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = None)

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

    (mockAuthApp.andThen[Request] _).expects(*).returns {
      StubbedCorrelationIdAction
    }


    val testController: VatRepaymentApiController = new VatRepaymentApiController(mockValidation, connector, StubbedCorrelationIdAction, mockAuthApp, mockCc)
    val documentId: String = "532493"
    val connectorResponse: Option[HttpResponse] = if (serverError) {
      Option.empty[HttpResponse]
    } else {
      Some(HttpResponse(ACCEPTED, None,
        Map("Content-Type" -> Seq("application/json"), "header" -> Seq("`123")))
      )
    }
    if (validBody.isDefined) {
      (connector.vatRepayment(_: JsValue, _: String, _: String)(_: ExecutionContext))
        .expects(validBody.get, correlationId, documentId, *)
        .returns(
          Future.successful(
            connectorResponse
          )
        )
    }
    (mockValidation.validate(_: JsValue, _: String)).expects(*, *).returns(validationErrors)

  }


  "The Vat Repayment Api Controller" when {
    "calling the getResponse route" should {
      "return Accepted if given a valid Json validBody" in new Setup(validBody = Some(Json.parse(getExample("ef")))) {


        val result: Future[Result] = testController.postRepaymentData(documentId)
          .apply(
            FakeRequest(
              POST, routes.VatRepaymentApiController.postRepaymentData(documentId).url
            )
              .withHeaders("CorrelationId" -> correlationId)
              .withJsonBody(requestBody)
          )
        status(result) shouldBe 202
        contentAsString(result) shouldBe ""


      }
      "return BadRequest if not given a validBody" in new Setup(
        Some(Json.parse(
          """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentBinary"},{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentMetadata"}]}
""".stripMargin
        )), validBody = None
      ) {
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
