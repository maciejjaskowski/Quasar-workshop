package workshop.csp;

import com.google.common.base.Function;

import java.util.Arrays;

public interface Api {
    public void get(int link, Function<Body, Void> callback);
}

class Body {
    public final Integer[] links;
    public final String value;

    @Override
    public String toString() {
        return "Body{" +
                "links=" + Arrays.toString(links) +
                ", value='" + value + '\'' +
                '}';
    }

    public Body(String value, Integer[] links) {
        this.links = links;
        this.value = value;
    }

    public Body(String value) {
        this.links = new Integer[]{};
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Body body = (Body) o;

        if (!Arrays.equals(links, body.links)) return false;
        if (value != null ? !value.equals(body.value) : body.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = links != null ? Arrays.hashCode(links) : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
