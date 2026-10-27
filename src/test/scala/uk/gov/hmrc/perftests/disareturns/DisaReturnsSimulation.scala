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
import io.gatling.core.structure.ChainBuilder
import uk.gov.hmrc.performance.simulation.PerformanceTestRunner
import uk.gov.hmrc.perftests.disareturns.AuthRequests.{navigateToAuthLoginStubPage, submitLogin}
import uk.gov.hmrc.perftests.disareturns.MonthlyReturnsRequests.{continueFromFileProcessing, getUploadPage, navigateToCheckYourAnswersPage, navigateToDeclarationPage, navigateToFileProcessingPage, navigateToMonthlyReportSubmissionPage, navigateToUploadedReportFilesPage, pollFileProcessingProgress, postCheckYourAnswersPage, postDeclarationPage, postNoMonthlyReportSubmissionPage, postNoUploadedReportFilesPage, postValidFileUpload, postYesMonthlyReportSubmissionPage}
import uk.gov.hmrc.perftests.disareturns.testSetup.BaseRequests
import uk.gov.hmrc.perftests.disareturns.utils.LoadSizing

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt


class DisaReturnsSimulation extends PerformanceTestRunner with BaseRequests {

  private def userCount(journeyId: String): Int =
    definitions(labels)
      .find(_.id == journeyId)
      .map { journey =>
        LoadSizing.userCount(
          smoke = runSingleUserJourney,
          journeyLoad = journey.load,
          loadFactor = loadFactor,
          rampUpTime = rampUpTime,
          constantRateTime = constantRateTime,
          rampDownTime = rampDownTime
        )
      }
      .getOrElse(0)

  private val submitReturnsUsers    = userCount("ISA-Manager-Submit-Returns")
  private val allocatedReferences   = LoadSizing.allocate(
    fileUploadCount = submitReturnsUsers,
    nilReturnCount = submitReturnsUsers
  )
  private val fileUploadZReferences = allocatedReferences.fileUpload
  private val nilReturnZReferences  = allocatedReferences.nilReturn
  private val testZReferences       = fileUploadZReferences ++ nilReturnZReferences

  private val setupAndCleanupTimeout = 2.minutes

  before {
    Await.result(setupSubmissionZReferences(testZReferences), setupAndCleanupTimeout)
  }

  after {
    Await.result(testDataCleanUp(testZReferences), setupAndCleanupTimeout)
  }

  private def zReferenceFeeder(zReferences: Seq[String], key: String): ChainBuilder =
    feed(zReferences.iterator.map(zRef => Map(key -> zRef)))

  val fileUploadFeeder: ChainBuilder = zReferenceFeeder(fileUploadZReferences, "fileUploadZRef")
  val nilReturnFeeder: ChainBuilder  = zReferenceFeeder(nilReturnZReferences, "nilReturnZRef")

  setup("ISA-Manager-Submit-Nil-Returns", "ISA Manager Submit Returns - Nil return Journey").withActions(
    nilReturnFeeder.actionBuilders: _*
  ).withRequests(
    navigateToAuthLoginStubPage,
    submitLogin(zrefValue = "#{nilReturnZRef}","monthly-report-submission"),
    navigateToMonthlyReportSubmissionPage,
    postNoMonthlyReportSubmissionPage,
    navigateToCheckYourAnswersPage,
    postCheckYourAnswersPage,
    navigateToDeclarationPage,
    postDeclarationPage
  )

  setup("ISA-Manager-Submit-Returns", "ISA Manager Submit Returns - File upload Journey").withActions(
    fileUploadFeeder.actionBuilders: _*
  ).withRequests(
    navigateToAuthLoginStubPage,
    submitLogin(zrefValue = "#{fileUploadZRef}","monthly-report-submission"),
    navigateToMonthlyReportSubmissionPage,
    postYesMonthlyReportSubmissionPage,
    getUploadPage,
    postValidFileUpload,
    navigateToFileProcessingPage
  ).withActions(
    pollFileProcessingProgress: _*
  ).withRequests(
    continueFromFileProcessing,
    navigateToUploadedReportFilesPage,
    postNoUploadedReportFilesPage,
    navigateToCheckYourAnswersPage,
    postCheckYourAnswersPage,
    navigateToDeclarationPage,
    postDeclarationPage
  )

  runSimulation()
}
