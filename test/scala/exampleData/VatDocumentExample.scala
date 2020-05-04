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

package scala.exampleData

import models.{ClassIndex, Document, DocumentMetadata, EF, NReg, PReg}

object VatDocumentExample {
  val dTRN: String = "9443402451823"
  val locCode: String = "731"
  val docPages: Int = 51255414
  val efInvalid: String =
    s"""
       |      "ef": {
       |        "locationCode": "agfsg",
       |        "category": "a-Category1",
       |        "enquiryReference": "UYp3V0",
       |        "caseReference": "ABC01234"
       |      }
       |""".stripMargin

  val ef: String =
    s"""
       |      "ef": {
       |        "dTRN": "$dTRN",
       |        "locationCode": "$locCode",
       |        "category": "a-Category1",
       |        "enquiryReference": "UYp3V0",
       |        "caseReference": "ABC01234"
       |      }
       |""".stripMargin

  val pRegInvalid: String =
    s"""
       |      "pReg": {
       |        "caseReference": "ABC01234",
       |        "name": "John Doe",
       |        "postCode": "AA99AAASD",
       |        "outcomeStatus": "Un -successful1",
       |        "riskScore": "345",
       |        "locationCode": "$locCode"
       |      }
       |""".stripMargin

  val pReg: String =
    s"""
       |      "pReg": {
       |        "caseReference": "ABC01234",
       |        "name": "John Doe",
       |        "postCode": "AA99AA",
       |        "outcomeStatus": "Un -successful1",
       |        "riskScore": "345",
       |        "locationCode": "$locCode"
       |      }
       |""".stripMargin
  val riskScore: Short = 345

  val pRegValidDocument: Document = Document("0123456789ABCDEF",
    `documentMetadata` = DocumentMetadata(
      PReg(Some("ABC01234"), Some("John Doe"), Some("AA99AA"), Some("Un  -successful1"), Some(riskScore), Some(locCode)),
      "VoHl",
      "2000-02-29",
      Some("fS6k2abFoTNuirZSLQw7"),
      Some(docPages),
      "400e406e13d5835aedfa61bff05299a9",
      "qVX29XN0iireH",
      "doc",
      Some("*AUTO*"),
      "YIfD"
    ))

  val nRegInvalid: String =
    """
      |      "nReg": {
      |        "name": "John Doe",
      |        "postCode": "AA99AA",
      |        "callerReference": "Ref -001",
      |        "enquiryReference": "UYp3V0",
      |        "caseReference": "ABC01234"
      |      }
      |""".stripMargin

  val nReg: String =
    """
      |      "nReg": {
      |        "name": "John Doe",
      |        "postCode": "AA99AA",
      |        "callerReference": "Ref -001",
      |        "enquiryReference": "UYp3V0",
      |        "caseReference": "ABC01234"
      |      }
      |""".stripMargin

  val nRegValidDocument: Document = Document("0123456789ABCDEF",
    `documentMetadata` = DocumentMetadata(
      NReg(Some("John Doe"), Some("AA99AA"), Some("Ref -001"), Some("UYp3V0"), Some("ABC01234")),
      "VoHl",
      "2000-02-29",
      Some("fS6k2abFoTNuirZSLQw7"),
      Some(docPages),
      "400e406e13d5835aedfa61bff05299a9",
      "qVX29XN0iireH",
      "doc",
      Some("*AUTO*"),
      "YIfD"
    ))

  def minWithEmptySpace(classIndex: String, isValidAllocateToUser: Boolean = true) = {
    s"""
       |{
       |  "documentBinary": "0123456789ABCDEF",
       |  "documentMetadata": {
       |    "classIndex": {
       |      $classIndex
       |    },
       |    "docType": "VoHl",
       |    "docDate": "2000-02-29",
       |    "docBinaryHash": "400e406e13d5835aedfa61bff05299a9",
       |    "docBinaryRef": "qVX29XN0iireH",
       |    ${
      if (isValidAllocateToUser) {
        """"docBinaryType": "doc","""
      } else {
        """"docBinaryType": "dododo","""
      }
    }
       |    "creatingUser": "YIfD",
       |    "docDescription": "fS6k2abFoTNuirZSLQw7",
       |    "docPages": 51255414,
       |    ${
      if (isValidAllocateToUser) {
        """"allocateToUser": "*AUTO*""""
      } else {
        """"allocateToUser": "INVALIDINVALIDINVALID""""
      }
    }
       |  }
       |}
       |
       |""".stripMargin
  }

  def invalidWithEmptySpace(classIndex: String) = {
    s"""
       |{
       |  "documentBinary": "0123456789ABCDEF",
       |  "documentMetadata": {
       |    "classIndex": {
       |      $classIndex
       |    },
       |    "allocateToUser": "*AUTO*"
       |  }
       |}
       |
       |""".stripMargin
  }

  val efValidDocument: Document = Document("0123456789ABCDEF",
    `documentMetadata` = DocumentMetadata(
      EF(dTRN, Some(locCode), Some("a-Category1"), Some("UYp3V0"), Some("ABC01234")),
      "VoHl",
      "2000-02-29",
      Some("fS6k2abFoTNuirZSLQw7"),
      Some(docPages),
      "400e406e13d5835aedfa61bff05299a9",
      "qVX29XN0iireH",
      "doc",
      Some("*AUTO*"),
      "YIfD"
    ))


  def getExample(classIndex: String) = classIndex match {
    case "ef" => minWithEmptySpace(ef)
    case "pReg" => minWithEmptySpace(pReg)
    case "nReg" => minWithEmptySpace(nReg)
    case "efInvalid" => minWithEmptySpace(efInvalid)
    case "pRegInvalid" => minWithEmptySpace(pRegInvalid)
    case "nRegInvalid" => minWithEmptySpace(nRegInvalid)
    case "justInvalid" => invalidWithEmptySpace(efInvalid)
    case "invalidNoMissing" => minWithEmptySpace(ef, isValidAllocateToUser = false)
    case _ => ""
  }

  val schema: String =
    """
      |{
      |  "$schema": "http://json-schema.org/draft-07/schema#",
      |  "title": "IF API#1562 Store Document request schema v1.1.0",
      |  "type": "object",
      |  "additionalProperties": false,
      |  "required": [
      |    "documentBinary",
      |    "documentMetadata"
      |  ],
      |  "properties": {
      |    "documentBinary": {
      |      "description": "The actual Base64 encoded document",
      |      "type": "string"
      |    },
      |    "documentMetadata": {
      |      "$ref": "#/definitions/documentMetadata"
      |    }
      |  },
      |  "definitions": {
      |    "documentMetadata": {
      |      "type": "object",
      |      "additionalProperties": false,
      |      "required": [
      |        "classIndex",
      |        "docType",
      |        "docDate",
      |        "docBinaryHash",
      |        "docBinaryRef",
      |        "docBinaryType",
      |        "creatingUser"
      |      ],
      |      "properties": {
      |        "classIndex": {
      |          "description": "Mandatory. Only 1 of either 'ef', 'nReg', or 'pReg' must be supplied",
      |          "oneOf": [
      |            {
      |              "type": "object",
      |              "additionalProperties": false,
      |              "required": [
      |                "ef"
      |              ],
      |              "properties": {
      |                "ef": {
      |                  "$ref": "#/definitions/classIndexEF"
      |                }
      |              }
      |            },
      |            {
      |              "type": "object",
      |              "additionalProperties": false,
      |              "required": [
      |                "nReg"
      |              ],
      |              "properties": {
      |                "nReg": {
      |                  "$ref": "#/definitions/classIndexNReg"
      |                }
      |              }
      |            },
      |            {
      |              "type": "object",
      |              "additionalProperties": false,
      |              "required": [
      |                "pReg"
      |              ],
      |              "properties": {
      |                "pReg": {
      |                  "$ref": "#/definitions/classIndexPReg"
      |                }
      |              }
      |            }
      |          ]
      |        },
      |        "docType": {
      |          "description": "Mandatory. A valid document type between 2 and 4 characters",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9]{2,4}$",
      |          "example": "UCRE"
      |        },
      |        "docDate": {
      |          "description": "Mandatory. Date format CCYY-MM-DD",
      |          "type": "string",
      |          "pattern": "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$"
      |        },
      |        "docDescription": {
      |          "description": "Optional. A brief description of the document.",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9 ,]{1,30}$"
      |        },
      |        "docPages": {
      |          "description": "Optional. If the document is multi-page then the number of pages.",
      |          "type": "integer",
      |          "minimum": 1
      |        },
      |        "docBinaryHash": {
      |          "description": "Mandatory. An MD5 hash checksum of the file used to ensure integrity.",
      |          "type": "string"
      |        },
      |        "docBinaryRef": {
      |          "description": "Mandatory. A Windows filename for the document. Can be a string representation of the 'documentId' URI path parameter.",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9]{1,25}$"
      |        },
      |        "docBinaryType": {
      |          "description": "Mandatory. A Windows filename extension for the document.",
      |          "type": "string",
      |          "enum": [
      |            "html",
      |            "pdf",
      |            "doc",
      |            "xls",
      |            "txt",
      |            "htm",
      |            "mht",
      |            "tif",
      |            "xml"
      |          ],
      |          "example": "pdf"
      |        },
      |        "allocateToUser": {
      |          "description": "User ID of the user or team to whom this document must be allocated for action. Use ‘*AUTO*’ to indicate that the document should be allocated based on automatic routing rules.",
      |          "type": "string",
      |          "pattern": "^([A-Za-z0-9]{4,8})|(\\*AUTO\\*)$",
      |          "example": "*AUTO*"
      |        },
      |        "creatingUser": {
      |          "description": "A user specific to the sending system",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9]{4,8}$",
      |          "example": "PROTCONN"
      |        }
      |      }
      |    },
      |    "classIndexEF": {
      |      "type": "object",
      |      "additionalProperties": false,
      |      "required": [
      |        "dTRN"
      |      ],
      |      "properties": {
      |        "dTRN": {
      |          "description": "Mandatory. The trader’s registration number including VAT number and suffix",
      |          "type": "string",
      |          "pattern": "^[0-9]{13}$",
      |          "example": "4563845950000"
      |        },
      |        "locationCode": {
      |          "$ref": "#/definitions/locationCodeType"
      |        },
      |        "category": {
      |          "description": "Optional. A valid category defined on the backend",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9 -]{1,12}$"
      |        },
      |        "enquiryReference": {
      |          "$ref": "#/definitions/enquiryReferenceType"
      |        },
      |        "caseReference": {
      |          "$ref": "#/definitions/caseReferenceType"
      |        }
      |      }
      |    },
      |    "classIndexNReg": {
      |      "type": "object",
      |      "additionalProperties": false,
      |      "properties": {
      |        "name": {
      |          "description": "Optional.",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9 '.&/-]{1,105}$"
      |        },
      |        "postCode": {
      |          "$ref": "#/definitions/postCodeType"
      |        },
      |        "callerReference": {
      |          "description": "Optional.",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9 -]{1,8}$"
      |        },
      |        "enquiryReference": {
      |          "$ref": "#/definitions/enquiryReferenceType"
      |        },
      |        "caseReference": {
      |          "$ref": "#/definitions/caseReferenceType"
      |        }
      |      }
      |    },
      |    "classIndexPReg": {
      |      "type": "object",
      |      "additionalProperties": false,
      |      "properties": {
      |        "caseReference": {
      |          "$ref": "#/definitions/caseReferenceType"
      |        },
      |        "name": {
      |          "description": "Optional.",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9 '.&/-]{1,105}$"
      |        },
      |        "postCode": {
      |          "$ref": "#/definitions/postCodeType"
      |        },
      |        "outcomeStatus": {
      |          "description": "Optional.",
      |          "type": "string",
      |          "pattern": "^[A-Za-z0-9 -]{1,20}$"
      |        },
      |        "riskScore": {
      |          "description": "Optional.",
      |          "type": "string",
      |          "pattern": "^[0-9]{3}$"
      |        },
      |        "locationCode": {
      |          "$ref": "#/definitions/locationCodeType"
      |        }
      |      }
      |    },
      |    "locationCodeType": {
      |      "description": "Optional. Trader location code; it must be padded with leading zeros if less than 100, for example '081'",
      |      "type": "string",
      |      "pattern": "^[0-9]{3}$",
      |      "example": "250"
      |    },
      |    "enquiryReferenceType": {
      |      "description": "Optional. Used for storing the reference of a contact centre enquiry",
      |      "type": "string",
      |      "pattern": "^[A-Za-z0-9 -]{1,12}$"
      |    },
      |    "caseReferenceType": {
      |      "description": "Optional. Used for identifying the case the document is associated with",
      |      "type": "string",
      |      "pattern": "^[A-Za-z0-9 -]{1,14}$"
      |    },
      |    "postCodeType": {
      |      "description": "Optional. Used for identifying the case the document is associated with",
      |      "type": "string",
      |      "pattern": "^[A-Z]{1,2}[0-9][0-9A-Z]?\\s?[0-9][A-Z]{2}$"
      |    },
      |    "errorResponse": {
      |      "type": "object",
      |      "additionalProperties": false,
      |      "required": [
      |        "failures"
      |      ],
      |      "properties": {
      |        "failures": {
      |          "type": "array",
      |          "minItems": 1,
      |          "uniqueItems": true,
      |          "items": {
      |            "type": "object",
      |            "additionalProperties": false,
      |            "required": [
      |              "code",
      |              "reason"
      |            ],
      |            "properties": {
      |              "code": {
      |                "description": "Keys for all the errors returned",
      |                "type": "string",
      |                "pattern": "^[A-Z0-9_-]{1,160}$"
      |              },
      |              "reason": {
      |                "description": "A simple description for the failure",
      |                "type": "string",
      |                "minLength": 1,
      |                "maxLength": 160
      |              }
      |            }
      |          }
      |        }
      |      }
      |    }
      |  }
      |}
      |""".stripMargin
  val invalidSchema: String =
    """
      |{
      |    "$schema": "http://json-schema.org/draft-07/schema",
      |    "$id": "http://example.com/example.json",
      |    "type": "object",
      |    "title": "The Root Schema",
      |    "description": "The root schema comprises the entire JSON document.",
      |    "default": {},
      |    "additionalProperties": true,
      |    "required": [
      |        "documentBinary",
      |        "documentMetadata"
      |    ],
      |    "properties": {
      |        "documentBinary": {
      |            "$id": "#/properties/documentBinary",
      |            "type": "string",
      |            "title": "The Documentbinary Schema",
      |            "description": "An explanation about the purpose of this instance.",
      |            "default": "",
      |            "examples": [
      |                "0123456789ABCDEF"
      |            ]
      |        },
      |        "documentMetadata": {
      |            "$id": "#/properties/documentMetadata",
      |            "type": "object",
      |            "title": "The Documentmetadata Schema",
      |            "description": "An explanation about the purpose of this instance.",
      |            "default": {},
      |            "examples": [
      |                {
      |                    "classIndex": {
      |                        "ef": {
      |                            "dTRN": "9443402451823"
      |                        }
      |                    }
      |                }
      |            ],
      |            "additionalProperties": true,
      |            "required": [
      |                "classIndex"
      |            ],
      |            "properties": {
      |                "classIndex": {
      |                    "$id": "#/properties/documentMetadata/properties/classIndex",
      |                    "type": "object",
      |                    "title": "The Classindex Schema",
      |                    "description": "An explanation about the purpose of this instance.",
      |                    "default": {},
      |                    "examples": [
      |                        {
      |                            "ef": {
      |                                "dTRN": "9443402451823"
      |                            }
      |                        }
      |                    ],
      |                    "additionalProperties": true,
      |                    "required": [
      |                        "ef"
      |                    ],
      |                    "properties": {
      |                        "ef": {
      |                            "$id": "#/properties/documentMetadata/properties/classIndex/properties/ef",
      |                            "type": "object",
      |                            "title": "The Ef Schema",
      |                            "description": "An explanation about the purpose of this instance.",
      |                            "default": {},
      |                            "examples": [
      |                                {
      |                                    "dTRN": "9443402451823"
      |                                }
      |                            ],
      |                            "additionalProperties": true,
      |                            "required": [
      |                                "dTRN"
      |                            ],
      |                            "properties": {
      |                                "dTRN": {
      |                                    "$id": "#/properties/documentMetadata/properties/classIndex/properties/ef/properties/dTRN",
      |                                    "type": "string",
      |                                    "title": "The Dtrn Schema",
      |                                    "description": "An explanation about the purpose of this instance.",
      |                                    "default": "",
      |                                    "examples": [
      |                                        "9443402451823"
      |                                    ]
      |                                }
      |                            }
      |                        }
      |                    }
      |                }
      |            }
      |        }
      |    }
      |}
      |""".stripMargin
  val fitsInvalidSchema: String =
    """
      |{
      |  "documentBinary": "0123456789ABCDEF",
      |  "documentMetadata": {
      |    "classIndex": {
      |      "ef": {
      |        "dTRN": "9443402451823"
      |      }
      |    }
      |  }
      |}
      |""".stripMargin
}
