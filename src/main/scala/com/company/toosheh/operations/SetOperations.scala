package com.company.toosheh.operations

import com.bisphone.sarf.Func
import com.bisphone.util.AsyncResult
import com.company.toosheh.protocol.SetProtocol.{Error, Get, Set, UnSet}

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object SetOperations {
  def set = Func[Set] {
    case Set(key, _) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Set(key, value) => ???
  }

  def get = Func[Get] {
    case Get(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case Get(key) => ???
  }

  def unset = Func[UnSet] {
    case UnSet(key) if key.isEmpty =>
      AsyncResult left Error("key should not empty!")
    case UnSet(key) => ???
  }
}
