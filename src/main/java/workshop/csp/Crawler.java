package workshop.csp;


// Main Exercise:
// Using Fibers and Channels create a crawler that will asynchronously crawl an Api

// For simplicity our Api is just an Asynchronous Graph (AsyncGraph).
// An implementation (AsyncLimitedBinaryTree) is provided. Examine the tests to make sure you understand how it works.


// I recommend solving this exercise in a couple of steps.

// Step I:
// Fill in FiberAsyncGraphAdaptor so that FiberAsyncGraphAdaptorTest passes.
//
// Step II:
// You should be able to send one request to the requestsCh (e.g. 1).
// As a result a single body from Api should be registered in the answersCh receive port.
// Make sure that closing requestsCh causes the answersCh to receive the answer and closes answersCh, too!
// Write tests!
//
// Step III:
// Each Node of AsyncGraph might be connected to zero or more neighbour Nodes. Combine the channels in such a way that
// the Crawler crawls the whole AsyncGraph.
// Make sure that the computation finishes.
// Write tests!
//
// Step IV:
// The whole crawling thing might take looong. Add a timeout capability to the Crawler.
// After a specified amount of time no more requests to Api shall be sent; all channels get closed; the answer so far is
// available in answersCh
//
// Step V:
// Some of the links are of more interest to us then others. Since crawling can time out, we would like to get them first.
// Use java.util.concurrent.PriorityBlockingQueue
// Make sure threads are not blocked but suspended.
// Write tests with an example comparator!
//
// Step VI:
// Since the FiberApi suspends the Fiber, only one Api request is being processed concurrently.
// Change your Crawler in such a way that up to N requests can be processed concurrently.


import co.paralleluniverse.fibers.FiberAsync;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.ReceivePort;

public class Crawler {

//    public final Channel<Integer> requestsCh;
//    public final Channel<List<Body>> answersCh;

    public ReceivePort<Node> run() {
        // Fill in.
        return null;
    }
}

class FiberAsyncGraphAdaptor {
    private final AsyncGraph asyncGraph;

    public FiberAsyncGraphAdaptor(AsyncGraph asyncGraph) {
        this.asyncGraph = asyncGraph;
    }

    public Node getNode(NodeId nodeId) throws InterruptedException, SuspendExecution {
        return new FiberAsync<Node, RuntimeException>() {
          @Override
          protected void requestAsync() {
              // Fill in.
          }
        }.run();
    }
}


