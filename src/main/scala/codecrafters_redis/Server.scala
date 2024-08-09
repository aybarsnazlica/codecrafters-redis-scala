package codecrafters_redis

import java.net.{InetSocketAddress, ServerSocket, Socket}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.BufferedSource
import scala.util.Using

object Server {
  def main(args: Array[String]): Unit = {
    val serverSocket = new ServerSocket(6379)
    println("Server is running on port 6379")

    while (true) {
      val clientSocket = serverSocket.accept()
      Future(handleClient(clientSocket))
    }
  }

  private def handleClient(clientSocket: Socket): Unit = {
    Using.resources(
      new BufferedSource(clientSocket.getInputStream),
      clientSocket.getOutputStream
    ) { (source, outputStream) =>
      while (source != null) {
        val bulkStr = Parser.parse(source)
        val response = bulkStr.cmd match {
          case "echo" => s"$$${bulkStr.value1.length}\r\n${bulkStr.value1}\r\n"
          case "ping" => "+PONG\r\n"
          case _ => "-ERR unknown command\r\n"
        }
        outputStream.write(response.getBytes)
        outputStream.flush()
      }
    }
    clientSocket.close()
  }
}