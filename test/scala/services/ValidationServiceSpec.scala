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
import org.mockito.{ArgumentMatchersSugar, Mockito, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc._
import services.{ResourceService, ValidationService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.exampleData.VatDocumentExample._

class ValidationServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar
  with ScalaFutures with IntegrationPatience with BeforeAndAfterEach with ArgumentMatchersSugar {

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
      Mockito.when(mockResource.getFile(any)).thenReturn("{}")
      validationService.validate[Document](Json.parse(getExample("justInvalid")), "1234").left.get mustBe Json.parse(
        """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation for the ef model. Invalid payload.","errors":[{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/classIndex//dTRN"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docDate"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docBinaryHash"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docBinaryRef"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docType"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docBinaryType"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/creatingUser"},{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload."}]}
          |""".stripMargin
      )
    }

    "return INVALID_DOCUMENT_ID if given wrong document id" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(schema)
      validationService.validate[Document](Json.parse(getExample("pReg")), "1234a").left.get mustBe Json.parse(
        """
          |{"code":"INVALID_DOCUMENT_ID","message":"Submission has not passed validation. Invalid path parameter DocumentId."}
          |""".stripMargin
      )
    }
    "return nothing if given valid input - EF" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(schema)
      assert(validationService.validate[Document](Json.parse(getExample("ef")), "1234").isRight)
    }
    "return nothing if given valid input - nReg" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(schema)
      assert(validationService.validate[Document](Json.parse(getExample("nReg")), "1234").isRight)
    }
    "return nothing if given valid input - pReg" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(schema)
      assert(validationService.validate[Document](Json.parse(getExample("pReg")), "1234").isRight)
    }
    "return bad request if given invalid classIndex" in {
      when(mockResource.getFile(any)).thenReturn(schema)
      val resultOfBadOne = validationService.validate[Document](Json.parse(minWithEmptySpace(badDocument)), "1234")
      assert(resultOfBadOne.isLeft)
      resultOfBadOne.left.get mustBe Json.parse(
        """
          |{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload.","errors":[{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/classIndex"}]}
          |""".stripMargin
      )

    }
    "return bad request if given invalid input with one field not matching Regex" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(schema)
      val resultOfBadOne = validationService.validate[Document](Json.parse(getExample("invalidNoMissing")), "1234")
      assert(resultOfBadOne.isLeft)
      resultOfBadOne.left.get mustBe Json.parse(
        """
          |{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation for the ef model. Invalid payload.","errors":[{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docBinaryType"}]}
          |""".stripMargin)
    }
    "return bad request if given invalid input with both missing & unexpected fields" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(schema)
      val resultOfBadOne = validationService.validate[Document](Json.parse(getExample("unexpectedAndMissing")), "1234")
      resultOfBadOne.left.get mustBe Json.parse(
        """
{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation for the ef model. Invalid payload.","errors":[{"code":"MISSING_FIELD","message":"Expected field not present","path":"/documentBinary"},{"code":"UNEXPECTED_FIELD","message":"Unexpected field found","path":"/documentMetadata/wrong"}]}
""".stripMargin
      )
    }
    "return bad request if given valid input that doesn't match the model" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(invalidSchema)
      val resultOfBadOne = validationService.validate[Document](Json.parse(fitsInvalidSchema), "1234")
      assert(resultOfBadOne.isLeft)
      resultOfBadOne.left.get mustBe Json.parse(
        """
          |{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation for the ef model. Invalid payload.","errors":[{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docDate"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docBinaryHash"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docBinaryRef"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docType"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/docBinaryType"},{"code":"INVALID_FIELD","message":"Invalid value in field","path":"/documentMetadata/creatingUser"},{"code":"INVALID_PAYLOAD","message":"Submission has not passed validation. Invalid payload."}]}
          |""".stripMargin
      )
    }
  }
  "The validate doc type method" should {
    "return bad request if given invalid Json" in {
      Mockito.when(mockResource.getFile(any)).thenReturn(schema)

      val docJson = Json.obj("documentMetadata" -> Json.obj("classIndex" -> Json.obj("ef" -> Json.obj("dTRN" -> "9443402451823"))))
      assert(validationService.validateDocType(docJson).isLeft)
    }
  }
}
