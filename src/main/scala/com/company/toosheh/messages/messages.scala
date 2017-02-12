package com.company.toosheh.messages

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
case class GetRequest(key: String)

case class SetRequest(key: String, value: Array[Byte])

case class UnsetRequest(key: String)

case class InitCounterRequest(key: String, value: Long)

case class IncRequest(key: String)

case class DecRequest(key: String)