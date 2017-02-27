import domain.Game

import util.Args

object AsciiBird {

  def main(rawArgs: Array[String]) {
    val args = Args(rawArgs)
    val width = args.optional("width")
      .map(_.toInt)
      .getOrElse(Game.MinWidth)
    val height = args.optional("height")
      .map(_.toInt)
      .getOrElse(Game.MinHeight)
    val difficulty = args.optional("difficulty")
      .map(_.toInt)
      .map(Game.Levels.valueOf)
      .getOrElse(Game.Levels.Normal)
    val game = Game(width, height, difficulty)
    sys.addShutdownHook {
      game.shutdown()
    }
    game.start()
  }
}
