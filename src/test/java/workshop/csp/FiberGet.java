package workshop.csp;

import co.paralleluniverse.fibers.FiberAsync;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.client.extra.ListenableFutureAdapter;

import java.io.IOException;

class FiberGet extends FiberAsync<Response, IOException> implements FutureCallback<Response> {

        @Override
        protected void requestAsync() {
            try {
                AsyncHttpClient c = new AsyncHttpClient();
                ListenableFuture<Response> f = c.prepareGet("http://www.gazeta.pl").execute();
                Futures.addCallback(ListenableFutureAdapter.asGuavaFuture(f), this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onSuccess(Response response) {
            asyncCompleted(response);
        }

        @Override
        public void onFailure(Throwable t) {
            asyncFailed(t);
        }
    }