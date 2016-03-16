package domain

import scala.util.Random
import com.googlecode.lanterna.input.Key
import com.googlecode.lanterna.terminal.Terminal
import org.slf4j.LoggerFactory

case class State private(
    terminal: Terminal,
    width: Int,
    height: Int,
    origin: (Int, Int),
    player: Player,
    maxMissiles: Int,
    activeMissiles: Seq[(Int, Int)],
    score: Long,
    platform: CharBuffer,
    obstacle: CharBuffer,
    obstacleDelay: Int) {

  private val logger = LoggerFactory.getLogger(getClass)

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
    /*
    val updatedPlatform = activeMissiles.foldLeft(platform) { (platform, missile) =>
      val (x, y) = missile
      if (platform.getChar(x, y) != CharBuffer.EmptyChar) {
        //platform.putChar(x, y, CharBuffer.EmptyChar)
        platform
      } else {
        platform
      }
    }
    */
    if (obstacleDelay > 0)
      copy(platform = platform.scrollLeft(), obstacleDelay = obstacleDelay - 1)
    else if (obstacle.isEmpty)
      copy(platform = platform.scrollLeft(), obstacle = obstacles.next(), obstacleDelay = Random.nextInt(5) + 1)
    else
      copy(platform = platform.scrollLeft(obstacle.getColumn(0)), obstacle = obstacle.dropLeftColumn())
  }

  def updatePlayer(): State = {
    terminal.synchronized {
      val updatedPlayer = terminal.readInput() match {
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
            case ' ' | 'm'          => player.fireMissile()
            case _                  => player
          }
          case _                    => player
        }
      }
      val updatedActiveMissiles = if (player.numMissiles > updatedPlayer.numMissiles) {
        activeMissiles :+ (player.x + 1 -> player.y)
      } else {
        activeMissiles
      }
      copy(player = updatedPlayer, activeMissiles = updatedActiveMissiles)
    }
  }

  def updateActiveMissiles(): State = {
    val updatedActiveMissiles = activeMissiles.flatMap { case (x, y) =>
      // out of game bounds
      if (x < originX || y < originY || x >= originX + width || y >= originY + height) {
        None
      }
      // hit obstacle, clear the coordinate
      else if (platform.getChar(x - originX, y - originY) != CharBuffer.EmptyChar) {
        Some((x + 1, y))
      }
      // continue rendering
      else {
        Some((x + 1, y))
      }
    }
    copy(activeMissiles = updatedActiveMissiles)
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

  def renderActiveMissiles() {
    activeMissiles.foreach { case (x, y) =>
      renderToken(x, y, "*")
    }
  }

  def renderInactiveMissiles() {
    def draw(m: Int, n: Int, char: Char = ' ') {
      terminal.moveCursor(originX + width - (m * 2), originY + height + Game.BorderSize)
      (1 to n).foreach { _ =>
        terminal.putCharacter(' ')
        terminal.putCharacter(char)
      }
    }
    val playerMissiles = math.min(maxMissiles, player.numMissiles)
    terminal.synchronized {
      draw(maxMissiles, maxMissiles, ' ')
      draw(maxMissiles, playerMissiles, '*')
      terminal.moveCursor(0, 0)
    }
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

  def init(terminal: Terminal, width: Int, height: Int, originX: Int, originY: Int, numMissiles: Int): State = {
    val left = originX - (width / 2)
    val top = originY - (height / 2)
    val origin = (left, top)
    val player = Player(left + (width / 4), top + (height / 4), numMissiles)
    val activeMissiles = Seq.empty[(Int, Int)]
    val score = 0L
    val platform = CharBuffer.empty(width, height)
    val obstacle = Obstacle(height).buffer
    State(terminal, width, height, origin, player, numMissiles, activeMissiles, score, platform, obstacle, obstacleDelay = 0)
  }
}
