import domain.Game

import scala.util.Try

object AsciiBird {

  def main(args: Array[String]) {
    val difficulty = Try(args(0).toInt).map(Game.Levels.valueOf).getOrElse(Game.Levels.Normal)
    val width = Try(args(1).toInt).getOrElse(Game.MinWidth)
    val height = Try(args(2).toInt).getOrElse(Game.MinHeight)
    val game = Game(width, height, difficulty)
    sys.addShutdownHook {
      game.shutdown()
    }
    game.start()
  }
}
