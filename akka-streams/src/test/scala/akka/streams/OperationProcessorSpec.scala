package akka.streams

import org.scalatest.{ BeforeAndAfterAll, WordSpec, ShouldMatchers }
import rx.async.tck._
import akka.streams.testkit.TestKit
import akka.testkit.TestKitBase
import akka.actor.ActorSystem
import rx.async.api.Processor

class OperationProcessorSpec extends WordSpec with TestKitBase with ShouldMatchers with BeforeAndAfterAll {
  implicit lazy val system = ActorSystem()
  implicit lazy val settings: ProcessorSettings = ProcessorSettings(system)

  "An OperationProcessor" should {
    "work uninitialized without publisher" when {
      "subscriber requests elements" in pending
      "subscriber cancels subscription and resubscribes" in pending
    }
    "work uninitialized without subscriber" when {
      "publisher completes" in pending
      "publisher errs out" in pending
    }
    "work initialized" when {
      "subscriber requests elements" in new InitializedChainSetup(Identity[String]()) {
        downstreamSubscription.requestMore(1)
        upstream.probe.expectMsg(RequestMore(upstreamSubscription, 1))
      }
      "publisher sends element" in new InitializedChainSetup(Identity[String]()) {
        downstreamSubscription.requestMore(1)
        upstream.probe.expectMsg(RequestMore(upstreamSubscription, 1))
        upstreamSubscription.sendNext("test")
        downstream.probe.expectMsg(OnNext("test"))
      }
      "publisher sends elements and then completes" in new InitializedChainSetup(Identity[String]()) {
        downstreamSubscription.requestMore(1)
        upstream.probe.expectMsg(RequestMore(upstreamSubscription, 1))
        upstreamSubscription.sendNext("test")
        upstreamSubscription.sendComplete()
        downstream.probe.expectMsg(OnNext("test"))
        downstream.probe.expectMsg(OnComplete)
      }
      "publisher immediately completes" in pending
      "publisher immediately fails" in pending
      "operation publishes Producer" in pending
      "operation consumes Producer" in pending
      "complex operation" in pending
    }
    "work with multiple subscribers" when {
      "one subscribes while elements were requested before" in pending
    }
    "work in special situations" when {
      "single subscriber cancels subscription while receiving data" in pending
    }
    "work after initial upstream was completed" when {}
    "work when subscribed to multiple publishers" when {
      "really?" in pending
    }
  }

  override protected def afterAll(): Unit = system.shutdown()

  def processor[I, O](operation: Operation[I, O]): Processor[I, O] =
    OperationProcessor(operation, ProcessorSettings(system))

  class InitializedChainSetup[I, O](operation: Operation[I, O]) {
    val upstream = TestKit.producerProbe[I]()
    val downstream = TestKit.consumerProbe[O]()

    import DSL._

    val processed = upstream.andThen(operation).consume()
    val upstreamSubscription = upstream.expectSubscription()
    processed.link(downstream)
    val downstreamSubscription = downstream.expectSubscription()
  }
}

