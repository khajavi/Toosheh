package com.company.toosheh

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object DBActorSystem {
  implicit val ec     = ExecutionContext.Implicits.global
  implicit val system = ActorSystem()
}
