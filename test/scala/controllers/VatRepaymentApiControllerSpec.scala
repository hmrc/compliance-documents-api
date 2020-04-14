package scala.controllers

import config.AppConfig
import controllers.routes
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._

class VatRepaymentApiControllerSpec extends WordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {





  "The Vat Repayment Api Controller" when {
    "calling the getResponse route" should {
      "return Accepted if given a Json body" in {
        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData().url).withJsonBody(Json.obj("a" -> "b"))).map{result =>
          status(result) shouldBe Status.ACCEPTED
          contentAsJson(result) shouldBe Json.obj("a" -> "b")
        }

      }
      "return BadRequest if not given a Json body" in {
        route(app, FakeRequest(POST, routes.VatRepaymentApiController.postRepaymentData().url).withBody("This is not Json!")).map{result =>
          status(result) shouldBe Status.BAD_REQUEST
        }
      }
    }
  }
}
