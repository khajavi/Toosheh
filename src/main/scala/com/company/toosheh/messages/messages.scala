package com.company.toosheh.messages

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
case class GetRequest(key: String)

case class SetRequest(key: String, value: String)
