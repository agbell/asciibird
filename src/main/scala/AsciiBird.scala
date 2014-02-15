import domain.Game

object AsciiBird {

  def main(args: Array[String]) {

    def parseArg[A](i: Int, default: A)(f: String => A): A = (try {
      args.lift(i).map(f)
    } catch {
      case e: Exception => None
    }).getOrElse(default)

    val difficulty  = parseArg[Game.Level](0, default = Game.Levels.Normal) { arg =>
      Game.Levels.valueOf(arg.toInt)
    }

    val width = parseArg(1, default = Game.MinWidth)(_.toInt)

    val height = parseArg(2, default = Game.MinHeight)(_.toInt)

    val game = Game(width, height, difficulty)

    sys.addShutdownHook {
      game.shutdown()
    }

    game.start()
  }
}
