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

package models.responses

import play.api.libs.json.{Json, Writes}

case class BadRequestErrorResponse(code: String, message: String, Errors: Seq[OtherError])

case class BadRequestCorrDoc(message: String, Errors: Seq[OtherError])

object BadRequestCorrDoc {
  implicit def badRequestCorrDocWrites: Writes[BadRequestCorrDoc] = Json.writes[BadRequestCorrDoc]

  def apply(errors: Seq[OtherError]) = {
    new BadRequestCorrDoc("Unable to process request.", errors)
  }
}

object BadRequestErrorResponse {
  implicit def badRequestWrites: Writes[BadRequestErrorResponse] = Json.writes[BadRequestErrorResponse]



  def apply(errors: Seq[OtherError], classDocument: String): BadRequestErrorResponse = {
    new BadRequestErrorResponse("JSON_VALIDATION_ERROR",
      s"The provided JSON was unable to be validated as the $classDocument model.",
      errors
    )
  }

  def apply(errors: Seq[OtherError], classDocument: Option[String]): BadRequestErrorResponse = {
    val message = if (classDocument.isDefined) {
      s"The provided JSON was unable to be validated as the ${classDocument.get.filter(char => char.isLetter)} model."
    }
    else {
      "The provided JSON was unable to be validated."
    }
    new BadRequestErrorResponse("JSON_VALIDATION_ERROR",
      message,
      errors
    )
  }


}