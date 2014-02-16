package domain

import scala.util.Random
import com.googlecode.lanterna.input.Key
import com.googlecode.lanterna.terminal.Terminal

case class State private(
    terminal: Terminal,
    width: Int,
    height: Int,
    origin: (Int, Int),
    player: Player,
    score: Long,
    platform: CharBuffer,
    obstacle: CharBuffer,
    obstacleDelay: Int) {

  private val (originX, originY) = origin

  private val obstacles = Stream.continually(Obstacle(height).buffer).iterator

  def isValid: Boolean = {
    def helper(x: Int, y: Int): Boolean = {
      x >= originX &&
      y >= originY &&
      x < originX + width &&
      y < originY + height &&
      platform.getChar(x - originX, y - originY) == CharBuffer.EmptyChar
    }
    (0 to player.token.size).map(i => helper(player.x + i, player.y)).reduceLeft(_ && _)
  }

  def updatePlatform(): State = {
    if (obstacleDelay > 0)
      copy(platform = platform.scrollLeft(), obstacleDelay = obstacleDelay - 1)
    else if (obstacle.isEmpty)
      copy(platform = platform.scrollLeft(), obstacle = obstacles.next(), obstacleDelay = Random.nextInt(5) + 1)
    else
      copy(platform = platform.scrollLeft(obstacle.getColumn(0)), obstacle = obstacle.dropLeftColumn())
  }

  def updatePlayer(): State = {
    terminal.synchronized {
      copy(player = terminal.readInput() match {
        case null => player
        case key  => key.getKind match {
          case Key.Kind.ArrowUp     => player.moveUp()
          case Key.Kind.ArrowDown   => player.moveDown()
          case Key.Kind.ArrowLeft   => player.moveLeft()
          case Key.Kind.ArrowRight  => player.moveRight()
          case Key.Kind.NormalKey   => key.getCharacter match {
            case 'w' | 'k'          => player.moveUp()
            case 's' | 'j'          => player.moveDown()
            case 'a' | 'h'          => player.moveLeft()
            case 'd' | 'l'          => player.moveRight()
            case _                  => player
          }
          case _                    => player
        }
      })
    }
  }

  def updateScore(level: Game.Level): State = {
    copy(score = score + level.value)
  }

  def renderPlatform() {
    def drawBorder(x: Int, y: Int) {
      terminal.moveCursor(x, y)
      for (col <- 0 until width) terminal.putCharacter('-')
    }
    terminal.synchronized {
      drawBorder(originX, originY - Game.BorderSize)
      drawBorder(originX, originY + height)
      for {
        y <- 0 until height
        x <- 0 until width
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
  }

  def hidePlayer() {
    renderToken(player.x, player.y, player.token.map(_ => CharBuffer.EmptyChar).mkString)
  }

  def renderScore() {
    terminal.synchronized {
      terminal.moveCursor(originX, originY + height + Game.BorderSize)
      s"Score: $score".foreach(terminal.putCharacter)
      terminal.moveCursor(0, 0)
    }
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

  def init(terminal: Terminal, width: Int, height: Int, originX: Int, originY: Int): State = {
    val left = originX - (width / 2)
    val top = originY - (height / 2)
    val origin = (left, top)
    val player = Player(left + (width / 4), top + (height / 4))
    val score = 0L
    val platform = CharBuffer.empty(width, height)
    val obstacle = Obstacle(height).buffer
    State(terminal, width, height, origin, player, score, platform, obstacle, obstacleDelay = 0)
  }
}
