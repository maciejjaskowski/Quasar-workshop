package workshop.csp;

import com.google.common.base.Function;
import org.assertj.core.util.Arrays;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Arrays.array;


public class RemoteApiTest {

    volatile Body answer = null;

    @Test
    public void returns() throws Exception {

        new RemoteApi(1).get(1, setAnswer);
        while (answer == null);
        assertThat(answer).isEqualTo(new Body("I am 1"));
    }

    @Test
    public void returnsChildren() throws Exception {
        new RemoteApi(2).get(1, setAnswer);
        while (answer == null);
        assertThat(answer).isEqualTo(new Body("I am 1", array(2, 3)));
    }

    Function<Body, Void> setAnswer = new Function<Body, Void>() {

        @Override
        public Void apply(Body body) {
            answer = body;
            return null;
        }
    };
}