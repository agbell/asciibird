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

  private val executor = Executors.newFixedThreadPool(2)

  private val futures = new Array[Future[_]](2)

  private val terminal = new UnixTerminal(System.in, System.out, Charset.forName("UTF8"), JLineTerminalSizeQuerier)

  private var state: State = _

  private val isOver = new AtomicBoolean(false)

  private val isShutdown = new AtomicBoolean(false)

  private val playerLoop = new EventLoop(fps = 24) {
    def update() {
      state.synchronized {
        state.hidePlayer()
        state = state.updatePlayer
        if (!state.isValid) stop()
      }
    }

    def render() {
      state.synchronized {
        state.renderPlayer()
      }
    }
  }

  private val platformLoop = new EventLoop(fps = 2) {
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

    def render() {
      /* no-op */
    }
  }

  def start() {
    logger.debug("Starting")
    terminal.enterPrivateMode()
    state = State.init(terminal)
    isOver.compareAndSet(true, false)
    futures(0) = executor.submit(playerLoop)
    futures(1) = executor.submit(platformLoop)
  }

  def stop() {
    logger.debug("Stop requested")
    if (isOver.get == false) {
      logger.debug("Stopping")
      isOver.set(true)
      futures.foreach(_.cancel(true))
      val message = s"Game Over\nScore: ${state.score}\nPress `n` for a new game\nPress `q` to quit"
      prompt(message)
      futures(2) = executor.submit(promptLoop)
    } else {
      logger.debug("Already stopped")
    }
  }

  def restart() {
    logger.debug("Restart requested")
    futures.foreach(_.cancel(true))
    terminal.clearScreen()
    start()
  }

  def shutdown() {
    logger.debug("Shutdown requested")
    if (isShutdown.get == false) {
      logger.debug("Shutting down")
      isShutdown.set(true)
      futures.foreach(_.cancel(true))
      executor.shutdown()
      terminal.clearScreen()
      terminal.flush()
      terminal.exitPrivateMode()
    } else {
      logger.debug("Already shutdown")
    }
  }

  private def prompt(message: String) = {
    val messages = message.split('\n')
    val screenSize = terminal.getTerminalSize
    val x = math.floor(screenSize.getColumns / 2).toInt - (messages.maxBy(_.size).size / 2).toInt
    var y = math.floor(screenSize.getRows / 2).toInt - (messages.size / 2).toInt
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

object Game {

  def apply(width: Int = Width, height: Int = Height, difficulty: Level = Levels.Normal): Game = {
    new Game(width, height, difficulty)
  }

  val Width = 28

  val Height = 12

  val Border = 1

  sealed trait Level

  object Levels {

    // todo adjust fps to 1
    case object Easy extends Level

    // todo adjust fps to 2
    case object Normal extends Level

    // todo adjust fps to 4
    case object Difficult extends Level
  }
}
