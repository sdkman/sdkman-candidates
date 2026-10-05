package clients

import domain.Candidate
import play.api.{Configuration, Logging}

import java.time.{Clock, Instant}
import java.util.concurrent.atomic.AtomicReference
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

// The in-process candidate set, fetched whole from the State API's
// GET /candidates. A failed refresh never replaces a good set: stale is
// better than empty (specs/candidate-registry-read-flip.md, §Sourcing the
// candidate set).
@Singleton
class CandidateRegistry @Inject() (
    stateApi: StateApiImpl,
    configuration: Configuration,
    clock: Clock
) extends Logging {

  private val refreshInterval =
    configuration.get[FiniteDuration]("candidate-registry.refresh-interval")

  private val candidateSet = new AtomicReference[Option[Seq[Candidate]]](None)
  private val lastSuccess  = new AtomicReference[Option[Instant]](None)

  // java's record never carries a `default`; its value is the Temurin lts
  // identifier, resolved here and refreshed with the set so no request issues
  // a tag lookup (specs/candidate-registry-read-flip.md, §GET /candidates/list).
  private val javaIdentifier = new AtomicReference[Option[String]](None)

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

  private def fetch(): Future[Unit] = {
    val set  = fetchCandidates()
    val java = fetchJavaDefault()
    set.zip(java).map(_ => ())
  }

  private def fetchCandidates(): Future[Unit] =
    stateApi
      .findAllCandidates()
      .transform {
        case Success(fetched) =>
          candidateSet.set(Some(fetched))
          lastSuccess.set(Some(clock.instant()))
          Success(())
        case Failure(e) =>
          logger.warn("Candidate registry refresh failed; retaining the previous set", e)
          Success(())
      }

  // A 404 is a failed refresh, not a resolved answer: the last resolved
  // identifier keeps serving until a later refresh resolves a new one.
  private def fetchJavaDefault(): Future[Unit] =
    stateApi
      .findVersionByCandidateAndTag("java", "lts", "LINUX_X64", Some("tem"))
      .map {
        case Some(version) => javaIdentifier.set(Some(version.identifier))
        case None =>
          logger.warn("Java default refresh found no lts tag; retaining the previous value")
      }
      .recover { case e =>
        logger.warn("Java default refresh failed; retaining the previous value", e)
      }

  /** The cached set. With none held, waits for a fetch and yields `None` only if it fails. A stale
    * set is served at once while a refresh runs behind it: a request never blocks on a refresh.
    */
  def candidates(): Future[Option[Seq[Candidate]]] =
    candidateSet.get() match {
      case None => refresh().map(_ => candidateSet.get())
      case held =>
        if (isStale) refresh()
        Future.successful(held)
    }

  /** The cached java identifier. Waits for a fetch only while no candidate set is held. */
  def javaDefault(): Future[Option[String]] =
    candidateSet.get() match {
      case None => refresh().map(_ => javaIdentifier.get())
      case _    => Future.successful(javaIdentifier.get())
    }

  private def isStale: Boolean =
    lastSuccess.get().forall(_.plusMillis(refreshInterval.toMillis).isBefore(clock.instant()))

  private[clients] def current: Option[Seq[Candidate]] = candidateSet.get()

  private[clients] def currentJavaDefault: Option[String] = javaIdentifier.get()

  /** Test-scoped: empties the registry so a scenario starts cold. */
  def clear(): Unit = {
    candidateSet.set(None)
    lastSuccess.set(None)
    javaIdentifier.set(None)
  }
}
