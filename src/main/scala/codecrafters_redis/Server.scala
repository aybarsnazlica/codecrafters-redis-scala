package codecrafters_redis

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.{InetSocketAddress, ServerSocket}

object Server {
  def main(args: Array[String]): Unit = {
    val serverSocket = new ServerSocket()
    serverSocket.bind(new InetSocketAddress("localhost", 6379))

    val clientSocket = serverSocket.accept() // wait for client

    while (true) {
      val input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream)).readLine()
      val output = new PrintStream(clientSocket.getOutputStream)

      input match {
        case _ => output.write("+PONG\r\n".getBytes)
      }
    }
  }
}
