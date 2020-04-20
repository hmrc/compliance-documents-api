package helpers

import java.util.UUID

import play.api.libs.json.{JsValue, Json}


trait Fixtures {
  val correlationId: String = UUID.randomUUID().toString

  val createRepaymentDocumentJson: JsValue =
    Json.parse("""{
                 |  "documentBinary": "0123456789ABCDEF",
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
                 |    "docBinaryHash": "abcdef01234567890",
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
