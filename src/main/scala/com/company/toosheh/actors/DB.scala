package com.company.toosheh.actors

import akka.actor.Actor
import akka.event.Logging
import com.bisphone.util.LongCodec
import com.company.toosheh.messages._

import scala.collection.mutable

object Long extends LongCodec.BigEndianDecoder with LongCodec.BigEndianEncoder

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
class DB extends Actor {
  val log = Logging(context.system, this)

  val map      = new mutable.HashMap[String, Array[Byte]]()
  val numerics = new mutable.HashSet[String]()

  override def receive = {
    case SetRequest(key, value) =>
      map += key -> value
      sender ! Right("OK")

    case GetRequest(key) =>
      map get key map (sender ! Right(_)) getOrElse sender ! Left("Nil")

    case UnsetRequest(key) =>
      map -= key
      sender ! Right("OK")

    case InitCounterRequest(key, value) =>
      if (map contains key)
        sender ! Right("key exist")
      else {
        map += key -> Long.encodeLong(value)
        numerics += key
        sender ! Right(value)
      }

    case IncRequest(key) =>
      incdec(key, 1)(_ + 1)

    case DecRequest(key) =>
      incdec(key, -1)(_ - 1)

    case other =>
      log.info("unrelated command {}", other)
  }

  def incdec(key: String, initial: Long) = (fn: Long => Long) =>
    map
      .get(key)
      .fold {
        val bvalue = Long.encodeLong(initial)
        map += key -> bvalue
        numerics += key
        sender ! Right(initial)
      } { bvalue =>
        if (numerics contains key) {
          val lvalue = fn(Long.decodeLong(bvalue.iterator, bvalue.length))
          map += key -> Long.encodeLong(lvalue)
          sender ! Right(lvalue)
        } else
          sender ! Left("not numeric")
      }
}