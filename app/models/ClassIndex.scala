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

package models

import play.api.Logger
import play.api.libs.json.{JsValue, Json, Reads}

trait ClassIndex

case class EF(
               dTRN: Long,
               locationCode: Option[Short],
               category: Option[String],
               enquiryReference: Option[String],
               caseReference: Option[String]
             ) extends ClassIndex

case class NReg(
                 name: Option[String],
                 postCode: Option[String],
                 callerReference: Option[String],
                 enquiryReference: Option[String],
                 caseReference: Option[String]
               ) extends ClassIndex

case class PReg(
                 caseReference: Option[String],
                 name: Option[String],
                 postCode: Option[String],
                 outcomeStatus: Option[String],
                 riskScore: Option[Short],
                 locationCode: Option[Short]
               ) extends ClassIndex

object ClassIndex {
  implicit def efReads: Reads[EF] = Json.reads[EF]

  implicit def nRegReads: Reads[NReg] = Json.reads[NReg]

  implicit def pRegReads: Reads[PReg] = Json.reads[PReg]

  implicit def classesReads: Reads[ClassIndex] = (json: JsValue) => {
    Logger.debug("\n\n\n Json is " + json + "\n\n\n")

    json.validate[EF] orElse (
      if (json.toString().contains("nReg")) {
        json.validate[NReg]
      }
      else {
        json.validate[PReg]
      }
      )
  }
}
