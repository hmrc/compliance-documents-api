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

package scala.services

import akka.stream.Materializer
import models.Document
import org.mockito.{Matchers, Mockito}
import org.scalatest.{BeforeAndAfterEach, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc._
import services.{ResourceService, ValidationService}
import uk.gov.hmrc.http.HeaderCarrier
import scala.exampleData.VatDocumentExample._
import scala.concurrent.ExecutionContext.Implicits.global

class ValidationServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar
  with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  implicit lazy val hc: HeaderCarrier = HeaderCarrier(sessionId = None)

  implicit lazy val materializer: Materializer = app.materializer

  val bodyParser = new BodyParsers.Default
  val mockResource = mock[ResourceService]

  def validationService = new ValidationService(bodyParser, mockResource)


  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockResource)
  }


  "The validation service" should {
    "return errors when model does not map properly" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn("{}")
      validationService.validate[Document](Json.parse(getExample("justInvalid")), "1234", validCorrelationId = true).left.get mustBe Json.parse(
        """
{"code":"JSON_VALIDATION_ERROR","message":"The provided JSON was unable to be validated as the ef model.","errors":[{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/classIndex"}]}
          |""".stripMargin
      )
    }

    "return INVALID_CORRELATIONID if given wrong correlation id" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)
      validationService.validate[Document](Json.parse(getExample("ef")), "1234", validCorrelationId = false).left.get mustBe Json.parse(
        """
 {"message":"Unable to process request.","errors":[{"code":"INVALID_CORRELATIONID","message":"Submission has not passed validation. Invalid Header CorrelationId."}]}
          |""".stripMargin
      )
    }

    "return INVALID_DOCUMENTID if given wrong document id" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)
      validationService.validate[Document](Json.parse(getExample("pReg")), "1234a").left.get mustBe Json.parse(
        """
          |{"message":"Unable to process request.","errors":[{"code":"INVALID_DOCUMENTID","message":"Submission has not passed validation. Invalid parameter documentId."}]}
          |""".stripMargin
      )
    }
    "return INVALID_CORRELATIONID, INVALID_PAYLOAD and INVALID_DOCUMENTID if all are wrong" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)
      validationService.validate[Document](Json.parse(getExample("efInvalid")), "1234a", validCorrelationId = false).left.get mustBe Json.parse(
        """
          |{"message":"Unable to process request.","errors":[{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload."},{"code":"INVALID_CORRELATIONID","message":"Submission has not passed validation. Invalid Header CorrelationId."},{"code":"INVALID_DOCUMENTID","message":"Submission has not passed validation. Invalid parameter documentId."}]}
          |""".stripMargin
      )
    }
    "return nothing if given valid input - EF" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)
      assert(validationService.validate[Document](Json.parse(getExample("ef")), "1234").isRight)
    }
    "return nothing if given valid input - nReg" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)
      assert(validationService.validate[Document](Json.parse(getExample("nReg")), "1234").isRight)
    }
    "return nothing if given valid input - pReg" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)
      assert(validationService.validate[Document](Json.parse(getExample("pReg")), "1234").isRight)
    }
    "return bad request if given invalid input with one field not matching Regex" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)
      val resultOfBadOne = validationService.validate[Document](Json.parse(getExample("invalidNoMissing")), "1234")
      assert(resultOfBadOne.isLeft)
    }
    "return bad request if given valid input that doesn't match the model" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(invalidSchema)
      assert(validationService.validate[Document](Json.parse(fitsInvalidSchema), "1234").isLeft)
    }
  }
  "The validate doc type method" should {
    "return bad request if given invalid Json" in {
      Mockito.when(mockResource.getFile(Matchers.any())).thenReturn(schema)

      val docJson = Json.obj("documentMetadata" -> Json.obj("classIndex" -> Json.obj("ef" -> Json.obj("dTRN" -> "9443402451823"))))
      assert(validationService.validateDocType(docJson).isLeft)
    }
  }
}
