package rendering

trait VersionListColumnLength {

  val DefaultVersionCount: Int

  val DefaultColumnCount = 4

  private lazy val defaultColumnLength = DefaultVersionCount / DefaultColumnCount

  def toColumnLength(totalSize: Int): Int =
    if(totalSize > DefaultVersionCount)
      defaultColumnLength + ((totalSize - DefaultVersionCount - 1) / DefaultColumnCount) + 1
    else defaultColumnLength
}
