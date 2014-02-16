package domain

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{Executors, Future}
import java.nio.charset.Charset
import com.googlecode.lanterna.input.Key
import com.googlecode.lanterna.terminal.text.UnixTerminal
import org.slf4j.LoggerFactory
import util.{JLineTerminalSizeQuerier, EventLoop}

class Game(width: Int, height: Int, difficulty: Game.Level) {

  private val logger = LoggerFactory.getLogger(getClass)

  private val executor = Executors.newFixedThreadPool(3)

  private val futures = new Array[Future[_]](2)

  private val terminal = new UnixTerminal(System.in, System.out, Charset.forName("UTF8"), JLineTerminalSizeQuerier)

  terminal.enterPrivateMode()

  private val (originX, originY) = {
    val screenSize = terminal.getTerminalSize
    val originX = screenSize.getColumns / 2
    val originY = screenSize.getRows / 2
    (originX, originY)
  }

  private var state: State = _

  private val isOver = new AtomicBoolean(false)

  private val isShutdown = new AtomicBoolean(false)

  private val playerLoop = new EventLoop(fps = 24) {
    def update() {
      state.synchronized {
        state.hidePlayer()
        state = state.updatePlayer
        state = state.updateScore(difficulty)
        if (!state.isValid) stop()
      }
    }

    def render() {
      state.synchronized {
        state.renderPlayer()
        state.renderScore()
      }
    }
  }

  private val platformLoop = new EventLoop(fps = difficulty.value) {
    def update() {
      state.synchronized {
        state = state.updatePlatform
        if (!state.isValid) stop()
      }
    }

    def render() {
      state.synchronized {
        state.renderPlatform()
        state.renderPlayer()
      }
    }
  }

  private def promptLoop = new EventLoop(fps = 24) {
    def update() {
      terminal.synchronized {
        val key = terminal.readInput()
        if (key != null && key.getKind == Key.Kind.NormalKey)
          key.getCharacter match {
            case 'n' =>
              restart()
              break
            case 'q' =>
              shutdown()
              break
            case  _  =>
          }
      }
    }

    def render() {
      /* no-op */
    }
  }

  def start() {
    logger.debug("Starting")
    terminal.synchronized {
      terminal.clearScreen()
      state = State.init(terminal, width, height, originX, originY)
    }
    isOver.compareAndSet(true, false)
    futures(0) = executor.submit(playerLoop)
    futures(1) = executor.submit(platformLoop)
  }

  def stop() {
    logger.debug("Stop requested")
    if (isOver.get)
      logger.debug("Already stopped")
    else {
      logger.debug("Stopping")
      isOver.set(true)
      futures.foreach(_.cancel(true))
      val message = state.synchronized {
        s"Game Over\nScore: ${state.score}\nPress `n` for a new game\nPress `q` to quit"
      }
      prompt(message)
      futures(2) = executor.submit(promptLoop)
    }
  }

  def restart() {
    logger.debug("Restart requested")
    futures.foreach(_.cancel(true))
    start()
  }

  def shutdown() {
    logger.debug("Shutdown requested")
    if (isShutdown.get)
      logger.debug("Already shutdown")
    else {
      logger.debug("Shutting down")
      isShutdown.set(true)
      futures.foreach(_.cancel(true))
      executor.shutdown()
      terminal.synchronized {
        terminal.clearScreen()
        terminal.flush()
        terminal.exitPrivateMode()
      }
    }
  }

  private def prompt(message: String) = {
    val messages = message.split('\n')
    val x = originX - (messages.maxBy(_.size).size / 2).toInt
    var y = originY - (messages.size / 2).toInt
    terminal.synchronized {
      terminal.clearScreen()
      messages.foreach { message =>
        terminal.moveCursor(x, y)
        message.foreach(terminal.putCharacter)
        y += 1
      }
      terminal.moveCursor(0, 0)
      terminal.flush()
    }
  }
}

object Game {

  def apply(width: Int = MinWidth, height: Int = MinHeight, difficulty: Level = Levels.Normal): Game = {
    new Game(math.max(width, MinWidth), math.max(height, MinHeight), difficulty)
  }

  val MinWidth = 28

  val MaxWidth = 80

  val MinHeight = 12

  val MaxHeight = 40

  val BorderSize = 1

  sealed trait Level {

    val value: Int
  }

  object Levels {

    def valueOf(i: Int): Level = if (i <= 1) Easy else if (i < 4) Normal else Difficult

    case object Easy extends Level {

      val value = 1
    }

    case object Normal extends Level {

      val value = 2
    }

    case object Difficult extends Level {

      val value = 4
    }
  }
}
