package api

import helpers.{Fixtures, WireMockSpec}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.Helpers._

class VatRepaymentISpec extends PlaySpec with WireMockSpec with Fixtures {
  "POST /vat-repayment-info/(id)" should {
    s"return an $ACCEPTED if $ACCEPTED received from IF" in {
      stubPostWithoutResponseBody("/organisations/document/4321", ACCEPTED, correlationId)
      stubPostWithResponseBodyNoHeaders("/auth/authorise", ACCEPTED, Json.obj(
        "applicationId" -> "ID-1"
      ).toString)
      stubPostWithoutRequestAndResponseBody("/write/audit", NO_CONTENT)
      stubPostWithoutRequestAndResponseBody("/write/audit/merged", NO_CONTENT)

      val response = await(buildClient("/vat-repayment-info/4321")
        .withHttpHeaders("CorrelationId" -> correlationId)
        .post(createRepaymentDocumentJson))

      response.status mustBe ACCEPTED
    }

    s"return a $BAD_REQUEST if $BAD_REQUEST received from IF" in {
      stubPostWithResponseBody("/organisations/document/4321", BAD_REQUEST, correlationId, Json.obj(
        "code" -> "BAD_REQUEST", "message" -> "Bad request test!"
      ).toString)
      stubPostWithResponseBodyNoHeaders("/auth/authorise", ACCEPTED, Json.obj(
        "applicationId" -> "ID-1"
      ).toString)
      stubPostWithoutRequestAndResponseBody("/write/audit", NO_CONTENT)
      stubPostWithoutRequestAndResponseBody("/write/audit/merged", NO_CONTENT)

      val response = await(buildClient("/vat-repayment-info/4321")
        .withHttpHeaders("CorrelationId" -> correlationId)
        .post(createRepaymentDocumentJson))

      response.status mustBe BAD_REQUEST
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
        .withHttpHeaders("CorrelationId" -> correlationId)
        .post(createRepaymentDocumentJson))

      response.status mustBe INTERNAL_SERVER_ERROR
      response.body mustBe """{"code":"INTERNAL_SERVER_ERROR","message":"Internal server error"}"""
    }
  }
}
