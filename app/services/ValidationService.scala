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

package services

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.{ListReportProvider, LogLevel, ProcessingMessage, ProcessingReport}
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.google.inject.Inject
import models.responses._
import play.api.libs.json.{Json, _}
import play.api.mvc._

import scala.collection.JavaConverters._
import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.util.Try

class ValidationService @Inject()(val bodyParser: BodyParsers.Default, resources: ResourceService)
                                 (implicit val ec: ExecutionContext) {

  private lazy val addDocumentSchema = resources.getFile("/schemas/addDocumentSchema.json")


  private val factory = JsonSchemaFactory
    .newBuilder()
    .setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.FATAL))
    .freeze()


  private def validateInternallyAgainstSchema(schemaString: String, input: JsValue) = {
    val schemaJson = JsonLoader.fromString(schemaString)
    val json = JsonLoader.fromString(Json.stringify(input))
    val schema = factory.getJsonSchema(schemaJson)
    schema.validate(json, true)
  }

  def validateDocType(docJson: JsValue): Either[BadRequestErrorResponse, Unit] = {
    def getResult(schema: String, docType: String): Either[BadRequestErrorResponse, Unit] = {
      val result = validateInternallyAgainstSchema(schema, docJson)
      if (result.isSuccess) Right(()) else {
        val errors = getJsonObjs(result)
        Left(
          BadRequestErrorResponse(errors, docType)
        )
      }
    }

    (docJson \ "documentMetadata" \ "classIndex").validate[JsObject] match {
      case JsSuccess(x, _) if x.keys("ef") => getResult(addDocumentSchema, "ef")
      case JsSuccess(x, _) if x.keys("nReg") => getResult(addDocumentSchema, "nReg")
      case JsSuccess(x, _) if x.keys("pReg") => getResult(addDocumentSchema, "pReg")
      case JsSuccess(_, _) =>
        val unknownClass = __ \ "documentMetadata" \ "classIndex"
        Left(mappingErrorResponse(JsError(unknownClass, "invalid class type provided").errors,
          None))
      case JsError(errors) =>
        Left(mappingErrorResponse(errors.map {
          case (_, errors) => (__ \ "documentMetadata" \ "classIndex", errors)
        }, getClassDoc(docJson.toString())))
    }
  }

  def getFieldName(processingMessage: ProcessingMessage, prefix: String = ""): String = {
    processingMessage.asJson().get("instance").asScala.map(instanceName => prefix + instanceName.asText).headOption.getOrElse("Field cannot be found")
  }

  def getMissingFields(processingMessage: ProcessingMessage, prefix: String = ""): List[MissingField] = {
    Option(processingMessage.asJson().get("missing")).map(_.asScala.map(
      instanceName => MissingField(path = s"${getFieldName(processingMessage, prefix)}/${instanceName.asText()}")
    ).toList).getOrElse(List())
  }

  def getUnexpectedFields(processingMessage: ProcessingMessage, prefix: String = ""): List[UnexpectedField] = {
    Option(processingMessage.asJson().get("unwanted")).map(_.asScala.map(
      instanceName => UnexpectedField(path = s"${getFieldName(processingMessage, prefix)}/${instanceName.asText()}")
    ).toList).getOrElse(List())
  }

  def getJsonObjs(result: ProcessingReport, prefix: String = ""): immutable.Seq[FieldError] = {
    result.iterator.asScala.toList
      .flatMap {
        error =>
          val missingFields = getMissingFields(error, prefix)
          val unexpectedFields = getUnexpectedFields(error, prefix)
          if (missingFields.isEmpty && unexpectedFields.isEmpty) {
            List(
              InvalidField(getFieldName(error, prefix))
            )
          } else {
            if (missingFields.isEmpty) unexpectedFields else missingFields
          }
      }
  }

  private def mappingErrorResponse(mappingErrors: Seq[(JsPath, Seq[JsonValidationError])], typeOfDoc: Option[String]): BadRequestErrorResponse = {
    val errors = mapErrors(mappingErrors)
    BadRequestErrorResponse(errors, typeOfDoc)
  }

  private def mapErrors(mappingErrors: Seq[(JsPath, Seq[JsonValidationError])]) = {
    mappingErrors.map {
      x => InvalidField(path = x._1.toString())
    }
  }

  def validate[A](input: JsValue, docId: String = "")
                 (implicit rds: Reads[A]): Either[JsValue, Unit] = {
    if (checkDocId(docId)) {
      validateDocType(input.as[JsValue]).flatMap(_ =>
        Json.fromJson[A](input) match {
          case JsSuccess(_, _) => Right(())
          case JsError(errors) =>
            Left(
              BadRequestErrorResponse((mapErrors(errors) ++ List(InvalidPayload())), getClassDoc(input.toString))
            )
        }
      ).fold(
        invalid => {
          Left(Json.toJson(invalid))
        },
        valid => {
          Right(valid)
        })
    } else {
      Left(
        Json.toJson(InvalidDocId())
      )
    }
  }

  def getClassDoc(toFindIn: String): Option[String] = {
    List(""""ef":""", "nReg", "pReg").collectFirst {
      case el if toFindIn.contains(el) => el
    }
  }

  def checkDocId(docId: String) = {
    Try(docId.toLong).isSuccess
  }
}
