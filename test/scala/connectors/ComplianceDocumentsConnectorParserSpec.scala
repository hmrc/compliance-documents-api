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

package scala.connectors

import connectors.httpParsers.ComplianceDocumentsConnectorParser
import org.apache.pekko.event.Logging
import play.api.http.Status.*
import uk.gov.hmrc.http.HttpResponse
import utils.LoggerHelper.logProcess

import scala.utils.BaseSpec.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.play.bootstrap.tools.{LogCapturing}
import play.api.Logger
import play.api.LoggerLike
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as Logger2
import org.apache.pekko.event.Logging.LogEvent
import org.scalatest.matchers.should.Matchers.should

class ComplianceDocumentsConnectorParserSpec extends SpecBase with MockitoSugar with LogCapturing {

  val testClassName = "TestComplianceDocumentsConnectorParser"
  val testUrl = "http://test-url"

  class TestLogger extends LoggerLike {
    // Create a play.api.Logger directly
     override val logger: Logger2 = Logger("test-logger")

    // Use the SLF4J logger for log capturing
    def logbackLogger: Logger2 = LoggerFactory.getLogger("test-logger").asInstanceOf[Logger2]
  }

  "ComplianceDocumentsConnectorParser" - {

    "log a warning for NOT_FOUND status" in {
      val correlationId = "testCorrelationId"
      val testLogger = new TestLogger
      val testParser = new ComplianceDocumentsConnectorParser {
        override val className: String = "TestComplianceDocumentsConnectorParser"
        override val logger = testLogger.logger
      }

      val response = HttpResponse(NOT_FOUND, "")

      withCaptureOfLoggingFrom(testLogger.logbackLogger) { events =>
        testParser.httpReads(correlationId).read("GET", testUrl, response)

        val expectedLogMessage = logProcess(
          testClassName,
          "connector parser",
          s"received a not found status when calling $testUrl ( IF_VAT_REPAYMENT_ENDPOINT_NOT_FOUND_RESPONSE )",
          Some(correlationId)
        )
        events.map(_.getMessage) should contain(expectedLogMessage)
      }
    }

    "log a warning for BAD_REQUEST status" in {
      val correlationId = "testCorrelationId"
      val testLogger = new TestLogger
      val testParser = new ComplianceDocumentsConnectorParser {
        override val className: String = "TestComplianceDocumentsConnectorParser"
        override val logger = testLogger.logger
      }

      val response = HttpResponse(BAD_REQUEST, "")

      withCaptureOfLoggingFrom(testLogger.logbackLogger) { events =>
        testParser.httpReads(correlationId).read("GET", testUrl, response)

        val expectedLogMessage = logProcess(
          testClassName,
          "connector parser",
          s"received a bad request status when calling $testUrl ( IF_VAT_REPAYMENT_ENDPOINT_BAD_REQUEST_RESPONSE )",
          Some(correlationId)
        )
        events.map(_.getMessage) should contain(expectedLogMessage)
      }
    }

    "log a warning for unexpected status (e.g., 500 status)" in {
      val correlationId = "testCorrelationId"
      val testLogger = new TestLogger
      val testParser = new ComplianceDocumentsConnectorParser {
        override val className: String = "TestComplianceDocumentsConnectorParser"
        override val logger = testLogger.logger
      }

      val response = HttpResponse(INTERNAL_SERVER_ERROR, "")

      withCaptureOfLoggingFrom(testLogger.logbackLogger) { events =>
        testParser.httpReads(correlationId).read("GET", testUrl, response)

        val expectedLogMessage = logProcess(
          testClassName,
          "connector parser",
          s"received status 500 when calling $testUrl ( IF_VAT_REPAYMENT_ENDPOINT_UNEXPECTED_RESPONSE )",
          Some(correlationId)
        )
        events.map(_.getMessage) should contain(expectedLogMessage)
      }
    }

    "log an info message for ACCEPTED status" in {
      val correlationId = "testCorrelationId"
      val testLogger = new TestLogger
      val testParser = new ComplianceDocumentsConnectorParser {
        override val className: String = "TestComplianceDocumentsConnectorParser"
        override val logger = testLogger.logger
      }

      val response = HttpResponse(ACCEPTED, "")

      withCaptureOfLoggingFrom(testLogger.logbackLogger) { events =>
        testParser.httpReads(correlationId).read("GET", testUrl, response)

        val expectedLogMessage = logProcess(
          testClassName,
          "connector parser",
          s"received an accepted when calling $testUrl",
          Some(correlationId)
        )
        events.map(_.getMessage) should contain(expectedLogMessage)
      }
    }
  }
}
