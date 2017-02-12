package com.company.toosheh.operations

import akka.actor.Props
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import com.bisphone.sarf.Func
import com.bisphone.std._
import com.bisphone.util.{AsyncResult, LongCodec}
import com.company.toosheh.DBActorSystem
import com.company.toosheh.actors.DB
import com.company.toosheh.messages._
import com.company.toosheh.protocol.SetProtocol.{BinaryGet, BinarySet, BinaryUnSet, BinaryValue, Decr, Error, Incr, InitCounter, LongValue, StringGet, StringSet, StringUnSet, StringValue, Success}

object Long extends LongCodec.BigEndianDecoder with LongCodec.BigEndianEncoder

import scala.concurrent.duration._

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object SetOperations {

  import DBActorSystem._

  val db = system.actorOf(Props[DB], name = "dbactor")

  implicit val duration: Timeout = 20 seconds

  def sset = Func[StringSet] {
    case StringSet(key, _) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case StringSet(key, value) => {
      val res = (db ? SetRequest(key, value.map(_.toByte).toArray))
        .mapTo[Either[String, String]] //TODO: fix unsafe cast
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(Success(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def bset = Func[BinarySet] {
    case BinarySet(key, _) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case BinarySet(key, value) => {
      val res = (db ? SetRequest(key, value))
        .mapTo[Either[String, String]] //TODO: fix unsafe cast
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(Success(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }


  def sget = Func[StringGet] {
    case StringGet(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case StringGet(key) => {
      val res = (db ? GetRequest(key)).mapTo[Either[String, Array[Byte]]] //TODO: fix unsafe cast
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(StringValue(ByteString(v).utf8String))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def bget = Func[BinaryGet] {
    case BinaryGet(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case BinaryGet(key) => {
      val res = (db ? GetRequest(key)).mapTo[Either[String, Array[Byte]]] //TODO: fix unsafe cast
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(BinaryValue(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def sunset = Func[StringUnSet] {
    case StringUnSet(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case StringUnSet(key) => {
      val res = (db ? UnsetRequest(key)).mapTo[Either[String, String]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(Success(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def bunset = Func[BinaryUnSet] {
    case BinaryUnSet(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case BinaryUnSet(key) => {
      val res = (db ? UnsetRequest(key)).mapTo[Either[String, String]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(Success(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def initc = Func[InitCounter] {
    case InitCounter(key, _) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case InitCounter(key, value) => {
      val res = (db ? InitCounterRequest(key, value))
        .mapTo[Either[String, Long]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(LongValue(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def incr = Func[Incr] {
    case Incr(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Incr(key) => {
      val res = (db ? IncRequest(key)).mapTo[Either[String, Long]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(LongValue(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def decr = Func[Decr] {
    case Decr(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Decr(key) => {
      val res = (db ? DecRequest(key)).mapTo[Either[String, Long]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(LongValue(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }
}
