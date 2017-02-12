package com.company.toosheh.protocol

import akka.util.ByteString
import com.bisphone.sarf._
import com.bisphone.util.ByteOrder

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object SetProtocol {
  self =>
  val order = ByteOrder.BigEndian
  implicit val javaValueForByteOrder = order.javaValue

  case class
  Tracked(override val dispatchKey: TypeKey[_],
          c1: ByteString,
          c2: Option[ByteString],
          override val trackingKey: Int)
    extends TrackedFrame {
    override val bytes: ByteString =
      ByteString.newBuilder
        .putInt(dispatchKey.typeKey)
        .putInt(trackingKey)
        .putInt(c1.length)
        .append(c1)
        .append(c2.getOrElse(ByteString()))
        .result()
  }

  case class
  Untracked(override val dispatchKey: TypeKey[_],
            bytes1: ByteString,
            bytes2: Option[ByteString])
    extends UntrackedFrame[Tracked]


  def writer = new FrameWriter[Tracked, Untracked] {
    def writeFrame(uf: Untracked, tk: Int): Tracked = {
      Tracked(uf.dispatchKey, uf.bytes1, uf.bytes2, tk)
    }
  }

  val reader = new FrameReader[Tracked] {
    def readFrame(bytes: ByteString): Tracked = {
      val iter = bytes.iterator
      val key = order.decodeInt(iter, 4)
      val tk = order.decodeInt(iter, 4)
      val contentLength1 = order.decodeInt(iter, 4)
      val c1 = ByteString(iter.getBytes(contentLength1))
      val c2 = ByteString(iter.toArray)
      Tracked(TypeKey(key), c1, Some(c2), tk)
    }
  }

  def writer[T](fn: T => Untracked) = new Writer[T, Tracked, Untracked] {
    override def write(t: T) = fn(t)
  }

  def reader[T](fn: Tracked => T) = new Reader[T, Tracked] {
    override def read(f: Tracked) = fn(f)
  }

  //======================================

  case class Error(value: String)

  implicit val errorKey = TypeKey[Error](1)

  implicit val errorWriter = writer[Error] { err =>
    Untracked(
      errorKey,
      ByteString(err.value),
      None
    )
  }

  implicit val errorReader = reader[Error] { frame =>
    Error(new String(frame.c1.toArray))
  }

  //======================================

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
    override type Error = self.Error
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
    override type Error = self.Error
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
    override type Error = self.Error
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
    override type Error = self.Error
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
    override type Error = self.Error
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

  case class Success(message: String)

  implicit val successKey = TypeKey[Success](9)

  implicit val successWriter = writer[Success] { value =>
    Untracked(
      successKey,
      ByteString(value.message),
      None
    )
  }

  implicit val successReader = reader[Success] { frame =>
    Success(new String(frame.c1.toArray))
  }

  //======================================

  case class BinaryUnSet(key: String) extends Func {
    override type Error = self.Error
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