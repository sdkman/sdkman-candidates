package clients

import domain.Candidate
import play.api.Logging

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

// The in-process candidate set, fetched whole from the State API's
// GET /candidates. A failed refresh never replaces a good set: stale is
// better than empty (specs/candidate-registry-read-flip.md, §Sourcing the
// candidate set).
@Singleton
class CandidateRegistry @Inject() (stateApi: StateApiImpl) extends Logging {

  private val candidates  = new AtomicReference[Option[Seq[Candidate]]](None)
  private val lastSuccess = new AtomicReference[Option[Instant]](None)

  // At most one refresh is in flight. A trigger arriving meanwhile joins it
  // instead of starting a second fetch that could land out of order.
  private val inFlight = new AtomicReference[Option[Future[Unit]]](None)

  def refresh(): Future[Unit] = {
    val promise = Promise[Unit]()
    if (inFlight.compareAndSet(None, Some(promise.future))) {
      fetch().onComplete { _ =>
        inFlight.set(None)
        promise.success(())
      }
      promise.future
    } else inFlight.get().getOrElse(refresh())
  }

  private def fetch(): Future[Unit] =
    stateApi
      .findAllCandidates()
      .transform {
        case Success(fetched) =>
          candidates.set(Some(fetched))
          lastSuccess.set(Some(Instant.now()))
          Success(())
        case Failure(e) =>
          logger.warn("Candidate registry refresh failed; retaining the previous set", e)
          Success(())
      }

  private[clients] def current: Option[Seq[Candidate]] = candidates.get()

  /** Test-scoped: empties the registry so a scenario starts cold. */
  def clear(): Unit = {
    candidates.set(None)
    lastSuccess.set(None)
  }
}
