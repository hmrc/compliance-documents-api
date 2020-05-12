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

import models.ClassIndex
import org.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.{JsError, JsPath, Json}

class ClassIndexSpec extends WordSpec with Matchers with MockitoSugar {
  "The classes reads method" should {
    "return a JsError if given Json not containing nReg, ef or pReg" in {
      Json.fromJson[ClassIndex](Json.obj("a" -> "b")) shouldBe JsError(JsPath.apply(),"unable to validate")
    }
  }
}
