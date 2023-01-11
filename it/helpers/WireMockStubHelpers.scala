/*
 * Copyright 2023 HM Revenue & Customs
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

package helpers

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait WireMockStubHelpers {

  def stubPostWithoutResponseBody(url: String, status: Int, correlationId: String): StubMapping =
    stubFor(post(urlMatching(url))
      .withHeader("CorrelationId", equalTo(correlationId))
      .withHeader("Authorization", equalTo("Bearer some-token"))
      .withHeader("Environment", equalTo("local"))
      .willReturn(
        aResponse()
          .withStatus(status)))

  def stubPostWithoutRequestAndResponseBody(url: String, status: Int): StubMapping =
    stubFor(post(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)))

  def stubPostWithResponseBody(url: String, status: Int, correlationId: String, response: String): StubMapping =
    stubFor(post(urlMatching(url))
      .withHeader("CorrelationId", equalTo(correlationId))
      .withHeader("Authorization", equalTo("Bearer some-token"))
      .withHeader("Environment", equalTo("local"))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
          .withHeader("Content-Type", "application/json; charset=utf-8")))

  def stubPostWithResponseBodyNoHeaders(url: String, status: Int, response: String): StubMapping =
    stubFor(post(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
          .withHeader("Content-Type", "application/json; charset=utf-8")
      )
    )

  def stubPostWithFault(url: String, correlationId: String): StubMapping =
    stubFor(post(urlMatching(url))
      .withHeader("CorrelationId", equalTo(correlationId))
      .withHeader("Authorization", equalTo("Bearer some-token"))
      .withHeader("Environment", equalTo("local"))
      .willReturn(
        aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))
}
