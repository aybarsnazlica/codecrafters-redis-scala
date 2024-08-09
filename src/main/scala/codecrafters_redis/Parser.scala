package codecrafters_redis

import scala.io.BufferedSource

object Parser {
  case class BulkString(cmd: String, value1: String)

  def parse(source: BufferedSource): BulkString = {
    val lines = source.getLines()
    lines.next() // Skip command count (e.g., *1 or *2)
    lines.next() // Skip command length (e.g., $4)
    val cmd = lines.next().toLowerCase()

    val value = if (cmd == "ping") "" else {
      lines.next() // Skip value length
      lines.next() // Actual value
    }

    BulkString(cmd, value)
  }
}