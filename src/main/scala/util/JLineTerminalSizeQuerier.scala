package util

import com.googlecode.lanterna.terminal.TerminalSize
import com.googlecode.lanterna.terminal.text.UnixTerminalSizeQuerier

object JLineTerminalSizeQuerier extends UnixTerminalSizeQuerier {

  def queryTerminalSize(): TerminalSize = {
    val terminal = jline.TerminalFactory.get()
    new TerminalSize(terminal.getWidth, terminal.getHeight)
  }
}
