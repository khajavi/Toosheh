import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.util.ByteString
import com.bisphone.sarf._
import com.bisphone.sarf.implv1.StatCollector.Ref
import com.bisphone.sarf.implv1.util.{StreamConfig, TCPConfigForClient, TCPConfigForServer}
import com.bisphone.sarf.implv1.{Service, StatCollector, TCPClient, TCPServer}
import com.bisphone.testkit.BaseSuite
import com.company.toosheh.operations.CounterOperations._
import com.company.toosheh.protocol.CounterProtocol._
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * @author Milad Khajavi <khajavi@gmail.com>
  */
class CounterTestSuit
  extends TestKit(ActorSystem())
    with BaseSuite {
  implicit val ec = ExecutionContext.Implicits.global

  import com.company.toosheh.protocol.ProtocolUtils._

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
