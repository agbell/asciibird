package domain

import org.slf4j.LoggerFactory

case class Player(x: Int, y: Int, numMissiles: Int, token: String = Player.Token) {

  private val logger = LoggerFactory.getLogger(getClass)

  def moveUp(): Player = move(0, -1)

  def moveDown(): Player = move(0, 1)

  def moveLeft(): Player = move(-1, 0)

  def moveRight(): Player = move(1, 0)

  def move(dx: Int, dy: Int): Player = Player(x + dx, y + dy, numMissiles)

  def fireMissile(): Player = {
    if (numMissiles > 0) {
      logger.info("Firing missile")
      Player(x, y, numMissiles - 1)
    } else {
      logger.info("No missiles left!")
      this
    }
  }
}

object Player {

  val Token = "@>"
}
