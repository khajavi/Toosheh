package com.company.toosheh.protocol

import akka.util.ByteString
import com.bisphone.sarf.{Func, TypeKey}

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object CounterProtocol {

  import ProtocolUtils._

  case class InitCounter(key: String, value: Long) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = LongValue
  }

  implicit val initCounterKey: TypeKey[InitCounter] = TypeKey[InitCounter](11)

  implicit val initCounterWriter = writer[InitCounter] { value =>
    Untracked(
      initCounterKey,
      ByteString(value.key),
      Some(ByteString(Long.encodeLong(value.value)))
    )
  }

  implicit val intiCounterReader = reader[InitCounter] { frame =>
    InitCounter(
      new String(frame.c1.toArray),
      Long.decodeLong(frame.c2.get.toArray.toIterator, frame.c2.get.toArray.length)
    )
  }

  //======================================

  case class Incr(key: String) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = LongValue
  }

  implicit val incrGetKey: TypeKey[Incr] = TypeKey[Incr](12)

  implicit val incrGetWriter = writer[Incr] { value =>
    Untracked(
      incrGetKey,
      akka.util.ByteString(value.key),
      None
    )
  }

  implicit val incrGetReader = reader[Incr] { frame =>
    Incr(new String(frame.c1.toArray))
  }

  //======================================

  case class Decr(key: String) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = LongValue
  }

  implicit val decrGetKey: TypeKey[Decr] = TypeKey[Decr](13)

  implicit val decrGetWriter = writer[Decr] { value =>
    Untracked(
      decrGetKey,
      akka.util.ByteString(value.key),
      None
    )
  }

  implicit val decrGetReader = reader[Decr] { frame =>
    Decr(new String(frame.c1.toArray))
  }

  //======================================

  case class LongValue(value: Long)

  implicit val longValueKey = TypeKey[LongValue](14)

  implicit val longValueWriter = writer[LongValue] { value =>
    Untracked(
      longValueKey,
      ByteString(Long.encodeLong(value.value)),
      None
    )
  }

  implicit val longValueReader = reader[LongValue] { frame =>
    LongValue(Long.decodeLong(frame.c1.toArray.toIterator, frame.c1.toArray.length))
  }
}
