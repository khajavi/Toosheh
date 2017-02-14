package com.company.toosheh.operations

import akka.pattern.ask
import akka.util.Timeout
import com.bisphone.sarf.Func
import com.bisphone.std._
import com.bisphone.util.{AsyncResult, LongCodec}
import com.company.toosheh.DBActorSystem
import com.company.toosheh.messages.{DecRequest, IncRequest, InitCounterRequest}
import com.company.toosheh.protocol.CounterProtocol.{Decr, Incr, InitCounter, LongValue}
import com.company.toosheh.protocol.ProtocolUtils.Error

import scala.concurrent.duration._

object Long extends LongCodec.BigEndianDecoder with LongCodec.BigEndianEncoder

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object CounterOperations {

  import DBActorSystem._

  implicit val duration: Timeout = 20 seconds

  def initc = Func[InitCounter] {
    case InitCounter(key, _) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case InitCounter(key, value) =>
      val res = (db ? InitCounterRequest(key, value))
        .mapTo[Either[String, Long]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(LongValue(v))
        case Left(v) => StdLeft(Error(v))
      }
  }

  def incr = Func[Incr] {
    case Incr(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Incr(key) =>
      val res = (db ? IncRequest(key)).mapTo[Either[String, Long]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(LongValue(v))
        case Left(v) => StdLeft(Error(v))
      }
  }

  def decr = Func[Decr] {
    case Decr(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Decr(key) =>
      val res = (db ? DecRequest(key)).mapTo[Either[String, Long]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(LongValue(v))
        case Left(v) => StdLeft(Error(v))
      }
  }
}
