package com.company.toosheh.protocol

import akka.util.ByteString
import com.bisphone.sarf._
import com.bisphone.util.LongCodec

object Long extends LongCodec.BigEndianDecoder with LongCodec.BigEndianEncoder


/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object SetProtocol {

  import ProtocolUtils._

  case class StringValue(value: String)

  implicit val valueKey = TypeKey[StringValue](2)

  implicit val valueWriter = writer[StringValue] { value =>
    Untracked(
      valueKey,
      ByteString(value.value),
      None
    )
  }

  implicit val valueReader = reader[StringValue] { frame =>
    StringValue(new String(frame.c1.toArray))
  }

  //======================================

  case class BinaryValue(value: Array[Byte])

  implicit val binaryValueKey = TypeKey[BinaryValue](3)

  implicit val binaryValueWriter = writer[BinaryValue] { value =>
    Untracked(
      binaryValueKey,
      ByteString(value.value),
      None
    )
  }

  implicit val binaryValueReader = reader[BinaryValue] { frame =>
    BinaryValue(frame.c1.toArray)
  }

  //======================================

  case class StringSet(key: String, value: String) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = Success
  }

  implicit val stringSetKey: TypeKey[StringSet] = TypeKey[StringSet](4)

  implicit val stringSetWriter = writer[StringSet] { value =>
    Untracked(
      stringSetKey,
      ByteString(value.key),
      Some(ByteString(value.value))
    )
  }

  implicit val stringSetReader = reader[StringSet] { frame =>
    StringSet(
      new String(frame.c1.toArray),
      new String(frame.c2.get.toArray)
    )
  }

  //======================================

  case class StringUnSet(key: String) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = Success
  }

  implicit val stringUnsetKey: TypeKey[StringUnSet] = TypeKey[StringUnSet](5)

  implicit val stringUnsetWriter = writer[StringUnSet] { value =>
    Untracked(
      stringUnsetKey,
      akka.util.ByteString(value.key),
      None
    )
  }

  implicit val stringUnsetReader = reader[StringUnSet] { frame =>
    StringUnSet(new String(frame.c1.toArray))
  }

  //======================================

  case class StringGet(key: String) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = StringValue
  }

  implicit val stringGetKey: TypeKey[StringGet] = TypeKey[StringGet](6)

  implicit val stringGetWriter = writer[StringGet] { value =>
    Untracked(
      stringGetKey,
      akka.util.ByteString(value.key),
      None
    )
  }

  implicit val stringGetReader = reader[StringGet] { frame =>
    StringGet(new String(frame.c1.toArray))
  }

  //======================================

  case class BinaryGet(key: String) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = BinaryValue
  }

  implicit val binaryGetKey: TypeKey[BinaryGet] = TypeKey[BinaryGet](7)

  implicit val binaryGetWriter = writer[BinaryGet] { value =>
    Untracked(
      binaryGetKey,
      ByteString(value.key),
      None
    )
  }

  implicit val binaryGetReader = reader[BinaryGet] { frame =>
    BinaryGet(frame.c1.utf8String)
  }

  //======================================

  case class BinarySet(key: String, value: Array[Byte]) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = Success
  }

  implicit val binarySetKey: TypeKey[BinarySet] = TypeKey[BinarySet](8)

  implicit val binarySetWriter = writer[BinarySet] { value =>
    Untracked(
      binarySetKey,
      ByteString(value.key),
      Some(ByteString(value.value))
    )
  }

  implicit val binarySetReader = reader[BinarySet] { frame =>
    BinarySet(
      new String(frame.c1.toArray),
      frame.c2.get.toArray
    )
  }

  //======================================

  case class BinaryUnSet(key: String) extends Func {
    override type Error = ProtocolUtils.Error
    override type Result = Success
  }

  implicit val binaryUnsetKey: TypeKey[BinaryUnSet] = TypeKey[BinaryUnSet](10)

  implicit val binaryUnsetWriter = writer[BinaryUnSet] { value =>
    Untracked(
      binaryUnsetKey,
      ByteString(value.key),
      None
    )
  }

  implicit val binaryUnsetReader = reader[BinaryUnSet] { frame =>
    BinaryUnSet(new String(frame.c1.toArray))
  }
}