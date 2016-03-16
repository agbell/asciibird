package domain

abstract class CharBuffer(val width: Int, val height: Int) {

  protected def underlying: Array[Array[Char]]

  def putChar(x: Int, y: Int, char: Char): CharBuffer = {
    /*
    val copy = underlying.transpose.transpose
    copy(y)(x) = char
    CharBuffer(width, height)(copy)
    */
    //CharBuffer(width, height)(underlying)
    this
  }

  def getChar(x: Int, y: Int) = underlying(y)(x)

  def getRow(y: Int): Array[Char] = underlying(y)

  def getColumn(x: Int): Array[Char] = underlying.map(_.apply(x))

  def isEmpty: Boolean = underlying.forall(_.forall(_ == CharBuffer.EmptyChar))

  def scrollLeft(rightMostColumn: Array[Char] = Array.fill(height)(CharBuffer.EmptyChar)): CharBuffer = {
    dropLeftColumn().appendColumn(rightMostColumn)
  }

  def dropLeftColumn(): CharBuffer = {
    val newWidth = width - 1
    if (newWidth > 0) CharBuffer(newWidth, height)(underlying.map(_.tail)) else CharBuffer.empty(1, height)
  }

  def appendColumn(newColumn: Array[Char]): CharBuffer = CharBuffer(width + 1, height) {
    underlying.zipWithIndex.map { case (column, i) =>
      column :+ newColumn(i)
    }
  }
}

object CharBuffer {

  val EmptyChar = ' '

  def empty(width: Int, height: Int): CharBuffer = new EmptyCharBuffer(width, height)

  def apply(width: Int, height: Int)(chars: Array[Array[Char]]): CharBuffer = new NonEmptyCharBuffer(width, height)(chars)

  private class EmptyCharBuffer(width: Int, height: Int) extends CharBuffer(width, height) {
    protected val underlying = Array.fill(height, width)(EmptyChar)
  }

  private class NonEmptyCharBuffer(width: Int, height: Int)(protected val underlying: Array[Array[Char]]) extends CharBuffer(width, height) {
    require(underlying.size == height)
    require(underlying.forall(_.size == width))
  }
}
