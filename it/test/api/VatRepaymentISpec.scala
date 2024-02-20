/*
 * Copyright 2022 HM Revenue & Customs
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

package test.api

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.Helpers._
import test.helpers.{Fixtures, WireMockSpec}
import akka.util.Timeout

import scala.concurrent.duration.Duration

class VatRepaymentISpec extends AnyWordSpec with Matchers with WireMockSpec with Fixtures {
  implicit val timeout:Timeout = Timeout.durationToTimeout(Duration.create(30,"s"))
  "POST /vat-repayment-info/(id)" should {
    s"return an $ACCEPTED if $ACCEPTED received from IF" in {
      stubPostWithoutResponseBody("/organisations/document/4321", ACCEPTED, correlationId)
      stubPostWithResponseBodyNoHeaders("/auth/authorise", ACCEPTED, Json.obj(
        "applicationId" -> "ID-1"
      ).toString)
      stubPostWithoutRequestAndResponseBody("/write/audit", NO_CONTENT)
      stubPostWithoutRequestAndResponseBody("/write/audit/merged", NO_CONTENT)

      val response = await(buildClient("/vat-repayment-info/4321")
        .withHttpHeaders("CorrelationId" -> correlationId, AUTHORIZATION -> "Bearer some-token")
        .post(createRepaymentDocumentJson))

      response.status mustBe ACCEPTED
    }

    s"return a $INTERNAL_SERVER_ERROR if $BAD_REQUEST received from IF" in {
      stubPostWithResponseBody("/organisations/document/4321", BAD_REQUEST, correlationId, Json.obj(
        "code" -> "BAD_REQUEST", "message" -> "Bad request test!"
      ).toString)
      stubPostWithResponseBodyNoHeaders("/auth/authorise", ACCEPTED, Json.obj(
        "applicationId" -> "ID-1"
      ).toString)
      stubPostWithoutRequestAndResponseBody("/write/audit", NO_CONTENT)
      stubPostWithoutRequestAndResponseBody("/write/audit/merged", NO_CONTENT)

      val response = await(buildClient("/vat-repayment-info/4321")
        .withHttpHeaders("CorrelationId" -> correlationId, AUTHORIZATION -> "Bearer some-token")
        .post(createRepaymentDocumentJson))

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    s"return a $UNAUTHORIZED if application is not authorised" in {
      stubPostWithResponseBodyNoHeaders("/auth/authorise", UNAUTHORIZED, "{}")
      stubPostWithoutRequestAndResponseBody("/write/audit", NO_CONTENT)
      stubPostWithoutRequestAndResponseBody("/write/audit/merged", NO_CONTENT)

      val response = await(buildClient("/vat-repayment-info/4321")
        .withHttpHeaders("CorrelationId" -> correlationId)
        .post(createRepaymentDocumentJson))

      response.status mustBe UNAUTHORIZED
      response.body mustBe """{"code":"UNAUTHORIZED","message":"Bearer token is missing or not authorized"}"""

    }

    s"return an $INTERNAL_SERVER_ERROR if an exception occurs received from IF" in {
      stubPostWithFault("/organisations/document/4321", correlationId)
      stubPostWithResponseBodyNoHeaders("/auth/authorise", ACCEPTED, Json.obj(
        "applicationId" -> "ID-1"
      ).toString)
      stubPostWithoutRequestAndResponseBody("/write/audit", NO_CONTENT)
      stubPostWithoutRequestAndResponseBody("/write/audit/merged", NO_CONTENT)

      val response = await(buildClient("/vat-repayment-info/4321")
        .withHttpHeaders("CorrelationId" -> correlationId, AUTHORIZATION -> "Bearer some-token")
        .post(createRepaymentDocumentJson))

      response.status mustBe INTERNAL_SERVER_ERROR
      response.body mustBe """{"code":"INTERNAL_SERVER_ERROR","message":"Internal server error"}"""
    }
  }
}
