package codecrafters_redis

import scala.io.BufferedSource

object Parser {
  case class BulkString(cmd: String, value1: String, value2: String)

  def parse(source: BufferedSource): BulkString = {
    val lines = source.getLines()

    lines.next() // Skip command count (e.g., *1 or *2)
    lines.next() // Skip command length (e.g., $3 for SET)
    val cmd = lines.next().toLowerCase()

    val value1 = if (cmd != "ping") {
      lines.next() // Skip length
      lines.next() // Actual value
    } else ""

    val value2 = if (cmd == "set") {
      lines.next() // Skip length
      lines.next() // Actual value
    } else ""

    BulkString(cmd, value1, value2)
  }
}