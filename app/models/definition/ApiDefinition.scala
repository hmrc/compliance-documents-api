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

package models.definition

import play.api.libs.json.{JsValue, Json, Writes}

case class ApiDefinition(endpointsEnabled: Boolean, status: String) {
  val apiConfig: JsValue = Json.obj(
    "name" -> "Compliance Documents",
    "description" -> "Api to manage vat repayment documents sent to EF",
    "context" -> "misc/compliance-documents",
    "categories" -> Json.arr("PRIVATE_GOVERNMENT"),
    "versions" -> Json.arr(
      Json.obj(
        "version" -> "1.0",
        "status" -> status,
        "endpointsEnabled" -> endpointsEnabled,
        "access" -> Json.obj(
          "type" -> "PRIVATE"
        )
      )
    )
  )
}


object ApiDefinition {
  implicit def definitionWrites: Writes[ApiDefinition] = (definition: ApiDefinition) => {
    Json.obj(
      "api" -> definition.apiConfig
    )
  }
}
