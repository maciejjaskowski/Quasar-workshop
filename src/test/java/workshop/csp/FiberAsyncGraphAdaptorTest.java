package workshop.csp;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;


public class FiberAsyncGraphAdaptorTest {
    
    @Test @Ignore
    public void fiberAsyncGraphAdaptor_invokes_underlying_AsyncGraph_implementation() throws Exception {
        final Channel<Node> ch = Channels.newChannel(4);
        final AsyncGraph anAsyncGraph = new AsyncLimitedBinaryTree(5);

        final FiberAsyncGraphAdaptor fiberAsyncGraphAdaptor = new FiberAsyncGraphAdaptor(anAsyncGraph);

        new Fiber<Integer[]>(new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {

                Node node = fiberAsyncGraphAdaptor.getNode(new NodeId(1));
                ch.send(node);
            }
        }).start();

        assertThat(ch.receive()).isEqualTo(new Node("I am node 1", array(new NodeId(2), new NodeId(3))));
    }

    @Test @Ignore
    public void suspends_fiber() throws Exception {
        final Channel<Long> ch = Channels.newChannel(4);
        new Fiber<Long>(new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                ch.send(System.currentTimeMillis());
                new FiberAsyncGraphAdaptor(new AsyncLimitedBinaryTree(5)).getNode(new NodeId(1));
                ch.send(System.currentTimeMillis());
            }
        }).start();

        Long before = ch.receive();
        Long after = ch.receive();
        assertThat(after - before).isBetween(30L, 70L);
    }
}