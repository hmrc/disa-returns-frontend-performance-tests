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
import io.gatling.core.check.css.CssCheckType
import jodd.lagarto.dom.NodeSelector
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration
import uk.gov.hmrc.perftests.disareturns.constants.AppConfig.{disaBaseUrl, disaRoute}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object MonthlyReturnsRequests extends ServicesConfiguration {

  val fileProcessingPollInterval: FiniteDuration = 2.seconds
  val fileProcessingMaxWait: FiniteDuration      = 5.minutes

  private val uploadedReportFilesPath = s"$disaRoute/uploaded-report-files"

  val navigateToMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Navigate to Monthly Report Submission Page")
      .get(s"$disaBaseUrl$disaRoute/monthly-report-submission")
      .check(status.is(200))
      .check(css("input[name=csrfToken]", "value").saveAs("csrfToken"))

  val postYesMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Post 'yes' on Monthly Report Submission Page")
      .post(s"$disaBaseUrl$disaRoute/monthly-report-submission": String)
      .formParam("value", "yes")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))

  val postNoMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Post 'no' on Monthly Report Submission Page")
      .post(s"$disaBaseUrl$disaRoute/monthly-report-submission": String)
      .formParam("value", "no")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))

  val getUploadPage: HttpRequestBuilder =
    http("Get Upload a valid file")
      .get(s"$disaBaseUrl$disaRoute/file-upload")
      .check(status.is(200))
      .check(form("#file-upload-form").saveAs("DisaUploadForm"))
      .check(css("#file-upload-form", "action").saveAs("fileUploadAmazonUrl")
      )

  private def postFileUpload(requestName: String, filePath: String, contentType: String): HttpRequestBuilder =
    http(requestName)
      .post("#{fileUploadAmazonUrl}")
      .asMultipartForm
      .form("#{DisaUploadForm}")
      .bodyPart(RawFileBodyPart("file", filePath).contentType(contentType))
      .check(status.is(303))
      .check(headerRegex("Location", """/file-processing\?(?:.*&)?key=([^&]+)""").saveAs("uploadReference"))

  val postCsvFileUpload: HttpRequestBuilder =
    postFileUpload("Post a valid CSV File Upload", "data/isa-monthly-return-3000.csv", "text/csv")

  val postXlsxFileUpload: HttpRequestBuilder =
    postFileUpload(
      "Post a valid XLSX File Upload",
      "data/isa-monthly-return-3000.xlsx",
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )

  val navigateToFileProcessingPage: HttpRequestBuilder =
    http("Navigate to File Processing Page")
      .get(s"$disaBaseUrl$disaRoute/file-processing?key=#{uploadReference}")
      .check(status.is(200))

  val navigateToUploadedReportFilesPage: HttpRequestBuilder =
    http("Navigate to Uploaded Report Files Page")
      .get(s"$disaBaseUrl$uploadedReportFilesPath")
      .check(status.is(200))
      .check(css("input[name=csrfToken]", "value").saveAs("csrfToken"))

  val postYesUploadedReportFilesPage: HttpRequestBuilder =
    http("Post 'yes' on Uploaded Report Files Page")
      .post(s"$disaBaseUrl$uploadedReportFilesPath": String)
      .formParam("value", "yes")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))
      .check(header("Location").is(s"$disaRoute/file-upload"))

  val postNoUploadedReportFilesPage: HttpRequestBuilder =
    http("Post 'no' on Uploaded Report Files Page")
      .post(s"$disaBaseUrl$uploadedReportFilesPath": String)
      .formParam("value", "no")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))
      .check(header("Location").is(s"$disaRoute/check-your-answers"))

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
      .check(css("input[name=csrfToken]", "value").saveAs("csrfToken"))

  val postDeclarationPage: HttpRequestBuilder =
    http("Post Declaration Page")
      .post(s"$disaBaseUrl$disaRoute/declaration": String)
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))
      .check(header("Location").is(s"$disaRoute/submission-complete"))


  // UPSCAN RELATED METHODS BELOW
  // Matches on the current uploadReference so a continue URL saved for an earlier file in the journey
  // doesn't end the poll. Kept as a single top-level action because ChainBuilder.actionBuilders is reversed.
  private def fileProcessingComplete(session: Session): Boolean =
    session("fileProcessingContinueUrl").asOption[String].exists(_.contains(session("uploadReference").as[String]))

  val pollFileProcessingProgress: List[ActionBuilder] =
    asLongAsDuring(session => !fileProcessingComplete(session), fileProcessingMaxWait) {
      exec(
        http("Check file processing progress")
          .get(s"$disaBaseUrl$disaRoute/file-processing/#{uploadReference}/check-progress")
          .check(status.is(200))
          .check(css("a[href$='/continue']", "href").optional.saveAs("fileProcessingContinueUrl"))
      ).doIf(session => !fileProcessingComplete(session)) {
        pause(fileProcessingPollInterval)
      }
    }.actionBuilders

  val continueFromFileProcessing: HttpRequestBuilder =
    http("Continue from File Processing Page")
      .get(s"$disaBaseUrl$disaRoute/file-processing/#{uploadReference}/continue")
      .check(status.is(303))
      .check(header("Location").is(uploadedReportFilesPath))
}

