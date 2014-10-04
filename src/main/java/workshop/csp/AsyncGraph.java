package workshop.csp;

import com.google.common.base.Function;

import java.util.Arrays;

public interface AsyncGraph {
    public void getNode(NodeId nodeId, Callback<Node> callback);
}

interface Callback<T> extends Function<T, Void> {}

class Node {
    public final NodeId[] neighbours;
    public final String value;

    public Node(String value, NodeId[] neighbours) {
        this.value = value;
        this.neighbours = neighbours;
    }

    public Node(String value) {
        this(value, new NodeId[]{});
    }

    @Override
    public String toString() {
        return "Node{" +
                "neighbours=" + Arrays.toString(neighbours) +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        if (!Arrays.equals(neighbours, node.neighbours)) return false;
        if (value != null ? !value.equals(node.value) : node.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = neighbours != null ? Arrays.hashCode(neighbours) : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}

class NodeId {
    public final int id;

    public NodeId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeId)) return false;

        NodeId nodeId1 = (NodeId) o;

        if (id != nodeId1.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Id{" +
                "id=" + id +
                '}';
    }
}