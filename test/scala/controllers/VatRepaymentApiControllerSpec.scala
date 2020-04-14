package scala.controllers

import config.AppConfig
import controllers.routes
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._

class VatRepaymentApiControllerSpec extends WordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {





  "The Vat Repayment Api Controller" when {
    "calling the getResponse route" should {
      "return Accepted" in {
        route(app, FakeRequest(GET, routes.VatRepaymentApiController.getResponse().url)).map{result =>
          status(result) shouldBe Status.ACCEPTED
        }

      }
    }
  }
}
