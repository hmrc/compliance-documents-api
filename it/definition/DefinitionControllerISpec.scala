package definition

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.test.Helpers.OK
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

class DefinitionControllerISpec extends AnyWordSpec with Matchers with GuiceOneServerPerSuite with FutureAwaits with DefaultAwaitTimeout {

  def wsClient: WSClient = app.injector.instanceOf[WSClient]

  "api/definition" should {
    "return the correct definition from config" in {
      val response = await(wsClient.url(s"http://localhost:$port/api/definition").get())

      response.status mustBe OK
      response.body[JsValue] mustBe Json.parse(
        s"""
           |{
           |  "scopes": [
           |    {
           |      "key": "write:protect-connect",
           |      "name": "Protect Connect",
           |      "description": "Scope for accessing protect connect APIs"
           |    }
           |  ],
           |  "api": {
           |    "name": "Compliance Documents",
           |    "description": "Api to manage vat repayment documents sent to EF",
           |    "context": "misc/compliance-documents",
           |    "categories": ["PRIVATE_GOVERNMENT"],
           |    "versions": [
           |      {
           |        "version": "1.0",
           |        "status": "ALPHA",
           |        "endpointsEnabled": false,
           |        "access" : {
           |          "type": "PRIVATE"
           |        }
           |      }
           |    ]
           |  }
           |}
      """.stripMargin)
    }
  }
}
