package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

import com.github.kennedyoliveira.hystrix.contrib.vertx.metricsstream.EventMetricsStreamHelper;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Observable;

/**
 * @author Kennedy Oliveira
 */
@RunWith(VertxUnitRunner.class)
public class HystrixDashboardVerticleTest {

  @Rule
  public RunTestOnContext runTestOnContext = new RunTestOnContext();

  // default port
  int serverPort = 7979;

  @Before
  public void setUp(TestContext testContext) throws Exception {
    EventMetricsStreamHelper.deployStandaloneMetricsStream(runTestOnContext.vertx(), testContext.asyncAssertSuccess());
    runTestOnContext.vertx().deployVerticle(HystrixDashboardVerticle.class.getName(), testContext.asyncAssertSuccess());
  }

  @Test
  public void testDashboard(TestContext testContext) throws Exception {
    final Async asyncRedirect = testContext.async();
    final HttpClient httpClient = runTestOnContext.vertx().createHttpClient();
    httpClient.get(serverPort, "localhost", "/hystrix-dashboard")
              .handler(response -> {
                testContext.assertEquals(301, response.statusCode()); // redirect due to missing trailing '/' in request
              })
              .exceptionHandler(testContext::fail)
              .endHandler(e -> asyncRedirect.complete())
              .end();

    final Async asyncMainPage = testContext.async();
    httpClient.get(serverPort, "localhost", "/hystrix-dashboard/")
              .handler(response -> testContext.assertEquals(200, response.statusCode()))
              .exceptionHandler(testContext::fail)
              .endHandler(e -> asyncMainPage.complete())
              .end();

    // generate some metrics
    new HystrixObservableCommand<String>(HystrixObservableCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("dummy"))) {
      @Override
      protected Observable<String> construct() {
        return Observable.just("dummy");
      }
    }.observe();

    final Async asyncProxying = testContext.async();
    final String url = "/hystrix-dashboard/proxy.stream?origin=http://localhost:8099/hystrix.stream";
    httpClient.get(serverPort, "localhost", url)
              .handler(response -> {
                testContext.assertEquals(200, response.statusCode());
                testContext.assertEquals("text/event-stream;charset=UTF-8", response.getHeader(HttpHeaders.CONTENT_TYPE));
                response.handler(buffer -> {
                  testContext.assertTrue(buffer.length() > 0);
                  testContext.assertTrue(buffer.toString().contains("data:")); // the first buffer will have a data
                });
              })
              .exceptionHandler(testContext::fail)
              .endHandler(e -> asyncProxying.complete())
              .end();
  }

  @After
  public void tearDown(TestContext testContext) throws Exception {
    runTestOnContext.vertx().close();
  }
}