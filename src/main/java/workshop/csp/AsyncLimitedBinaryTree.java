package workshop.csp;

import static org.assertj.core.util.Arrays.array;

public class AsyncLimitedBinaryTree implements AsyncGraph {

    private final int limit;

    public AsyncLimitedBinaryTree(int limit) {
        this.limit = limit;
    }

    public void getNode(final NodeId nodeId, final Callback<Node> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (nodeId.id > limit / 2) {
                        Thread.sleep(50);
                        callback.apply(new Node("I am node " + nodeId.id));
                    } else {
                        Thread.sleep(50);
                        callback.apply(
                                new Node("I am node " + nodeId.id,
                                        array(new NodeId(nodeId.id * 2), new NodeId(nodeId.id * 2 + 1))));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }
}
