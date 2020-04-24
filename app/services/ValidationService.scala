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
import com.github.fge.jsonschema.core.report.{ListReportProvider, LogLevel, ProcessingMessage, ProcessingReport}
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import com.google.inject.Inject
import models.Document
import models.responses.{BadRequestErrorResponse, FieldError, InvalidCorrelationId, InvalidDocId, InvalidField, InvalidPayload, MissingField}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConverters._
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

  def validate[A](input: JsValue, docId: String = "", validCorrelationId: Boolean = true, schemaString: String = addDocumentSchema)
                 (implicit rds: Reads[A]): Either[JsValue, Unit] = {
    val result = validateInternallyAgainstSchema(schemaString, input)
    if (result.isSuccess && checkDocId(docId) && validCorrelationId) {
      Json.fromJson[A](input) match {
        case JsSuccess(_, _) =>
          Right(())
        case JsError(_) => Left(
          Json.toJson(BadRequestErrorResponse(List(InvalidPayload())))
        )
      }
    } else {
      //Uncomment if want to log request json
      //      logger.debug(Json.prettyPrint(input))
      val errors = checkPayload(result)
      val errorsrs = List(addCorrIdError(validCorrelationId, _), addDocIdError(docId, _)).foldLeft(errors) { (previous, function) => function(previous) }
      errors.foreach(g => logger.error(g.toString()))
      Left(
        Json.toJson(BadRequestErrorResponse(errorsrs))
      )
    }
  }

  def checkDocId(docId: String) = {
    Try(docId.toLong).isSuccess
  }

  def addDocIdError(docId: String, errors: Seq[FieldError]) = {
    if (!checkDocId(docId)) errors ++ List(InvalidDocId()) else errors
  }

  def addCorrIdError(validCorrelationId: Boolean, errors: Seq[FieldError]) = {
    if (!validCorrelationId) errors ++ List(InvalidCorrelationId()) else errors
  }

  def checkPayload(result: ProcessingReport, prefix: String = "") = {
    result.iterator.asScala.toList.headOption match {
      case Some(_) =>
        List(InvalidPayload()).asInstanceOf[Seq[FieldError]]
      case None => List()
    }
  }

}
