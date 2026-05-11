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

package services

import com.networknt.schema.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.{SchemaRegistry, SpecificationVersion}
import com.google.inject.Inject
import models.responses._
import play.api.libs.json.{Json, _}
import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

class ValidationService @Inject()(resources: ResourceService) {

  private lazy val efSchema = resources.getFile("/schemas/efSchema.json")
  private lazy val nRegSchema = resources.getFile("/schemas/nRegSchema.json")
  private lazy val pRegSchema = resources.getFile("/schemas/pRegSchema.json")
  private lazy val addDocumentSchemaNoClassType = resources.getFile("/schemas/addDocumentSchemaNoClassType.json")


  private val mapper: ObjectMapper = new ObjectMapper()
  private val schemaRegistry: SchemaRegistry =
    SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4)

  private def validateInternallyAgainstSchema(schemaString: String, input: JsValue): java.util.List[Error] = {
    val schemaNode = mapper.readTree(schemaString)
    val json = mapper.readTree(Json.stringify(input))
    val schema = schemaRegistry.getSchema(schemaNode)
    schema.validate(json)
  }

  def getFieldName(processingMessage: Error, prefix: String = ""): String = {
    Option(processingMessage.getInstanceLocation).map(instanceName => prefix + instanceName.toString).getOrElse("Field cannot be found")
  }

  def getMissingFields(processingMessage: Error, prefix: String = ""): List[MissingField] = {
    if (processingMessage.getKeyword == "required") {
      Option(processingMessage.getProperty).map(
        instanceName => List(MissingField(path = s"${getFieldName(processingMessage, prefix)}/$instanceName"))
      ).getOrElse(Nil)
    } else {
      Nil
    }
  }

  def getUnexpectedFields(processingMessage: Error, prefix: String = ""): List[UnexpectedField] = {
    if (processingMessage.getKeyword == "additionalProperties") {
      Option(processingMessage.getProperty).map(
        instanceName => List(UnexpectedField(path = s"${getFieldName(processingMessage, prefix)}/$instanceName"))
      ).getOrElse(Nil)
    } else {
      Nil
    }
  }

  def getFieldErrorsFromReport(errors: java.util.List[Error], prefix: String = ""): Seq[FieldError] = {
    val result = errors.asScala.toList
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
      .sortBy {
        case _: MissingField => (0, "")
        case _: UnexpectedField => (1, "")
        case i: InvalidField => (2, i.path)
      }

    val invalidPaths = result.collect { case f: InvalidField => f.path }
    result.filterNot(e => invalidPaths.exists(p => e.path.startsWith(p + "/")))
  }
  
  @nowarn("msg=match may not be exhaustive")
  def validateDocType(docJson: JsValue): Either[BadRequestErrorResponse, Unit] = {
    def getResult(schema: String): Either[BadRequestErrorResponse, Unit] = {
      val result = validateInternallyAgainstSchema(schema, (docJson \ "documentMetadata" \ "classIndex").as[JsValue])
      if (!result.isEmpty) {
        Left(BadRequestErrorResponse(getFieldErrorsFromReport(result, "/documentMetadata/classIndex"), getClassDoc(docJson)))
      } else {
        Right(())
      }
    }

    (docJson \ "documentMetadata" \ "classIndex").validate[JsObject] match {
      case JsSuccess(x, _) if x.keys("ef") => getResult(efSchema)
      case JsSuccess(x, _) if x.keys("pReg") => getResult(pRegSchema)
      case JsSuccess(x, _) if x.keys("nReg") => getResult(nRegSchema)
      case JsError(errors) =>
        Left(mappingErrorResponse(errors.toSeq.map {
          case (_, errors) =>
            (__ \ "documentMetadata" \ "classIndex", errors.toSeq)
        }, getClassDoc(docJson)))
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

  def validateJsonObj(input: JsValue): Option[JsValue] = {
    input.asOpt[JsObject]

  }


  def validate(input: JsValue, docId: String = ""): Option[JsValue] = {
    if (checkDocIdMatchesRegex(docId)) {
      if (validateJsonObj(input).isDefined) {
        val result = validateInternallyAgainstSchema(addDocumentSchemaNoClassType, input)
        if (result.isEmpty) {
          validateDocType(input)
            .fold(invalid => Some(Json.toJson(invalid)), _ => None)
        } else {
          Some(
            Json.toJson[BadRequestErrorResponse](BadRequestErrorResponse(getFieldErrorsFromReport(result), None))
          )
        }
      }
      else {
        Some(Json.toJson[OtherError](InvalidJsonType()))
      }

    } else {
      Some(
        Json.toJson[OtherError] (InvalidDocId())
      )
    }
  }

  def getClassDoc(toFindIn: JsValue): Option[String] = {
    List("ef", "nReg", "pReg").collectFirst {
      case el if (toFindIn \ "documentMetadata" \ "classIndex").asOpt[JsObject]
        .fold(ifEmpty = false)(classIndex => classIndex.keys(el)) => el

    }
  }

  def checkDocIdMatchesRegex(docId: String): Boolean = {
    docId.matches("^(([0-9]{1,19})|(1[0-7][0-9]{18})|(18[0-3][0-9]{17})|(184[0-3][0-9]{16}))$")
  }
}
