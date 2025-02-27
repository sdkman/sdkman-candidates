package domain

case class Version(
    candidate: String,
    version: String,
    platform: String,
    url: String,
    visible: Boolean,
    vendor: Option[String]
)
