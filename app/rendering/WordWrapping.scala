package rendering

trait WordWrapping {

  def ConsoleWidth: Int

  def wrapText(txt: String): List[String] = consumeText(txt.split(" ").toList)

  private def consumeText(words: List[String]): List[String] = words match {
    case line :: word :: tail if exceedWidth(line, word) => line :: consumeText(word :: tail)
    case line :: word :: tail => consumeText(s"$line $word" :: tail)
    case line :: Nil => List(line)
  }

  private def exceedWidth(w1: String, w2: String): Boolean = (w1.length + w2.length) >= ConsoleWidth
}
