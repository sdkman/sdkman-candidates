package domain

case class Candidate(
    candidate: String,
    name: String,
    description: String,
    websiteUrl: String,
    default: Option[String]
)
