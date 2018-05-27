package rendering

import io.sdkman.repos.Candidate

class CandidateListSection(val prettyName: String,
                           val candidate: String,
                           val description: String,
                           val defaultVersion: String,
                           val websiteUrl: String) {

  def this(c: Candidate) = this(c.name, c.candidate, c.description, c.default.getOrElse("Coming soon!"), c.websiteUrl)
}

trait PlainTextRendering extends WordWrapping {

  self: CandidateListSection =>

  val ConsoleWidth = 80

  def header: String = s"$prettyName ($defaultVersion)".padTo(ConsoleWidth - websiteUrl.length, ' ') + websiteUrl

  def footer: String = s"$$ sdk install $candidate".reverse.padTo(ConsoleWidth, ' ').reverse

  def render(): String = header + "\n\n" + wrapText(description).mkString("\n") + "\n\n" + footer
}
