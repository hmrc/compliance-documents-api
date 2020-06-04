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

package scala.services

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.Environment
import services.ResourceService
import uk.gov.hmrc.http.HeaderCarrier

class ResourceServiceSpec extends WordSpec with MustMatchers
  with ScalaFutures with IntegrationPatience {

  private val env = Environment.simple()

  implicit lazy val hc: HeaderCarrier = HeaderCarrier(sessionId = None)

  val service = new ResourceService(env)


  "The resource service" should {
    "return an exception" in {
      try {
        service.getFile("/schemas/nonexistent.json")
      } catch {
        case e: Exception =>
          e mustBe an[Exception]
          e.getLocalizedMessage mustBe "resource not found: /schemas/nonexistent.json"
      }
    }
  }
}
