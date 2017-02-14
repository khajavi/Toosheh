package com.company.toosheh.protocol

import akka.util.ByteString
import com.bisphone.sarf._
import com.bisphone.util.ByteOrder

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
object ProtocolUtils {
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
}
