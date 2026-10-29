/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.perftests.disareturns.constants

import uk.gov.hmrc.performance.conf.ServicesConfiguration

object AppConfig extends ServicesConfiguration {
  val disaBaseUrl: String                  = baseUrlFor("disa-returns-frontend")
  val disaRoute: String                    = "/submit-manage-isas-return"
  val authHost: String                     = baseUrlFor("auth-login-api")
  val authWizardUrl: String                = s"$authHost/auth-login-stub/gg-sign-in"
  val disaReturnsSubmissionHost: String    = baseUrlFor("disa-returns-submission")
  val submissionOverridesPath: String      = "/disa-returns-submission/test-only/overrides"
  val submissionMonthlyReturnsPath: String = "/disa-returns-submission/test-only/monthly-returns"
  val disaReturnsBackendHost: String       = baseUrlFor("disa-returns-backend")
  val backendMonthlyReturnsPath: String    = "/disa-returns-backend/test-only/monthly-returns"

  val submissionClockDate: String = "2026-10-17"
}
