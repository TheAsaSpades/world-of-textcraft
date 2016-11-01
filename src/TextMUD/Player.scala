package TextMUD

import scala.io.Source
import akka.actor.Actor
import java.io.InputStream
import java.io.PrintStream
import akka.actor.ActorRef
import java.io.BufferedReader
import java.net.Socket


class Player(
    val name: String,
    private var health:Double,
    private var _inventory: MutableDLList[Item],
    val input: BufferedReader,
    val output: PrintStream,
    val sock: Socket) extends Actor {

  import Player._
  //location access
  private var _location: ActorRef = null
  def location = _location

  //Actor Management
  def receive = {
    case ProcessInput =>
      if (input.ready()) {
        val in = input.readLine().trim
        if (in.nonEmpty) {
          processCommand(in)
        }
      }
    case PrintMessage(msg) => output.println(msg)
    case AddToInventory(item) =>
      item match {
        case Some(item) =>
          addToInventory(item)
        case None =>
          output.println("Item not found")
      }
    case TakeExit(dir) =>
      dir match {
        case Some(dest) =>
          if (_location != null) location ! Room.LeaveRoom(self, name)
          _location = dest
          location ! Room.EnterRoom(self, name)
          location ! Room.PrintDescription
        case None =>
          output.println("You can't go that way")
      }
  }

  //Inventory Management
  def inventory = _inventory

  def getFromInventory(itemName: String): Option[Item] = {
    inventory.find(_.name == itemName) match {
      case Some(item) => {
        _inventory = _inventory.filter(_ != item)
        Some(item)
      }
      case None =>
        output.println("You can't drop something you don't have.")
        None
    }
  }

  def inspectItem(item: String): Unit = {
    for (i <- 0 until inventory.length; if (inventory(i).name == item)) yield output.println(inventory(i).description)
  }

  def addToInventory(item: Item): Unit = {
     _inventory += item
  }

  private def emptyInventory: Boolean = {
    inventory.isEmpty
  }

  def printInventory(): Unit = {
    if (emptyInventory == true) output.println("You have nothing in your bag.")
    else {
      output.println("You have: ")
      for (i <- 0 until inventory.length) yield output.println(inventory(i).name)
    }
  }

  //Move Player
  private def move(direction: Int): Unit = {
    location ! Room.GetExit(direction)
  }

  //Player Tell Messaging
  def tellMessage(s: String): Unit = {
    val Array(_, to, msg) = s.split(" +", 3)
    Main.entityManager ! EntityManager.PrintTellMessage(to, name, msg)
  }

  //Process Player Input
  def processCommand(in: String) = {
    //player quit
    if ("quit".startsWith(in)) {
      sock.close()
      location ! Room.LeaveGame(self, name)
    } //player movement
    else if ("north".startsWith(in)) move(0)
    else if ("south".startsWith(in)) move(1)
    else if ("east".startsWith(in)) move(2)
    else if ("west".startsWith(in)) move(3)
    else if ("up".startsWith(in)) move(4)
    else if ("down".startsWith(in)) move(5)
    else if ("look".startsWith(in)) location ! Room.PrintDescription
    //player inventory
    else if (in.length > 0 && "inventory".startsWith(in)) printInventory
    else if (in.startsWith("inspect")) inspectItem(in.trim.drop(8))
    else if (in.startsWith("get")) location ! Room.GetItem(name, in.trim.drop(4))
    else if (in.startsWith("drop")) getFromInventory(in.trim.drop(5)) match {
      case Some(item) =>
        location ! Room.DropItem(name, item)
      case None => None
    } //player messaging
    else if (in.startsWith("shout")) {
      Main.entityManager ! EntityManager.PrintShoutMessage(in.drop(6), name)
    } else if (in.startsWith("say")) location ! Room.SayMessage(in.drop(4), name)
    else if (in.startsWith("tell")) tellMessage(in)
    //help command
    else if ("help".startsWith(in)) {
      val source = Source.fromFile("help.txt")
      val lines = source.getLines()
      lines.foreach(output.println)
    } else output.println("What?")
  }
}
object Player {
  case object ProcessInput
  case class PrintMessage(msg: String)
  case class AddToInventory(item: Option[Item])
  case class TakeExit(dir: Option[ActorRef])
  
  val playerHealth = 100.0
}