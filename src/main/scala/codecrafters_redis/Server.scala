package codecrafters_redis

import java.net.{InetSocketAddress, ServerSocket, Socket}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.Using

case class BulkString(cmd: String, value: String)

object Server {
  def main(args: Array[String]): Unit = {
    val serverSocket = new ServerSocket()
    serverSocket.bind(new InetSocketAddress("localhost", 6379))
    println("Server is running on port 6379")

    while (true) {
      val clientSocket = serverSocket.accept()
      println("Client connected")
      handleClient(clientSocket)
    }
  }


  private def parse(source: Source): BulkString = {
    val lines = source.getLines()

    // Read the first line to determine the number of arguments (we'll skip this for simplicity)
    lines.next() // *1 or *2

    // Skip the line containing the length of the command
    lines.next() // $4

    // Extract the command
    val cmd = lines.next().toLowerCase() // PING or ECHO

    // If the command is PING, return it without a value
    if (cmd == "ping") {
      BulkString(cmd = cmd, value = "")
    } else {
      // For other commands like ECHO, continue parsing
      lines.next() // $<length>
      val value = lines.next() // value like "grape"
      BulkString(cmd = cmd, value = value)
    }
  }

  private def handleClient(clientSocket: Socket): Future[Unit] = Future {
    Using.resources(
      Source.fromInputStream(clientSocket.getInputStream),
      clientSocket.getOutputStream
    ) { (source, outputStream) =>

      val bulkStr = parse(source)

      bulkStr.cmd match {
        case "echo" =>
          outputStream.write(s"$$${bulkStr.value.length}\r\n${bulkStr.value}\r\n".getBytes)
        case "ping" =>
          outputStream.write("+PONG\r\n".getBytes)
        case _ =>
          outputStream.write("-ERR unknown command\r\n".getBytes)
      }
      outputStream.flush()
    }
    clientSocket.close()
  }
}