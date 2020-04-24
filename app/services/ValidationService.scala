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

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.processing.ProcessingResult
import com.github.fge.jsonschema.core.report.{ListReportProvider, LogLevel, ProcessingMessage, ProcessingReport}
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import com.google.inject.Inject
import models.{ClassIndex, Document, EF, NReg, PReg}
import models.responses._
import play.api.Logger
import play.api.libs.json._
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
  private val logger = Logger(this.getClass)


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
        val errors = getJsonObjs(result, "/documentMetadata/classIndex")
        errors.foreach(g => logger.error(g.toString()))
        Left(
          BadRequestErrorResponse(errors, docType)
        )
      }
    }

    Logger.debug("Validate doc type" + docJson.toString)
    (docJson \ "documentMetadata" \ "classIndex").validate[ClassIndex] match {
      case JsSuccess(EF(_, _, _, _, _), _) => getResult(addDocumentSchema, "ef")
      case JsSuccess(NReg(_, _, _, _, _), _) => getResult(addDocumentSchema, "nReg")
      case JsSuccess(PReg(_, _, _, _, _, _), _) => getResult(addDocumentSchema, "pReg")
      //below case appears impossible?
      //      case JsSuccess(_, _) => Left(mappingErrorResponse(JsError(__ \ "documentMetadata" \ "classIndex", "invalid doc type provided").errors,
      //        getClassDoc(docJson.toString())))
      case JsError(errors) => Left(mappingErrorResponse(errors.map {
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


  def getJsonObjs(result: ProcessingReport, prefix: String = ""): immutable.Seq[FieldError] = {
    result.iterator.asScala.toList
      .flatMap {
        error =>
          val missingFields = getMissingFields(error, prefix)
          if (missingFields.isEmpty) {
            List(
              InvalidField(getFieldName(error, prefix))
            )
          } else {
            missingFields
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

  def validate[A](input: JsValue, docId: String = "", validCorrelationId: Boolean = true, schemaString: String = addDocumentSchema)
                 (implicit rds: Reads[A]): Either[JsValue, Unit] = {
    if (checkDocId(docId) && validCorrelationId) {
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
      val result = validateInternallyAgainstSchema(schemaString, input)
      //Uncomment if want to log request json
      //      logger.debug(Json.prettyPrint(input))
      Logger.debug(result.toString)
      val errors = getAllNonJsonErrors(result, validCorrelationId, docId)
      errors.foreach(g => logger.error(g.toString()))
      Left(
        Json.toJson(BadRequestCorrDoc(errors))
      )
    }
  }

  def getClassDoc(toFindIn: String): Option[String] = {
    List(""""ef":""", "nReg", "pReg").map(el => if (toFindIn.contains(el)) el else "").find(el => el.nonEmpty)

  }

  def getAllNonJsonErrors(result: ProcessingReport, validCorrelationId: Boolean, docId: String) = {
    List(addCorrIdError(validCorrelationId, _), addDocIdError(docId, _))
      .foldLeft(checkPayload(result)) { (previous, function) => function(previous) }

  }

  def checkDocId(docId: String) = {
    Try(docId.toLong).isSuccess
  }

  def addDocIdError(docId: String, errors: Seq[OtherError]) = {
    if (!checkDocId(docId)) errors ++ List(InvalidDocId()) else errors
  }

  def addCorrIdError(validCorrelationId: Boolean, errors: Seq[OtherError]) = {
    if (!validCorrelationId) errors ++ List(InvalidCorrelationId()) else errors
  }

  def checkPayload(result: ProcessingReport, prefix: String = "") = {
    result.iterator.asScala.toList.headOption match {
      case Some(_) =>
        List(InvalidPayload()).asInstanceOf[Seq[OtherError]]
      case None =>
        List()
    }
  }

}
