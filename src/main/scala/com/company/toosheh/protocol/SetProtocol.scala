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
      akka.util.ByteString(err.value),
      None
    )
  }

  implicit val errorReader = reader[Error] { frame =>
    Error(new String(frame.c1.toArray))
  }

  //======================================

  case class Value(value: String)

  implicit val valueKey = TypeKey[Value](2)

  implicit val valueWriter = writer[Value] { value =>
    Untracked(
      valueKey,
      akka.util.ByteString(value.value),
      None
    )
  }

  implicit val valueReader = reader[Value] { frame =>
    Value(new String(frame.c1.toArray))
  }

  //======================================

  case class Set(key: String, value: String) extends Func {
    override type Error = self.Error
    override type Result = Value
  }

  implicit val setKey: TypeKey[Set] = TypeKey[Set](3)

  implicit val setWriter = writer[Set] { value =>
    Untracked(
      setKey,
      akka.util.ByteString(value.key),
      None
    )
  }

  implicit val setReader = reader[Set] { frame =>
    Set(
      new String(frame.c1.toArray),
      new String(frame.c2.get.toArray)
    )
  }

  //======================================

  case class UnSet(key: String) extends Func {
    override type Error = self.Error
    override type Result = Value
  }

  implicit val unsetKey: TypeKey[UnSet] = TypeKey[UnSet](4)

  implicit val unsetWriter = writer[UnSet] { value =>
    Untracked(
      unsetKey,
      akka.util.ByteString(value.key),
      None
    )
  }

  implicit val unsetReader = reader[UnSet] { frame =>
    UnSet(new String(frame.c1.toArray))
  }

  //======================================

  case class Get(key: String) extends Func {
    override type Error = self.Error
    override type Result = Value
  }

  implicit val getKey: TypeKey[Get] = TypeKey[Get](5)

  implicit val getWriter = writer[Get] { value =>
    Untracked(
      getKey,
      akka.util.ByteString(value.key),
      None
    )
  }

  implicit val getReader = reader[Get] { frame =>
    Get(new String(frame.c1.toArray))
  }

}