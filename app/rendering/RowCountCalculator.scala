package rendering

trait RowCountCalculator {

  val MinCountThreshold: Int

  val DefaultColumnCount: Int

  val SingleRowCount = 1

  private lazy val minRowCount = MinCountThreshold / DefaultColumnCount

  def asRowCount(totalCount: Int): Int ={
    if(totalCount > MinCountThreshold) {
      val excessCount = totalCount - MinCountThreshold
      val zeroShiftedRowCount = (excessCount - 1) / DefaultColumnCount
      minRowCount + zeroShiftedRowCount + SingleRowCount

    } else minRowCount
  }
}