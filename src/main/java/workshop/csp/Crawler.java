package workshop.csp;


// Main Exercise:
// Using Fibers and Channels create a crawler that will asynchronously crawl an Api (e.g. RemoteApi)
// For simplicity our Api is just an interface and a dummy RemoteApi implementation is provided.
// I recommend solving this exercise in a couple of steps.
//
// Step I:
// Fill in FiberApi so that FiberApiTest passes.
//
// Step II:
// You should be able to send one request to the requestsCh (e.g. 1).
// As a result a single body from Api should be registered in the answersCh receive port.
// Make sure that closing requestsCh causes the answersCh to receive the answer and closes answersCh, too!
// Write tests!
//
// Step III:
// Each body from Api might carry zero or more links. Combine the channels in such a way that
// the Crawler crawls the whole Api.
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
import co.paralleluniverse.strands.channels.ReceivePort;
import com.google.common.base.Function;

public class Crawler {

//    public final SendPort<Integer> requestsCh;
//    public final ReceivePort<List<Body>> answersCh;

    public ReceivePort<Body> run() {
        // Fill in.
        return null;
    }
}


class FiberApi extends FiberAsync<Body, RuntimeException> {

    private final Api api;
    private Integer parent;

    public FiberApi(Api api, Integer parent) {
        this.api = api;
        this.parent = parent;
    }

    @Override
    protected void requestAsync() {
        // Fill in.
    }

}


