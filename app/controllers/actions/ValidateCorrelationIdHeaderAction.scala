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

package controllers.actions

import com.google.inject.Inject
import play.api.Logger
import play.api.mvc.Results.BadRequest
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

case class RequestWithCorrelationId[A](request: Request[A], correlationId: String) extends WrappedRequest(request)

class ValidateCorrelationIdHeaderAction @Inject()(val parser: BodyParsers.Default)
                                                 (implicit val executionContext: ExecutionContext)
  extends ActionBuilder[RequestWithCorrelationId, AnyContent] {

  val correlationIdRegex: Regex = """(^[A-Za-z0-9\-]{36}$)""".r

  override def invokeBlock[A](request: Request[A], block: RequestWithCorrelationId[A] => Future[Result]): Future[Result] = {
    request.headers.get("CorrelationId").map {
      case correlationIdRegex(correlationId) =>
        Logger.info(
          ("ValidateCorrelationIdHeaderAction", "invokeBlock", "successfully found CorrelationId in request", correlationId).toString
        )
        block(RequestWithCorrelationId(request, correlationId))
      case x =>
        Logger.warn(
          ("ValidateCorrelationIdHeaderAction", "invokeBlock", s"invalid CorrelationId found in request $x").toString
        )
        Future.successful(BadRequest("Invalid correlation ID!"))
    }.getOrElse {
      Logger.warn(
        ("ValidateCorrelationIdHeaderAction", "invokeBlock", "failed to retrieve CorrelationId in request").toString
      )
      Future.successful(BadRequest("Missing correlation ID!"))
    }
  }
}