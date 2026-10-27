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

package uk.gov.hmrc.perftests.disareturns.testSetup

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.Assertions.cancel
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import uk.gov.hmrc.perftests.disareturns.constants.AppConfig.submissionClockDate

import java.time.{LocalDate, ZoneOffset}
import scala.collection.concurrent.TrieMap
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait BaseRequests {
  implicit val system: ActorSystem    = ActorSystem("setup-system")
  implicit val mat: Materializer      = Materializer(system)
  implicit val ec: ExecutionContext   = system.dispatcher
  val wsClient: StandaloneAhcWSClient = StandaloneAhcWSClient()
  val submissionTestOnlyRequests      = new SubmissionTestOnlyRequests(wsClient)
  val backendTestOnlyRequests         = new BackendTestOnlyRequests(wsClient)

  private val zReferenceCleanupBatchSize = 5000
  private val preparedZReferences        = TrieMap.empty[String, Unit]

  private def inBatches(zReferences: Seq[String])(request: Seq[String] => Future[Unit]): Future[Unit] =
    zReferences
      .grouped(zReferenceCleanupBatchSize)
      .foldLeft(Future.unit)((result, batch) => result.flatMap(_ => request(batch)))

  def setupSubmissionZReferences(zReferences: Seq[String]): Future[Unit] = {
    val setup = for {
      _ <- backendTestOnlyRequests.deleteAllMonthlyReturns()
      _ <- inBatches(zReferences)(submissionTestOnlyRequests.deleteMonthlyReturns)
      _ <- prepareSubmissionZReferences(zReferences)
    } yield ()

    setup.recover { case e =>
      cancel(s"Test has been aborted due to test setup failure: ${e.getMessage}")
    }
  }

  private def prepareSubmissionZReferences(zReferences: Seq[String]): Future[Unit] =
    if (zReferences.isEmpty) Future.unit
    else {
      val date      = LocalDate.parse(submissionClockDate)
      val startDate = date.atStartOfDay(ZoneOffset.UTC).toInstant.toString
      val endDate   = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant.minusNanos(1).toString

      zReferences.foreach(preparedZReferences.put(_, ()))
      inBatches(zReferences)(submissionTestOnlyRequests.setReportingWindowOverride(_, startDate, endDate))
    }

  def testDataCleanUp(submissionZReferences: Seq[String]): Future[Unit] = {
    val zReferencesToDelete  = submissionZReferences.distinct
    val monthlyReturnCleanup =
      if (zReferencesToDelete.nonEmpty) {
        inBatches(zReferencesToDelete)(submissionTestOnlyRequests.deleteMonthlyReturns).recover { case NonFatal(e) =>
          println(s"Warning: failed to clean submission monthly returns: ${e.getMessage}")
          ()
        }
      } else Future.unit
    val overrideCleanup      =
      if (preparedZReferences.nonEmpty) {
        val zReferences = preparedZReferences.keys.toSeq
        inBatches(zReferences)(submissionTestOnlyRequests.deleteOverrides).recover { case NonFatal(e) =>
          println(
            s"Warning: failed to clean submission overrides for ${zReferences.size} Z-references: ${e.getMessage}"
          )
          ()
        }
      } else Future.unit
    val backendMonthlyReturnCleanup =
      backendTestOnlyRequests.deleteAllMonthlyReturns().recover { case NonFatal(e) =>
        println(s"Warning: failed to clean backend monthly returns: ${e.getMessage}")
        ()
      }

    Future
      .sequence(Seq(monthlyReturnCleanup, overrideCleanup, backendMonthlyReturnCleanup))
      .map(_ => ())
      .andThen { case _ =>
        wsClient.close()
        system.terminate()
      }
  }
}
