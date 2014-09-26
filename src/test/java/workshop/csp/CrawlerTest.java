package workshop.csp;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;

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


@RunWith(MockitoJUnitRunner.class)
public class CrawlerTest {

  private static final Body SIMPLE_BODY1 = new Body("blah");
  private static final Body SIMPLE_BODY2 = new Body("trach");
  private static final Body SIMPLE_BODY3 = new Body("stach");

  @Mock
  private Api api;

  private Crawler crawler;

  @Before
  public void setUp() throws Exception {
    crawler = new Crawler(api);
  }

  @Test
  public void worksForSinglePage() throws Exception {
    mockApiFor(1, SIMPLE_BODY1);
    crawler.getRequestCh().send(1);
    crawler.getRequestCh().close();
    List<Body> answer = runCrawlerGetAnswer();
    assertThat(answer).containsExactly(SIMPLE_BODY1);
  }

  @Test
  public void worksForMultipleSinglePageRequests() throws Exception {
    mockApiFor(1, SIMPLE_BODY1);
    mockApiFor(2, SIMPLE_BODY2);
    mockApiFor(3, SIMPLE_BODY3);
    crawler.getRequestCh().send(1);
    crawler.getRequestCh().send(2);
    crawler.getRequestCh().send(3);
    crawler.getRequestCh().close();
    List<List<Body>> answerList = runCrawlerGetAnswers(3);
    assertThat(answerList.get(0)).containsExactly(SIMPLE_BODY1);
    assertThat(answerList.get(1)).containsExactly(SIMPLE_BODY2);
    assertThat(answerList.get(2)).containsExactly(SIMPLE_BODY3);
  }

  @Test
  public void worksForPageGraphWithCycle() throws Exception {
    Body pageA = new Body("a", 2);
    Body pageB = new Body("b", 1);
    mockApiFor(1, pageA);
    mockApiFor(2, pageB);
    crawler.getRequestCh().send(1);
    crawler.getRequestCh().close();
    List<Body> answer = runCrawlerGetAnswer();
    assertThat(answer).hasSize(2);
    assertThat(answer).contains(pageA, pageB);
  }

  @Test
  public void worksForSomePageGraph() throws Exception {
    Body pageA = new Body("a", 2);
    Body pageB = new Body("b", 3, 4);
    Body pageC = new Body("c");
    Body pageD = new Body("d");
    Body pageUnavailable = new Body("uanav");
    mockApiFor(1, pageA);
    mockApiFor(2, pageB);
    mockApiFor(3, pageC);
    mockApiFor(4, pageD);
    mockApiFor(5, pageUnavailable);
    crawler.getRequestCh().send(1);
    crawler.getRequestCh().close();
    List<Body> answer = runCrawlerGetAnswer();
    assertThat(answer).hasSize(4);
    assertThat(answer).contains(pageA, pageB, pageC, pageD);
  }

  @Test
  public void crawlsOnlySomePagesOnTimeout() throws Exception {
    initCrawlerWithTimeout(100);
    Body pageA = new Body("a", 2);
    Body pageB = new Body("b", 3);
    Body pageC = new Body("c");
    mockApiFor(1, pageA, 50);
    mockApiFor(2, pageB, 30);
    mockApiFor(3, pageC, 30);
    crawler.getRequestCh().send(1);
    crawler.getRequestCh().close();
    List<Body> answer = runCrawlerGetAnswer();
    assertThat(answer).hasSize(2);
    // page C should not get into result due to timeout
    assertThat(answer).contains(pageA, pageB);
  }

  private void initCrawlerWithTimeout(int timeout) {
    crawler = new Crawler(api, timeout);
  }

  private List<Body> runCrawlerGetAnswer() throws Exception {
    return runCrawlerGetAnswers(1).get(0);
  }

  private List<List<Body>> runCrawlerGetAnswers(int answersCount) throws Exception {
    ReceivePort<List<Body>> answerChannel = crawler.run();
    List<List<Body>> answerList = newArrayList();
    for (int i = 0; i < answersCount; ++i) {
      answerList.add(answerChannel.receive());
    }
    assertThat(answerChannel.isClosed()).isTrue();
    return answerList;
  }

  private void mockApiFor(int page, Body body) {
    mockApiFor(page, body, 0);
  }

  private void mockApiFor(int page, Body body, long sleepTime) {
    doAnswer(invocationOnMock -> {
      Function<Body, Void> callback = (Function<Body, Void>) invocationOnMock.getArguments()[1];
      if (sleepTime > 0) {
        Thread.sleep(sleepTime);
      }
      callback.apply(body);
      return null;
    }).when(api).get(Mockito.eq(page), Mockito.any(Function.class));
  }

}