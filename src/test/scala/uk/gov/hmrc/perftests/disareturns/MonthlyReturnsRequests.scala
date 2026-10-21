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
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.performance.conf.ServicesConfiguration
import uk.gov.hmrc.perftests.disareturns.constants.AppConfig.{disaBaseUrl, disaRoute}

object MonthlyReturnsRequests extends ServicesConfiguration {

  val navigateToMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Navigate to Monthly Report Submission Page")
      .get(s"$disaBaseUrl$disaRoute/monthly-report-submission")
      .check(status.is(200))
      .check(css("input[name=csrfToken]", "value").saveAs("csrfToken"))

  val postMonthlyReportSubmissionPage: HttpRequestBuilder =
    http("Post file upload option from The Monthly Report Submission Page")
      .post(s"$disaBaseUrl$disaRoute/monthly-report-submission": String)
      .formParam("value", "yes")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))

  val navigateToFileUploadPage: HttpRequestBuilder =
    http("Navigate to file upload Page")
      .get(s"$disaBaseUrl$disaRoute/file-upload")
      .check(status.is(200))

  lazy val appFileUpload: HttpRequestBuilder =
    http(s"GET $disaBaseUrl$disaRoute/file-upload")
      .get(s"$disaBaseUrl$disaRoute/file-upload")
      .check(status.is(200))
      .check(saveFileUploadurl)
      .check(saveCallBack)
      .check(saveAmazonDate)
      .check(saveAmazonCredential)
      .check(saveUpscanIniateResponse)
      .check(saveUpscanInitiateRecieved)
      .check(saveAmazonMetaOriginalFileName)
      .check(saveAmazonMetaRequestId)
      .check(saveAmazonAlgorithm)
      .check(saveKey)
      .check(saveAcl)
      .check(saveMetaSessionId)
      .check(saveConsumingService)
      .check(saveQAmazonSignature)
      .check(savePolicy)
      .check(saveReference)
      .check(saveFileType)

  lazy val appPostFileUploadAmls: HttpRequestBuilder =
    http(s"POST $urlUploadProofOfAmls")
      .post(f"#{fileUploadAmazonUrl}")
      .header("content-type", "multipart/form-data; boundary=----WebKitFormBoundarycQF5VGEC89D5MB5B")
      .asMultipartForm
      .bodyPart(StringBodyPart("x-amz-meta-callback-url", f"#{callBack}"))
      .bodyPart(StringBodyPart("x-amz-date", f"#{amazonDate}"))
      .bodyPart(StringBodyPart("x-amz-credential", f"#{amazonCredential}"))
      .bodyPart(StringBodyPart("x-amz-meta-upscan-initiate-response", f"#{upscanInitiateResponse}"))
      .bodyPart(StringBodyPart("x-amz-meta-upscan-initiate-received", f"#{upscanInitiateReceived}"))
      .bodyPart(StringBodyPart("x-amz-meta-request-id", f"#{metaRequestId}"))
      .bodyPart(StringBodyPart("x-amz-meta-original-filename", "UtilityBill.pdf"))
      .bodyPart(StringBodyPart("x-amz-algorithm", f"#{amazonAlgorithm}"))
      .bodyPart(StringBodyPart("key", f"#{key}"))
      .bodyPart(StringBodyPart("acl", f"#{acl}"))
      .bodyPart(StringBodyPart("x-amz-signature", f"#{amazonSignature}"))
      .bodyPart(StringBodyPart("x-amz-meta-session-id", f"#{metaSessionId}"))
      .bodyPart(StringBodyPart("x-amz-meta-consuming-service", f"#{consumingService}"))
      .bodyPart(StringBodyPart("policy", f"#{policy}"))
      .formUpload("file", "files/UtilityBill.pdf")
      .asMultipartForm
      .check(status.is(204))

  lazy val appGetFileUploadedSuccessfullyAmls: HttpRequestBuilder =
    http(s"GET $urlFileUploadedSuccessfully")
      .get(s"$baseUrlOverseas" + urlFileUploadedSuccessfully)
      .check(saveCsrfToken)
      .check(status.is(200))

}
