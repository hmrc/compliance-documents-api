/*
 * Copyright 2024 HM Revenue & Customs
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

package scala.controllers.Definition

import controllers.AssetsMetadata
import controllers.definition.ApiDocumentationController
import models.definition.ApiDefinition
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play._
import play.api.Configuration
import play.api.http.DefaultHttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, DefaultActionBuilder, Results}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.ExecutionContext.Implicits.global

class ApiDocumentationControllerSpec
  extends PlaySpec
    with ScalaFutures
    with Results {

  "ApiDocumentationController" should {

    "return the correct API definition JSON when valid configuration is provided" in {
      val config = Configuration(
        "apiDefinition.status" -> "STABLE",
        "apiDefinition.endpointsEnabled" -> true
      )
      val controller = createController(config)

      val result = controller.definition().apply(FakeRequest())

      status(result) mustBe OK
      val expectedJson = Json.toJson(ApiDefinition(endpointsEnabled = true, "STABLE"))
      contentAsJson(result) mustBe expectedJson
    }

    "return an empty list for whitelistedApplicationIds when not configured" in {
      val config = Configuration(
        "apiDefinition.status" -> "BETA",
        "apiDefinition.endpointsEnabled" -> true
      )
      val controller = createController(config)

      val result = controller.definition().apply(FakeRequest())

      status(result) mustBe OK
      val expectedJson = Json.toJson(ApiDefinition(endpointsEnabled = true, "BETA"))
      contentAsJson(result) mustBe expectedJson
    }

    "handle endpointsEnabled being false correctly" in {
      val config = Configuration(
        "apiDefinition.whitelistedApplicationIds" -> Seq("app1"),
        "apiDefinition.status" -> "ALPHA",
        "apiDefinition.endpointsEnabled" -> false
      )
      val controller = createController(config)

      val result = controller.definition().apply(FakeRequest())

      status(result) mustBe OK
      val expectedJson = Json.toJson(ApiDefinition(endpointsEnabled = false, "ALPHA"))
      contentAsJson(result) mustBe expectedJson
    }
  }


  private def createController(config: Configuration) = {
    val mockAssetsMetadata = mock[AssetsMetadata]
    new ApiDocumentationController(
      DefaultActionBuilder(stubBodyParser[AnyContent]()),
      DefaultHttpErrorHandler,
      mockAssetsMetadata,
      config
    )
  }
}
