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

class FieldError(val code: String, val reason: String)

case class MissingField()
  extends FieldError(code = "MISSING_FIELD", reason = "field not present")

case class InvalidField()
  extends FieldError(code = "BAD_REQUEST", reason = "an invalid value provided")

case class InvalidDocId()
  extends FieldError(code = "INVALID_DOCUMENTID", reason = "Submission has not passed validation. Invalid parameter documentId.")

case class InvalidPayload()
  extends FieldError(code = "INVALID_PAYLOAD", reason = "Submission has not passed validation. Invalid payload.")

case class InvalidCorrelationId()
  extends FieldError(code = "INVALID_CORRELATIONID", reason = "Submission has not passed validation. Invalid Header CorrelationId.")

object FieldError {
  implicit def invalidFieldWrites: Writes[FieldError] = (fieldError: FieldError) => {
    Json.obj(
      "code" -> fieldError.code,
      "reason" -> fieldError.reason
    )
  }
}
