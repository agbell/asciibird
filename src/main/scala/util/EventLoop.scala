package util

import org.slf4j.LoggerFactory

abstract class EventLoop(fps: Int) extends Runnable {

  private val logger = LoggerFactory.getLogger(getClass)

  @volatile
  private var isRunning = false

  private val actualFps = math.min(fps, EventLoop.MaxFps)

  private val framePeriod = 1000 / actualFps

  private val frameSkipsAllowed = math.ceil(((actualFps * EventLoop.MaxFrameSkipsAllowed) / EventLoop.MaxFps)).toInt

  def update(): Unit

  def render(): Unit

  def break = throw EventLoop.Break

  def run() {
    isRunning = true
    while (isRunning) {
      try {
        val startTime = System.currentTimeMillis
        update()
        render()
        val elapsedTime = System.currentTimeMillis - startTime
        var sleepTime = framePeriod - elapsedTime

        if (sleepTime > 0)
          Thread.sleep(sleepTime)
        else {
          var i = 0
          while (sleepTime < 0 && i < frameSkipsAllowed) {
            update()
            sleepTime += framePeriod
            i += 1
          }
        }
      } catch {
        case EventLoop.Break =>
          logger.debug("Break!")
          isRunning = false
        case e: InterruptedException =>
          logger.debug("Interrupted")
          isRunning = false
          Thread.currentThread.interrupt()
      }
    }
  }
}

object EventLoop {

  val MaxFps = 60

  val MaxFrameSkipsAllowed = 5

  private case object Break extends Throwable
}
