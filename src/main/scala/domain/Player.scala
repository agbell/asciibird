package domain

case class Player(x: Int, y: Int, token: String = Player.Token) {

  def moveUp(): Player = move(0, -1)

  def moveDown(): Player = move(0, 1)

  def moveLeft(): Player = move(-1, 0)

  def moveRight(): Player = move(1, 0)

  def move(dx: Int, dy: Int): Player = Player(x + dx, y + dy)
}

object Player {

  val Token = "@>"
}
