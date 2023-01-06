/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, Writes}

abstract class DefaultErrorResponse(
  val httpStatusCode: Int,
  val errorCode:      String,
  val message:        String
)

case object ErrorUnauthorized extends DefaultErrorResponse(UNAUTHORIZED, "UNAUTHORIZED", "Bearer token is missing or not authorized")

case class ErrorGenericBadRequest(msg: String = "Bad Request") extends DefaultErrorResponse(BAD_REQUEST, "BAD_REQUEST", msg)

case object ErrorInternalServerError extends DefaultErrorResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error")

object DefaultErrorResponse {
  implicit val writes = new Writes[DefaultErrorResponse] {
    def writes(e: DefaultErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}