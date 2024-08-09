package codecrafters_redis

import java.net.{ServerSocket, Socket}
import scala.collection.concurrent.TrieMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.BufferedSource
import scala.util.Using

object Server {
  private val storage: TrieMap[String, String] = TrieMap()

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
        println(bulkStr)
        val response = bulkStr.cmd match {
          case "ping" => "+PONG\r\n"
          case "echo" => s"$$${bulkStr.value1.length}\r\n${bulkStr.value1}\r\n"
          case "set" =>
            storage.put(bulkStr.value1, bulkStr.value2)
            "+OK\r\n"
          case "get" =>
            storage.get(bulkStr.value1) match {
              case Some(value) => s"$$${value.length}\r\n$value\r\n"
              case None => "$-1\r\n" // RESP format for nil
            }
          case _ => "-ERR unknown command\r\n"
        }
        outputStream.write(response.getBytes)
        outputStream.flush()
      }
    }
    clientSocket.close()
  }
}