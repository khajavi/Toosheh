import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.util.ByteString
import com.bisphone.sarf._
import com.bisphone.sarf.implv1.StatCollector.Ref
import com.bisphone.sarf.implv1.util.{StreamConfig, TCPConfigForClient, TCPConfigForServer}
import com.bisphone.sarf.implv1.{Service, StatCollector, TCPClient, TCPServer}
import com.bisphone.testkit.BaseSuite
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
class DBTestSuit
  extends TestKit(ActorSystem())
    with BaseSuite {

  import com.company.toosheh.operations.SetOperations._
  import com.company.toosheh.protocol.SetProtocol._

  implicit val ec = ExecutionContext.Implicits.global

  val tcpServer = TCPConfigForServer(
    host = "localhost",
    port = 2424,
    backlog = 100
  )

  val tcpClient = TCPConfigForClient(
    host = "localhost",
    port = 2424,
    connectingTimeout = 20 seconds
  )

  val stream = StreamConfig(
    maxSliceSize = 2000,
    byteOrder = order,
    concurrencyPerConnection = 10
  )

  val stat: Ref = StatCollector(
    name = "server-stat",
    config = StatCollector.Config(1 minute, 3),
    logger = LoggerFactory.getLogger("server-stat")
  )(ctx = system)

  val service: Service[Tracked, Untracked] = {

    val failureHandler = new FailureHandler {
      def apply(cause: Throwable, bytes: ByteString): Future[IOCommand] = {
        Future successful IOCommand.Close
      }
    }

    new Service.Builder[Tracked, Untracked](
      executor = system.dispatcher,
      failureHandler = failureHandler,
      frameReader = reader,
      frameWriter = writer,
      statCollector = Some(stat)
    )
      .serveFunc(sset)
      .serveFunc(sget)
      .serveFunc(sunset)
      .serveFunc(bset)
      .serveFunc(bget)
      .serveFunc(bunset)
      .serveFunc(initc)
      .serveFunc(incr)
      .serveFunc(decr)
      .result
      .get
  }

  val res = for {
    server <- TCPServer(
      name = "db-server",
      tcpConfig = tcpServer,
      streamConfig = stream,
      debug = true)(service)
    client <- TCPClient[Tracked, Untracked](
      name = "client",
      tcp = tcpClient,
      stream = stream,
      writer = writer,
      reader = reader)
  } yield (server, client)


  val (server, client) = try Await.result(res, 1 minute) catch {
    case NonFatal(cause) => info(s"Error: $cause")
      cause.printStackTrace()
      fail("Can't prepare resources")
  }

  // ==============================================

  "StringSet" must "set/get/unset string key/value" in {
    val (key, value) = ("foo", "bar")

    client(StringSet(key, value)) onRight {
      _.message shouldEqual "OK"
    }

    client(StringGet(key)) onRight {
      _ shouldEqual StringValue(value)
    }

    client(StringUnSet(key)) onRight {
      _ shouldEqual Success("OK")
    }

    client(StringGet(key)) onLeft {
      _ shouldEqual Error("Nil")
    }

  }

  "BinarySet" must "set/get/unset binary key/value" in {
    val (key, value) = ("foo", Array[Byte](98, 97, 114))

    client(BinarySet(key, value)) onRight {
      _ shouldEqual Success("OK")
    }

    client(BinaryGet(key)) onRight {
      _.value.toList shouldEqual value.toList
    }

    client(BinaryUnSet(key)) onRight {
      _ shouldEqual Success("OK")
    }

    client(BinaryGet(key)) onLeft {
      _ shouldEqual Error("Nil")
    }
  }


  "Counter" must "init/incr/decr values" in {

    val (key, value) = ("year", 1989)
    client(InitCounter(key, value)) onRight {
      _.value shouldEqual value
    }

    client(Incr(key)) onRight {
      _.value shouldEqual value + 1
    }

    client(Incr("abc")) onRight {
      _.value shouldEqual 1
    }

    client(Decr(key)) onRight {
      _.value shouldEqual value
    }

    client(Decr("cba")) onRight {
      _.value shouldEqual -1
    }
  }
}
