package domain

import scala.util.Random
import com.googlecode.lanterna.input.Key
import com.googlecode.lanterna.terminal.Terminal

case class State private(
    terminal: Terminal,
    origin: (Int, Int),
    player: Player,
    score: Long,
    platform: CharBuffer,
    obstacle: CharBuffer,
    obstacleDelay: Int) {

  private val (originX, originY) = origin

  private val obstacles = Stream.continually(Obstacle.random.buffer).iterator

  def isValid: Boolean = {
    def helper(x: Int, y: Int): Boolean = {
      x >= originX &&
      y >= originY &&
      x < originX + Game.Width &&
      y < originY + Game.Height &&
      platform.getChar(x - originX, y - originY) == CharBuffer.EmptyChar
    }
    (0 to player.token.size).map(i => helper(player.x + i, player.y)).reduceLeft(_ && _)
  }

  def updatePlatform(): State = {
    terminal.synchronized {
      if (obstacleDelay > 0)
        copy(platform = platform.scrollLeft(), obstacleDelay = obstacleDelay - 1)
      else if (obstacle.isEmpty)
        copy(platform = platform.scrollLeft(), obstacle = obstacles.next(), obstacleDelay = Random.nextInt(5) + 1)
      else
        copy(platform = platform.scrollLeft(obstacle.getColumn(0)), obstacle = obstacle.dropLeftColumn())
    }
  }

  // todo adjust score increment depending on game difficulty level
  def updatePlayer(): State = {
    terminal.synchronized {
      copy(score = score + 1L, player = terminal.readInput() match {
        case null => player
        case key  => key.getKind match {
          case Key.Kind.ArrowUp     => player.moveUp()
          case Key.Kind.ArrowDown   => player.moveDown()
          case Key.Kind.ArrowLeft   => player.moveLeft()
          case Key.Kind.ArrowRight  => player.moveRight()
          case _                    => player
        }
      })
    }
  }

  def renderPlatform() {
    def drawBorder(x: Int, y: Int) {
      terminal.moveCursor(x, y)
      for (col <- 0 until Game.Width) terminal.putCharacter('-')
    }
    terminal.synchronized {
      drawBorder(originX, originY - Game.Border)
      drawBorder(originX, originY + Game.Height)
      for {
        y <- 0 until Game.Height
        x <- 0 until Game.Width
      } {
        terminal.moveCursor(x + originX, y + originY)
        terminal.putCharacter(platform.getChar(x, y))
      }
      terminal.moveCursor(0, 0)
      terminal.flush()
    }
  }

  def renderPlayer() {
    renderToken(player.x, player.y, player.token)
    terminal.moveCursor(originX, originY + Game.Height + Game.Border)
    s"Score: $score".foreach(terminal.putCharacter)
    terminal.moveCursor(0, 0)
  }

  def hidePlayer() {
    renderToken(player.x, player.y, player.token.map(_ => CharBuffer.EmptyChar).mkString)
  }

  private def renderToken(x: Int, y: Int, token: String) {
    terminal.synchronized {
      terminal.moveCursor(x, y)
      token.foreach(terminal.putCharacter)
      terminal.moveCursor(0, 0)
      terminal.flush()
    }
  }
}

object State {

  def init(terminal: Terminal): State = {
    val screenSize = terminal.getTerminalSize
    val originX = math.floor(screenSize.getColumns / 2).toInt - math.floor(Game.Width / 2).toInt
    val originY = math.floor(screenSize.getRows / 2).toInt - math.floor(Game.Height / 2).toInt
    val origin = (originX, originY)
    val player = Player(originX + 5, originY + 5)
    val score = 0L
    val platform = CharBuffer.empty(width = Game.Width, height = Game.Height)
    val obstacle = Obstacle.random.buffer
    State(terminal, origin, player, score, platform, obstacle, obstacleDelay = 0)
  }
}
