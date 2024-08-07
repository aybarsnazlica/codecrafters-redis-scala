package codecrafters_redis

import java.net.{InetSocketAddress, ServerSocket, Socket}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.util.Using

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

  private def handleClient(clientSocket: Socket): Future[Unit] = Future {
    Using.resources(
      Source.fromInputStream(clientSocket.getInputStream),
      clientSocket.getOutputStream
    ) { (source, outputStream) =>
      source.getLines().foreach { line =>
        if (line.startsWith("PING")) {
          outputStream.write("+PONG\r\n".getBytes())
          outputStream.flush()
        }
      }
    }
    clientSocket.close()
  }
}