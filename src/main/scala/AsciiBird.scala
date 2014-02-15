import domain.Game

object AsciiBird {

  def main(args: Array[String]) {
    val game = Game()
    sys.addShutdownHook {
      game.shutdown()
    }
    game.start()
  }
}
