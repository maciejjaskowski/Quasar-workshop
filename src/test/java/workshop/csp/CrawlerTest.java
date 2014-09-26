package workshop.csp;

import static org.assertj.core.api.Assertions.assertThat;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.ReceivePort;
import com.google.common.base.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.ExecutionException;


@RunWith(MockitoJUnitRunner.class)
public class CrawlerTest {

  private static final Body SIMPLE_BODY = new Body("blah");

  @Mock
  private Api api;

  @InjectMocks
  private Crawler crawler;

  @Test
  public void worksForSinglePage() throws Exception {
    mockApiFor(1, SIMPLE_BODY);
    crawler.getRequestCh().send(1);
    List<Body> result = runCrawlerGetResult();
    assertThat(result).containsExactly(SIMPLE_BODY);
  }

  private List<Body> runCrawlerGetResult() throws Exception {
    ReceivePort<List<Body>> resultChannel = crawler.run();
    List<Body> result = resultChannel.receive();
    return result;
  }

  private void mockApiFor(int page, Body body) {
    Mockito.doAnswer(invocationOnMock -> {
      Function<Body, Void> callback = (Function<Body, Void>) invocationOnMock.getArguments()[1];
      callback.apply(body);
      return null;
    }).when(api).get(Mockito.eq(page), Mockito.any(Function.class));
  }

}