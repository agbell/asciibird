package domain

import org.scalatest.FlatSpec

class CharBufferSpec extends FlatSpec {

  val col1 = Array[Char]('a', 'b', 'c')
  val col2 = Array[Char]('d', 'e', 'f')
  val col3 = Array[Char]('g', 'h', 'i')
  val col4 = Array[Char]('j', 'k', 'l')

  behavior of "CharBuffer.scrollLeft"

  it should "scroll one column to the left" in {
    val buffer = CharBuffer(3, 3)(Array(col1, col2, col3))
    val nextBuffer = buffer.scrollLeft()
    assert(buffer.getColumn(1).sameElements(nextBuffer.getColumn(0)))
    assert(buffer.getColumn(2).sameElements(nextBuffer.getColumn(1)))
  }

  it should "scroll one column to the left and insert given column" in {
    val buffer = CharBuffer(3, 3)(Array(col1, col2, col3))
    val nextBuffer = buffer.scrollLeft(col4)
    assert(buffer.getColumn(1).sameElements(nextBuffer.getColumn(0)))
    assert(buffer.getColumn(2).sameElements(nextBuffer.getColumn(1)))
    assert(nextBuffer.getColumn(2).sameElements(Array[Char]('j', 'k', 'l')))
  }
}
