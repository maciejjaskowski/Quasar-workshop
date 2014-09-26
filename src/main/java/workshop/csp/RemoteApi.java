package workshop.csp;

import com.google.common.base.Function;

import static org.assertj.core.util.Arrays.array;

public class RemoteApi implements Api {

    private final int size;

    public RemoteApi(int size) {
        this.size = size;
    }

    public void get(final int link, final Function<Body, Void> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (link > size / 2) {
                        Thread.sleep(50);
                        callback.apply(new Body("I am " + link));
                    } else {
                        Thread.sleep(50);
                        callback.apply(new Body("I am " + link, array(link * 2, link * 2 + 1)));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }
}
