package TextMUD

import io.StdIn._
import java.io.PrintStream
import java.io.InputStream
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._
import scala.concurrent.Future
import java.net.ServerSocket
import java.io.BufferedReader
import java.io.InputStreamReader

object Main extends App {
  //Initializes system, Player Manager, and Room Manager
  val system = ActorSystem("Main")
  val playerManager = system.actorOf(Props[PlayerManager], "PlayerManager")
  val roomManager = system.actorOf(Props[RoomManager], "RoomManager")
  val npcManager = system.actorOf(Props[NPCManager], "NPCManager")
  val activityManager = system.actorOf(Props[ActivityManager], "ActivityManager")

  //Checks and schedules checks for connections
  implicit val ec = system.dispatcher
  def checkConnections(): Unit = {
    val ss = new ServerSocket(4445)
    while (true) {
      val sock = ss.accept()
      val in = new BufferedReader(new InputStreamReader(sock.getInputStream))
      val out = new PrintStream(sock.getOutputStream)
      Future {
        out.println("What is your name? \nNo spaces. Letters only.")
        var name = in.readLine()
        if (checkName(name)) {
          out.println("I said no spaces or numbers Zach!")
          name = in.readLine()
        }
        playerManager ! PlayerManager.NewPlayer(name, Player.playerHealth, "FirstRoom", new MutableDLList[Item](), in, out, sock)
      }
    }
  }
  system.scheduler.schedule(0.seconds, 0.1.seconds, activityManager, ActivityManager.CheckQueue)
  system.scheduler.schedule(0.seconds, 0.1.seconds, playerManager, PlayerManager.CheckInput)
  checkConnections()

  private def checkName(name: String): Boolean = {
    name.contains(" ") || name.contains("1") || name.contains("2") || name.contains("3") || name.contains("4") || name.contains("5") || name.contains("6") || name.contains("7") || name.contains("8") || name.contains("9") || name.contains("0")
  }
}