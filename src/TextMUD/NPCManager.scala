package TextMUD

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef

class NPCManager extends Actor {
  import NPCManager._
  def receive = {
    case NewNPC(name, health, loc, atk, armr, spd, itms, desc) =>
      val n = context.actorOf(Props(new NPC(name, health, atk, armr, spd, itms, desc)), name)
      Main.roomManager ! RoomManager.EnterRoom(loc, n)
      sender ! NPC.SetLoc(loc)
  }

}
object NPCManager {
  //NPC Management
  case class NewNPC(name: String, health: Double, loc: String, atk: Int, armr: Int, spd: Int, itms: List[Item], desc: String)
}