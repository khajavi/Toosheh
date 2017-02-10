package com.company.toosheh.actors

import akka.actor.{Actor, Status}
import akka.event.Logging
import com.company.toosheh.messages.{GetRequest, SetRequest}

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
      sender() ! value
    case GetRequest(key) =>
      map get key match {
        case Some(v) => sender() ! v
        case None => {
          Status.Failure(throw new Exception("Key not found"))
        }
      }
    case other => log.info("unrelated command {}", other)
  }
}