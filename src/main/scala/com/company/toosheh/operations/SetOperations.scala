package com.company.toosheh.operations

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.bisphone.sarf.Func
import com.bisphone.std._
import com.bisphone.util.AsyncResult
import com.company.toosheh.DBActorSystem
import com.company.toosheh.actors.DB
import com.company.toosheh.messages.{GetRequest, SetRequest, UnsetRequest}
import com.company.toosheh.protocol.SetProtocol.{Error, Get, Set, UnSet, Value}

import scala.concurrent.duration._

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object SetOperations {

  import DBActorSystem._

  val db = system.actorOf(Props[DB], name = "dbactor")

  implicit val duration: Timeout = 20 seconds

  def set = Func[Set] {
    case Set(key, _) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Set(key, value) => {
      val res = (db ? SetRequest(key, value)).mapTo[Either[String, String]] //TODO: fix unsafe cast
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(Value(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }

  def get = Func[Get] {
    case Get(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Get(key) => {
      val res = (db ? GetRequest(key)).mapTo[Either[String, String]] //TODO: fix unsafe cast
      AsyncResult fromFuture res.map {
        case Left(v) => StdLeft(Error(v))
        case Right(v) => StdRight(Value(v))
      }
    }
  }

  def unset = Func[UnSet] {
    case UnSet(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case UnSet(key) => {
      val res = (db ? UnsetRequest(key)).mapTo[Either[String, String]]
      AsyncResult fromFuture res.map {
        case Right(v) => StdRight(Value(v))
        case Left(v) => StdLeft(Error(v))
      }
    }
  }
}
