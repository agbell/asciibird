package domain

import scala.util.Random

class Obstacle(width: Int, height: Int) {

  val gapHeight = Random.nextInt(3) + 2

  val gapOffset = Random.nextInt(height - gapHeight + 1) + 1

  def buffer: CharBuffer = CharBuffer(width, height) {
    (for (y <- 1 to height) yield {
      (for (x <- 1 to width) yield {
        if (y < gapOffset || y > gapOffset + gapHeight)
          ':'
        else if (y == gapOffset || y == gapOffset + gapHeight)
          '='
        else
          ' '
      }).toArray
    }).toArray
  }
}

object Obstacle {

  val Width = 4

  val Height = Game.Height

  def random = new Obstacle(Width, Height)
}
