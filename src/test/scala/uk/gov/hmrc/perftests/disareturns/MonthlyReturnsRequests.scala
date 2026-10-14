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
      .post(s"$disaBaseUrl$disaRoute/company-type": String)
      .formParam("value", "yes")
      .formParam("csrfToken", "#{csrfToken}")
      .check(status.is(303))
}
