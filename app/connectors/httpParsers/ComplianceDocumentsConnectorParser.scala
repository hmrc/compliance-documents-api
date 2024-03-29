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

package connectors.httpParsers

import play.api.Logger
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, NOT_FOUND}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.LoggerHelper._

trait ComplianceDocumentsConnectorParser {
  val className: String
  val logger: Logger

  def httpReads(correlationId: String): HttpReads[Option[HttpResponse]] = (_, url, response) => {

    response.status match {
      case NOT_FOUND => {
        logger.warn(
          logProcess(className, "connector parser",
            s"received a not found status when calling $url ( IF_VAT_REPAYMENT_ENDPOINT_NOT_FOUND_RESPONSE )",
            Some(correlationId))
        )
        None
      }
      case BAD_REQUEST => {
        logger.warn(
          logProcess(className, "connector parser",
            s"received a bad request status when calling $url ( IF_VAT_REPAYMENT_ENDPOINT_BAD_REQUEST_RESPONSE )",
            Some(correlationId))
        )
        None
      }
      case status if status != ACCEPTED => {
        logger.warn(
          logProcess(className, "connector parser",
            s"received status $status when calling $url ( IF_VAT_REPAYMENT_ENDPOINT_UNEXPECTED_RESPONSE )",
            Some(correlationId))
        )
        None
      }
      case _ => {
        logger.info(logProcess(className, "connector parser",
          s"received an accepted when calling $url",
          Some(correlationId)))
        Some(response)
      }
    }

  }
}
