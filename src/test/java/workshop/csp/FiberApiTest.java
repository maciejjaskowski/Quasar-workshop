package workshop.csp;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import org.assertj.core.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;


public class FiberApiTest {
    
    @Test @Ignore
    public void wraps_Api() throws Exception {
        final Channel<Body> ch = Channels.newChannel(4);
        new Fiber<Integer[]>(new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                ch.send(new FiberApi(new RemoteApi(5), 1).run());
            }
        }).start();

        assertThat(ch.receive()).isEqualTo(new Body("I am 1", array(2, 3)));
    }

    @Test @Ignore
    public void suspends_fiber() throws Exception {
        final Channel<Long> ch = Channels.newChannel(4);
        new Fiber<Long>(new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                ch.send(System.currentTimeMillis());
                new FiberApi(new RemoteApi(5), 1).run();
                ch.send(System.currentTimeMillis());
            }
        }).start();

        Long before = ch.receive();
        Long after = ch.receive();
        assertThat(after - before).isBetween(30L, 70L);
    }
}