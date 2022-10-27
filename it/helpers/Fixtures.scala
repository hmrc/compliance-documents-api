/*
 * Copyright 2022 HM Revenue & Customs
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

package helpers

import java.util.UUID

import play.api.libs.json.{JsValue, Json}


trait Fixtures {
  val correlationId: String = UUID.randomUUID().toString

  val createRepaymentDocumentJson: JsValue =
    Json.parse("""{
                 |  "documentBinary": "9743yfshibfkjnjkjklfdjbgsuog==",
                 |  "documentMetadata": {
                 |    "classIndex": {
                 |      "ef": {
                 |        "dTRN": "9443402451823",
                 |        "locationCode": "731",
                 |        "category": "DLEBpc",
                 |        "enquiryReference": "UYp3V0"
                 |      }
                 |    },
                 |    "docType": "VoHl",
                 |    "docDate": "2000-02-29",
                 |    "docBinaryHash": "c186S8wUObpPHoQ6Y/0s+g==",
                 |    "docBinaryRef": "qVX29XN0iireH",
                 |    "docBinaryType": "doc",
                 |    "creatingUser": "YIfD",
                 |    "docDescription": "fS6k2abFoTNuirZSLQw7",
                 |    "docPages": 51255414,
                 |    "allocateToUser": "*AUTO*"
                 |  }
                 |}
                 |""".stripMargin)

}
