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

package uk.gov.hmrc.perftests.disareturns

import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.regex.RegexCheckType
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration
import uk.gov.hmrc.perftests.disareturns.constants.AppConfig.{disaBaseUrl, disaRoute}

import scala.concurrent.duration.DurationInt

object MonthlyReturnsRequests extends ServicesConfiguration {

  val amazonUrlPattern    = """action="(.*?)""""
  val upscanTimer: Int  = 25 // in seconds, how long to wait for upscan upload - first spinny wheel
  val statusChecks: Int = 50 // how many checks for file status to make during second spinny wheel - 3 second intervals
  val refreshes: Int    = 5 // how many times still checking file page should be refreshed - 1 second interval

  val navigateToMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Navigate to Monthly Report Submission Page")
      .get(s"$disaBaseUrl$disaRoute/monthly-report-submission")
      .check(status.is(200))
      .check(css("input[name=csrfToken]", "value").saveAs("csrfToken"))

  val postYesMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Post file upload option from The Monthly Report Submission Page")
      .post(s"$disaBaseUrl$disaRoute/monthly-report-submission": String)
      .formParam("value", "yes")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))

  val postNoMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Post file upload option from The Monthly Report Submission Page")
      .post(s"$disaBaseUrl$disaRoute/monthly-report-submission": String)
      .formParam("value", "no")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))

  val getUploadPage: HttpRequestBuilder =
    http("Get Upload a valid file")
      .get(s"$disaBaseUrl$disaRoute/file-upload")
      .check(form("#uploadForm").saveAs("DisaUploadForm"))
      .check(saveFileUploadUrl)
      .check(status.is(200))

  val postValidFileUpload: HttpRequestBuilder =
    http("Post a valid File Upload")
      .post("#{fileUploadAmazonUrl}")
      .asMultipartForm
      .form("#{DisaUploadForm}")
      .bodyPart(RawFileBodyPart("file", "data/isa-open-valid.xlsx"))
      .check(status.is(303))
      .check(header("location").saveAs("Status"))

  val navigateToCheckYourAnswersPage: HttpRequestBuilder =
    http("Navigate to Check your answers Page")
      .get(s"$disaBaseUrl$disaRoute/check-your-answers")
      .check(status.is(200))
      .check(css("input[name=csrfToken]", "value").saveAs("csrfToken"))

  val postCheckYourAnswersPage: HttpRequestBuilder =
    http("Post Check your answers Page")
      .post(s"$disaBaseUrl$disaRoute/check-your-answers": String)
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))

  val navigateToDeclarationPage: HttpRequestBuilder =
    http("Navigate to Declaration Page")
      .get(s"$disaBaseUrl$disaRoute/declaration")
      .check(status.is(200))


  // UPSCAN RELATED METHODS BELOW

  def saveFileUploadUrl: CheckBuilder[RegexCheckType, String] =
    regex(_ => amazonUrlPattern).saveAs("fileUploadAmazonUrl")

  val pollingStatus: List[ActionBuilder] = {
    val pollRequest: ChainBuilder =
      asLongAsDuring(
        session => !session("status").asOption[Int].contains(200),
        upscanTimer.seconds
      ) {
        exec(
          http("poll status")
            .get(s"$disaBaseUrl$disaRoute/file-upload")
            .check(status.is(303))
            .check(header("status").saveAs("status"))
            .silent
        )
          .pause(5)
      }
    pollRequest.actionBuilders
 }
}

