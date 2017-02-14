package com.company.toosheh

import akka.actor.{ActorSystem, Props}
import com.company.toosheh.actors.DB

import scala.concurrent.ExecutionContext

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object DBActorSystem {
  implicit val ec     = ExecutionContext.Implicits.global
  implicit val system = ActorSystem()
  val db = system.actorOf(Props[DB], name = "dbactor")
}
