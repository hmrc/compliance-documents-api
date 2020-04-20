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

package connectors

import connectors.httpParsers.ComplianceDocumentsConnectorParser
import javax.inject._
import play.api.{Configuration, Logger}
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.http.Status.{ACCEPTED, BAD_REQUEST, NOT_FOUND}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ComplianceDocumentsConnector @Inject()(
                                              httpClient: HttpClient,
                                              config: Configuration
                                            ) extends ComplianceDocumentsConnectorParser {


  override val className: String = this.getClass.getSimpleName

  lazy val bearerToken: String = config.get[String]("integration-framework.auth-token")
  lazy val iFEnvironment: String = config.get[String]("integration-framework.environment")
  lazy val ifBaseUrl: String = config.get[String]("integration-framework.base-url")
  lazy val vatRepaymentUri: String = config.get[String]("integration-framework.endpoints.vat-repayment-info")


  private def headers(correlationId: String) = Seq(
    HeaderNames.CONTENT_TYPE -> ContentTypes.JSON,
    "CorrelationId" -> correlationId,
    "Environment" -> iFEnvironment
  )

  def vatRepayment(request: JsValue, correlationId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[Unit, HttpResponse]] = {
    httpClient.POST[JsValue, HttpResponse](s"$ifBaseUrl$vatRepaymentUri", request, headers(correlationId))(
      implicitly, httpReads(correlationId), hc.copy(authorization = Some(Authorization(s"Bearer $bearerToken"))), ec
    ).map(Right.apply)
      .recover {
        case e: Exception =>
          e.printStackTrace()
          Left(())
      }
  }
}