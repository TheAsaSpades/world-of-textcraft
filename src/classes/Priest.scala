package classes

import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import entities.Player
import room.Room

class Priest extends Class {
  def classCommands(in: String, pl: Player, pla: ActorRef) = {
    if (in.startsWith("heal")) heal(pl, in.drop(5), pla)
    else pla ! Player.PrintMessage("What?")

  }

  def heal(pl: Player, nm: String, pla: ActorRef) = {
    if (pl.level < 3) {
      pla ! Player.PrintMessage("Level too low to use heal!")
    } else pl.location ! Room.CheckInRoom("heal", nm, pla)
  }

  val abilitySpeed = 20
  val abilityPower = 3
  val abilities = Map("Heal: heal a target" -> 3)

  val name = "Priest"

  val stamina = 100

  val classPower = 150

  val dmgReduc = 10

  val hlthInc = 10

}