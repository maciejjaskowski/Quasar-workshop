package workshop.csp;

import static co.paralleluniverse.strands.channels.Channels.OverflowPolicy;
import static co.paralleluniverse.strands.channels.Channels.filter;
import static co.paralleluniverse.strands.channels.Channels.map;
import static co.paralleluniverse.strands.channels.Channels.newChannel;
import static org.assertj.core.api.Assertions.assertThat;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberForkJoinScheduler;
import co.paralleluniverse.fibers.FiberScheduler;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.ReceivePort;
import co.paralleluniverse.strands.channels.SelectAction;
import co.paralleluniverse.strands.channels.Selector;
import com.ning.http.client.Response;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class Tutorial {

  // Have you setup the -javagent flag set up properly as VM arguments?
  // In my case I had to write:
  // -javaagent:/Users/maciej/.m2/repository/co/paralleluniverse/quasar-core/0.6.0/quasar-core-0.6.0.jar
  //
  // Now you can run these tests.

  // Please, start the tutorial by inspecting the tests below, playing around with them and filling in the exercises.


  // Documentation: http://docs.paralleluniverse.co/quasar/


  @Test
  public void channel_is_like_a_fifo_queue() throws Exception {

    // Channel is like a FIFO queue: one can send a message to it using .send() method
    // or read from it using .receive() method.

    int bufferSize = -1; //-1 means: unbuferred
    final Channel<String> ch = newChannel(bufferSize);

    ch.send("A message.");
    assertThat(ch.receive()).isEqualTo("A message.");

  }

  @Test
  public void exercise1_empty_channel() throws Exception {
    // EXERCISE 1: What will happen if one tries to .receive() message before anything is ready in channel?
    int bufferSize = -1; //-1 means: unbuferred
    final Channel<String> ch = newChannel(bufferSize);
    //ch.receive();
    // thread blocks
  }

  @Test
  public void exercise1a_unbuffered_channel() throws Exception {
    // Verify what is going to happen if one creates an unbuffered channel and puts there an abundance of messages?
  }


  @Test
  public void one_can_apply_map_to_channel() throws Exception {
    // One can use classics from functional programming world: map/flatMap/filter:
    final Channel<Integer> ch = newChannel(-1);

    final ReceivePort<String> transformedChannel = map(ch, Object::toString);

    ch.send(42);
    assertThat(transformedChannel.receive()).isEqualTo("42");

  }

  @Test
  public void exercise2_filtering() throws Exception {
    final Channel<Integer> chInt = newChannel(-1);

    // Un-@Ignore this test and fix the following line so that the test passes
    final ReceivePort<Integer> chFiltered = filter(chInt, integer -> integer % 2 == 0);

    chInt.send(0);
    chInt.send(1);
    chInt.send(2);
    chInt.send(3);
    assertThat(chFiltered.receive()).isEqualTo(0);
    assertThat(chFiltered.receive()).isEqualTo(2);
  }

  @Test
  @Ignore
  public void exercise_2a() throws Exception {
    // modify the implementation of SuspendableAction2 so that a sum of values sent to chInt is sent to sendPort once
    // chInt gets closed.
    // chSum should be closed immediately afterwards

    final Channel<Integer> chInt = newChannel(-1);
    final Channel<Integer> chSum = newChannel(1); //

    Channels.fiberTransform(chInt, chSum, (receivePort, sendPort) -> {
      // fill in
    });

    chInt.send(1);
    chInt.send(1);
    chInt.send(1);
    chInt.send(1);
    chInt.close(); //Note I am closing channel here!
    assertThat(chSum.receive()).isEqualTo(4);
    assertThat(chSum.receive()).isEqualTo(null);
  }

  @Test
  public void receive_from_multiple_channels() throws Exception {

    // Sometimes you need to receive from multiple channels at the same time.

    final Channel<String> ch1 = newChannel(-1);
    final Channel<String> ch2 = newChannel(-1);
    final Channel<String> chResult = newChannel(-1);

    new Thread(() -> {
      try {
        while (true) {
          SelectAction<String> selected = Selector.select(Selector.receive(ch1), Selector.receive(ch2));

          chResult.send(selected.message());
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    ch1.send("Message to ch1");
    ch1.send("Message to ch2");

    assertThat(Arrays.asList(chResult.receive(), chResult.receive()))
            .containsOnly("Message to ch1", "Message to ch2");

  }

  @Test
  public void exercise3() throws Exception {
    // Change the body of Runnable in Thread so, that the thread.join() doesn't block the the test
    // make use of the poisonPillCh!
    //
    // Alternatively, instead of poisonPillCh.send(1L) one could ch1.close() the channel.
    // What's the difference between those two solutions?

    final Channel<Long> ch1 = newChannel(-1);
    final Channel<Long> poisonPillCh = newChannel(-1);
    final Channel<Long> chResult = newChannel(-1);

    Thread thread = new Thread(() -> {
      try {
        while (true) {
          chResult.send(ch1.receive() + 2);
          Thread.sleep(100);


        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    thread.start();

    ch1.send(1L);
    ch1.send(2L);
    poisonPillCh.send(1L);

    thread.join();

  }

  @Test
  public void a_talk_between_threads() throws Exception {

    int bufferSize = 1;
    final Channel<Long> ch1 = newChannel(bufferSize, OverflowPolicy.BLOCK);
    final Channel<Long> ch2 = newChannel(bufferSize, OverflowPolicy.BLOCK);
    final Channel<Long> ch3 = newChannel(bufferSize, OverflowPolicy.BLOCK);
    final Channel<String> chThread = newChannel(-1);

    new Thread(() -> {
      try {
        chThread.send(Thread.currentThread().getName());
        ch1.send(1L);

        ch1.send(ch2.receive() + 1);

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    new Thread(() -> {
      try {
        chThread.send(Thread.currentThread().getName());
        ch2.send(ch1.receive() * 2);

        ch3.send(ch1.receive() * 2);

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    assertThat(ch3.receive()).isEqualTo(6);

    // As expected: two threads were needed to run this code:
    assertThat(chThread.receive()).isNotEqualTo(chThread.receive());

  }

  @Test
  public void exercise3_unbuffered_channel() throws Exception {
    // Verify what is going to happen if one creates an unbuffered channel and puts there an abundance of messages?
    Channel<Integer> unbufferedChannel = newChannel(-1);
    for (int i = 0; i < 10000000; ++i) {
      unbufferedChannel.send(i);
    }
  }

  @Test
  public void fibers_instead_of_threads() throws Exception {
    // The above code is cool, as it's easy to understand what is going on.
    // However, each Thread causes memory and CPU overhead that we would like to evade.
    //
    // To that end Quasar provides Fibers.
    // You write a Fiber in exactly the same way as a Thread. There is, however, no particular Thread
    // associated to Fiber. Moreover, on ".send()" or ".receive()" operation the Thread will not hang.
    // In particular the following code uses a single thread.

    String schedulerName = "Scheduler name.";
    FiberScheduler scheduler = new FiberForkJoinScheduler(schedulerName, 1);

    int bufferSize = 1;
    final Channel<Long> ch1 = newChannel(bufferSize, OverflowPolicy.BLOCK);
    final Channel<Long> ch2 = newChannel(bufferSize, OverflowPolicy.BLOCK);
    final Channel<Long> ch3 = newChannel(bufferSize, OverflowPolicy.BLOCK);
    final Channel<String> chThread = newChannel(-1);

    Fiber fib1 = new Fiber(scheduler, () -> {
      try {
        chThread.send(Thread.currentThread().getName());
        ch1.send(1L);

        ch1.send(ch2.receive() + 1);

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    Fiber fib2 = new Fiber(scheduler, () -> {
      try {
        chThread.send(Thread.currentThread().getName());
        ch2.send(ch1.receive() * 2);

        ch3.send(ch1.receive() * 2);

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    assertThat(ch3.receive()).isEqualTo(6);

    // Note: a single thread was enough to run this code!
    assertThat(chThread.receive()).isEqualTo(chThread.receive());

    fib1.join();
    fib2.join();
  }

  @Test
  public void exercise4_testing_fibers() throws Exception {
    // Since a Fiber is just something that communicates with the outside world using Channels
    // testing a Fiber in isolation is very simple.

    // Go ahead and write assertion proving that fib1 receives from either ch1 or ch2 and spits the message into chOut.

    final Channel<Long> ch1 = newChannel(1);
    final Channel<Long> ch2 = newChannel(1);
    final Channel<Long> chOut = newChannel(1);

    Fiber fib1 = new Fiber(() -> {
      try {
        ReceivePort<Long> any = Channels.group(ch1, ch2);

        chOut.send(any.receive());

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).start();

    ch1.send(10l);
    ch2.send(20l);
    Long received = chOut.receive();
    fib1.join();
    assertThat(received == 10l || received == 20l).isTrue();
  }

  @Test
  public void integration_with_callbacks_or_futures() throws Exception {

    // Thankfully you can also integrate with libraries exposing asynchronous API
    // but return classic Java Futures or simply let you setup a callback.

    final Channel<Long> ch = newChannel(2);

    Fiber<Response> fiber = new Fiber<Response>() {
      @Override
      protected Response run() throws SuspendExecution, java.lang.InterruptedException {
        try {
          Fiber.sleep(100);
          ch.send(System.currentTimeMillis());

          // Note that this line suspends the execution (see assertion below)
          Response f = new FiberGet().run();

          ch.send(System.currentTimeMillis());
          return f;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
    fiber.start();

    long before = ch.receive();
    long after = ch.receive();

    assertThat(after - before).isGreaterThanOrEqualTo(100);
    System.out.println(fiber.get().getResponseBody());
  }


}
