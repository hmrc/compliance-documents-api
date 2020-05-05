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

package scala.models

import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import utils.LoggerHelper

class LoggerHelperSpec extends WordSpec with Matchers with MockitoSugar{
  "The Logger Helper" when {
    "logging a process" should {
      "return a correct format without correlation id" in {
        LoggerHelper.logProcess("class", "method", "message") shouldBe("[class][method]\nmessage")
      }
      "return a correct format with correlation id" in {
        LoggerHelper.logProcess("class", "method", "message", Some("1234")) shouldBe("[class][method]\nmessage, correlationId: 1234")

      }
    }
  }
}
