/*
 * Copyright 2021 HM Revenue & Customs
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

import models.responses.{ErrorInternalServerError, ErrorUnauthorized}

import javax.inject.Inject
import org.slf4j.MDC
import play.api.libs.json.Json
import play.api.mvc.Results.{InternalServerError, Unauthorized}
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProvider, AuthProviders, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.LoggerHelper._

import scala.concurrent.{ExecutionContext, Future}


class AuthenticateApplicationAction @Inject()(
  val authConnector: AuthConnector,
  config: Configuration,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext) extends
  AuthorisedFunctions with ActionBuilder[Request, AnyContent] {
  lazy val applicationIdIsAllowed: Set[String] = config.get[Option[Seq[String]]]("apiDefinition.allowListedApplicationIds")
    .getOrElse(Seq.empty[String])
    .toSet
  val logger: Logger = Logger.apply(this.getClass.getSimpleName)

  private[actions] def updateContextWithRequestId(implicit hc: HeaderCarrier): Unit = {
    if(Option(MDC.getCopyOfContextMap).isEmpty || MDC.getCopyOfContextMap.isEmpty) {
      hc.requestId.foreach(id => MDC.put(HeaderNames.xRequestId, id.value))
      hc.sessionId.foreach(id => MDC.put(HeaderNames.xSessionId, id.value))
    }
  }

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSessionAndRequest(request.headers, request = Some(request))

    updateContextWithRequestId

    authorised(AuthProviders(AuthProvider.StandardApplication)).retrieve(Retrievals.applicationId) {
      case Some(applicationId) if applicationIdIsAllowed(applicationId) =>
        block(request)
      case _ =>
        logger.warn(
          logProcess("AuthenticateApplicationAction", "invokeBlock", "no application id or application id not in request")
        )
        Future.successful(Unauthorized(Json.toJson(ErrorUnauthorized)))
    } recover {
      case _: AuthorisationException =>
        logger.warn(
          logProcess("AuthenticateApplicationAction", "invokeBlock", "no application id or application id not in request")
        )
        Unauthorized(Json.toJson(ErrorUnauthorized))
      case e: Throwable =>
        logger.warn(
          logProcess("AuthenticateApplicationAction", "invokeBlock", s"an unexpected exception occurred: $e")
        )
        InternalServerError(Json.toJson(ErrorInternalServerError))
    }
  }

}
