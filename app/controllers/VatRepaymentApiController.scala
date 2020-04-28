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
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Request, Result}
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import utils.LoggerHelper._
import controllers.actions.ValidateCorrelationIdHeaderAction
import models.Document
import play.api.Logger
import play.api.http.ContentTypes
import services.ValidationService
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.api.controllers.ErrorInternalServerError
import utils.LoggerHelper

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class VatRepaymentApiController @Inject()(
                                           validator: ValidationService,
                                           complianceDocumentsConnector: ComplianceDocumentsConnector,
                                           appConfig: AppConfig,
                                           getCorrelationId: ValidateCorrelationIdHeaderAction,
                                           cc: ControllerComponents
                                         )(implicit ec: ExecutionContext) extends BackendController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def postRepaymentData(documentId: String): Action[AnyContent] = getCorrelationId.async { implicit request =>
    val input = request.body.asJson.getOrElse(JsNull)
    Logger.info(logProcess("VatRepaymentApiController",
      "postRepaymentData",
      s"Post request received",
      Some(request.correlationId),
      Some(input),
      Some(request.id)))

    validator.validate[Document](input, documentId, request.valid) match {
      case Right(_) =>
        logger.info(logProcess("VatRepaymentApiController", "postRepaymentData: Right",
          s"Request received - passing on to IF", Some(request.correlationId), Some(input), Some(request.id)))
        complianceDocumentsConnector.vatRepayment(input, request.correlationId, documentId.toLong).map {
          _.fold[Result](_ => InternalServerError(Json.toJson(ErrorInternalServerError)), responseMapper)
        }
      case Left(errors) =>
        logger.warn(LoggerHelper.logProcess("VatRepaymentApiController", "postRepaymentData: Left",
          s"request body didn't match json with errors: ${Json.prettyPrint(errors)}",
          Some(request.correlationId), Some(input),
          Some(request.id)))
        Future.successful(BadRequest(errors))
    }
  }

  private def responseMapper(response: HttpResponse): Result = {
    Status(response.status)

      .as(ContentTypes.JSON)
  }


}
