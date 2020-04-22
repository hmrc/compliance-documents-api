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

package connectors.httpParsers

import play.api.Logger
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, ACCEPTED}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

trait ComplianceDocumentsConnectorParser {
  val className: String

  def httpReads(correlationId: String): HttpReads[HttpResponse] = (_, url, response) => {
    def logMessage(message: String): String = message + ", class: " + className + ", correlation id: " + correlationId

    response.status match {
      case NOT_FOUND => Logger.warn(
        logMessage(s"received a not found status when calling $url ( IF_VAT_REPAYMENT_ENDPOINT_NOT_FOUND_RESPONSE )")
      )
      case BAD_REQUEST => Logger.warn(
        logMessage(s"received a bad request status when calling $url ( IF_VAT_REPAYMENT_ENDPOINT_BAD_REQUEST_RESPONSE )")
      )
      case status if status != ACCEPTED => Logger.warn(
        logMessage(s"received status $status when calling $url")
      )
      case _ => Logger.info(logMessage(s"received an accepted when calling $url"))
    }

    response
  }
}
