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


import static co.paralleluniverse.strands.channels.Channels.newChannel;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.empty;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberAsync;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.ReceivePort;
import co.paralleluniverse.strands.channels.SendPort;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Crawler {

  private final Channel<Integer> requestsCh = newChannel(-1);
  private final Channel<List<Body>> answersCh = newChannel(-1);
  private final Api remoteApi;
  private final Optional<Long> timeout;

  public Crawler(Api remoteApi) {
    this.remoteApi = remoteApi;
    this.timeout = empty();
  }

  public Crawler(Api remoteApi, long timeout) {
    this.remoteApi = remoteApi;
    this.timeout = Optional.of(timeout);
  }

  public SendPort<Integer> getRequestCh() {
    return requestsCh;
  }

  public ReceivePort<List<Body>> run() throws SuspendExecution, InterruptedException, ExecutionException {
    Fiber<Void> fiber = new Fiber<Void>((SuspendableRunnable) () -> {
      while (!requestsCh.isClosed()) {
        Integer parent = requestsCh.receive();
        handleRequest(parent);
      }
      answersCh.close();
    }).start();
    fiber.join();
    return answersCh;
  }

  private void handleRequest(Integer parent) throws SuspendExecution, InterruptedException {
    Channel<Integer> internalChannel = newChannel(-1);
    internalChannel.send(parent);
    Map<Integer, Body> visitedPages = Maps.newHashMap();
    long start = System.currentTimeMillis();
    while (true) {
      Integer pageLink = internalChannel.tryReceive();
      if (pageLink == null) {
        break;
      }

      Body body = getBody(start, pageLink);
      if (body == null) {
        // timeout
        break;
      }

      visitedPages.put(pageLink, body);
      for (Integer link : body.links) {
        if (!visitedPages.containsKey(link)) {
          internalChannel.send(link);
        }
      }
    }
    internalChannel.close();
    answersCh.send(newArrayList(visitedPages.values()));
  }

  private Body getBody(long crawlingStart, Integer pageLink) throws SuspendExecution, InterruptedException {
    Body body;
    if (timeout.isPresent()) {
      long now = System.currentTimeMillis();
      long remainingTimeout = timeout.get() - now + crawlingStart;
      if (remainingTimeout <= 0) {
        return null;
      }
      try {
        body = new FiberApi(remoteApi, pageLink).run(remainingTimeout, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        return null;
      }
    } else {
      body = new FiberApi(remoteApi, pageLink).run();
    }
    return body;
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
    api.get(parent, input -> {
      asyncCompleted(input);
      return null;
    });
  }

}


