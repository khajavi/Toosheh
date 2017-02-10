package com.company.toosheh.actors

import akka.actor.Actor
import akka.event.Logging
import com.company.toosheh.messages.{GetRequest, SetRequest, UnsetRequest}

import scala.collection.mutable

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
class DB extends Actor {
  val log = Logging(context.system, this)

  val map = new mutable.HashMap[String, String]()

  override def receive = {
    case SetRequest(key, value) =>
      map += key -> value
      sender ! Right("OK")
    case GetRequest(key) =>
      map get key map (sender ! Right(_)) getOrElse sender ! Left("Nil")
    case UnsetRequest(key) => {
      map -= key
      sender ! Right("OK")
    }
    case other => log.info("unrelated command {}", other)
  }

}