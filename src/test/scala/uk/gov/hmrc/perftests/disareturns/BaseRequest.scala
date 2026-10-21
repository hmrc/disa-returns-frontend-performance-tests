/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.perftests.disareturns

import com.fasterxml.jackson.databind.JsonNode
import io.gatling.core.Predef._
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.CheckBuilder.MultipleFind
import io.gatling.core.check.jsonpath.JsonPathCheckType
import io.gatling.core.check.regex.{RegexCheckType, RegexOfType}
import io.gatling.http.Predef._
import io.gatling.http.check.header.{HttpHeaderCheckType, HttpHeaderRegexCheckType, HttpHeaderRegexOfType}
import uk.gov.hmrc.performance.conf.ServicesConfiguration

import java.net.URLEncoder

trait BaseRequest extends ServicesConfiguration {

  def encodeContinuePath(baseUrl: String, path: String): String =
    URLEncoder.encode((if (runLocal) baseUrl else "") + path, "UTF-8")

  val baseUrlStubs: String    = baseUrlFor("agents-external-stubs")
  val baseUrlOverseas: String = baseUrlFor("agent-overseas-frontend") + "/agent-services/apply-from-outside-uk"

  private val CsrfPattern = """<input type="hidden" name="csrfToken"\s+value="([^"]+)""""

  private val userIdPattern         = """"userId":"([^"]+)""""
  private val planetIdPattern       = """"planetId":"([^"]+)""""
  private val userDetailsUrlPattern = s"""([^"]+)"""
  private val authTokenPattern      = """"authToken":"([^"]+)""""
  private val sessionIdPattern      = """"sessionId":"([^"]+)""""

  private val applicationReferencePattern = """"applicationReference":"([^"]+)""""
  val clientUserIdPattern: String         = """agents-external-stubs/users/([^"]+)"""

  def saveApplicationReference: CheckBuilder[RegexCheckType, String] =
    bodyCheck(applicationReferencePattern).saveAs("applicationReference")

  def saveCsrfToken: CheckBuilder[RegexCheckType, String] =
    regex(_ => CsrfPattern).saveAs("csrfToken")

  def saveUserId: CheckBuilder[RegexCheckType, String] =
    bodyCheck(userIdPattern).saveAs("userId")

  def savePlanetId: CheckBuilder[RegexCheckType, String] =
    bodyCheck(planetIdPattern).saveAs("planetId")

  def saveUserDetailsUrl: CheckBuilder[HttpHeaderRegexCheckType, Response] =
    headerRegex(_=>"Location", userDetailsUrlPattern).saveAs("userDetailsUrl")

  def saveBearerToken: CheckBuilder[RegexCheckType, String] =
    bodyCheck(authTokenPattern).saveAs("bearerToken")

  def saveBearerTokenHeader: CheckBuilder[HttpHeaderRegexCheckType, Response] =
    headerRegex(_=>"Authorization", """Bearer\s([^"]+)""").saveAs("bearerToken")

  def saveSessionIdHeader: CheckBuilder[HttpHeaderCheckType, Response] =
    header(_=>"X-Session-ID").saveAs("sessionId")

  def saveSessionId: CheckBuilder[RegexCheckType, String] =
    bodyCheck(sessionIdPattern).saveAs("sessionId")

  def saveRecordId: CheckBuilder[JsonPathCheckType, JsonNode] =
    jsonPath("$.recordIds[0]").saveAs("recordId")

  def saveUtr: CheckBuilder[JsonPathCheckType, JsonNode] =
    jsonPath("$.utr").saveAs("utr")

  def saveNino: CheckBuilder[JsonPathCheckType, JsonNode] =
    jsonPath("$.nino").saveAs("nino")

  def saveBusinessPostcode: CheckBuilder[JsonPathCheckType, JsonNode] =
    jsonPath("$.addressDetails.postalCode").saveAs("postalCode")

  def bodyCheck(body: String): MultipleFind[RegexCheckType, String, String] & RegexOfType = regex(body)

  def redirectionLocationIsEither(locations: String*): MultipleFind[HttpHeaderRegexCheckType, Response, String] & HttpHeaderRegexOfType = {
    val s = locations.map(l => s".*$l.*").reduce(_ + "|" + _)
    headerRegex(_=>"Location", s)
  }

  def redirectionLocationIs(location: String): MultipleFind[HttpHeaderRegexCheckType, Response, String] & HttpHeaderRegexOfType =
    redirectionLocationIsEither(location)

}
