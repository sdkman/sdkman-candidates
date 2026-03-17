package domain

case class Version(
    candidate: String,
    version: String,
    platform: String,
    url: String,
    visible: Option[Boolean],
    vendor: Option[String]
)
