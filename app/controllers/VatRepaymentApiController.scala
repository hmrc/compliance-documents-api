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

package controllers

import config.AppConfig
import connectors.ComplianceDocumentsConnector
import javax.inject._
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import java.util.UUID

import controllers.actions.ValidateCorrelationIdHeaderAction
import play.api.Logger
import play.api.http.ContentTypes
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class VatRepaymentApiController @Inject()(
                                           complianceDocumentsConnector: ComplianceDocumentsConnector,
                                           appConfig: AppConfig,
                                           getCorrelationId: ValidateCorrelationIdHeaderAction,
                                           cc: ControllerComponents
                                         )(implicit ec: ExecutionContext) extends BackendController(cc) {

  def postRepaymentData(): Action[AnyContent] = getCorrelationId.async { implicit request =>
    val input = request.body.asJson.getOrElse(JsNull)

    Logger.debug(s"Input for controller postRepaymentData: $input")

    input match {
      case JsNull => Future.successful(BadRequest)
      case _ =>
        Logger.info("Request received - passing on to IF.")
        complianceDocumentsConnector.vatRepayment(Json.toJson(input), request.correlationId).map {
          _.fold[Result](_ => InternalServerError(Json.toJson("error" -> "internal server error test")), mappingConnectorResponse)
        }
    }

  }

  private def mappingConnectorResponse(response: HttpResponse): Result = {
    val excludedHeaders = List(CONTENT_TYPE, CONTENT_LENGTH)

    Logger.debug(s"Excluded: $excludedHeaders")

    val headers = for {
      (key, values) <- response.allHeaders
      if !excludedHeaders.contains(key)
    } yield key -> values.mkString(", ")

    Status(response.status)
      .apply(response.body)
      .withHeaders(headers.toList: _*)
      .as(ContentTypes.JSON)
  }


}
