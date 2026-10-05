package modules

import clients.CandidateRegistry
import com.google.inject.AbstractModule

import java.time.Clock

// Eager, so the registry warms at startup instead of on the first request
// (specs/candidate-registry-read-flip.md, §Sourcing the candidate set).
class CandidateRegistryModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Clock]).toInstance(Clock.systemUTC())
    bind(classOf[CandidateRegistry]).asEagerSingleton()
  }
}
