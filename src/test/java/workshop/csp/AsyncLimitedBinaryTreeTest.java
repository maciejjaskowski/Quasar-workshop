package workshop.csp;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;


public class AsyncLimitedBinaryTreeTest {

    volatile Node answer = null;

    @Test
    public void returns() throws Exception {

        new AsyncLimitedBinaryTree(1).getNode(new NodeId(1), setAnswer);
        while (answer == null);
        assertThat(answer).isEqualTo(new Node("I am node 1"));
    }

    @Test
    public void returnsChildren() throws Exception {
        new AsyncLimitedBinaryTree(2).getNode(new NodeId(1), setAnswer);
        while (answer == null);
        assertThat(answer).isEqualTo(new Node("I am node 1", array(new NodeId(2), new NodeId(3))));
    }

    Callback<Node> setAnswer = new Callback<Node>() {

        @Override
        public Void apply(Node node) {
            answer = node;
            return null;
        }
    };
}