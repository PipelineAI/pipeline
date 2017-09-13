package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.runner.RunWith;

/**
 * @author Kennedy Oliveira
 */
@RunWith(VertxUnitRunner.class)
public class HystrixDashboardConfigurationTest {

  @SuppressWarnings({"PMD.AvoidUsingHardCodedIP"})
  private final static String SERVER_BIND_ADDRESS = "127.0.0.1";
  private final static int SERVER_PORT = 9999;
  private final static String SERVER_USE_COMPRESSION = "true";

  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  @Rule
  public RunTestOnContext runTestOnContext = new RunTestOnContext();

  @Before
  public void setUp(TestContext testContext) throws Exception {
    System.setProperty(Configuration.SERVER_PORT, String.valueOf(SERVER_PORT));
    System.setProperty(Configuration.DISABLE_COMPRESSION, String.valueOf(!Boolean.valueOf(SERVER_USE_COMPRESSION)));
    System.setProperty(Configuration.BIND_ADDRESS, SERVER_BIND_ADDRESS);

    runTestOnContext.vertx().deployVerticle(new HystrixDashboardVerticle(), testContext.asyncAssertSuccess());
  }

  @Test
  public void testConfigurationOptions(TestContext testContext) throws Exception {
    final HttpClientOptions options = new HttpClientOptions().setTryUseCompression(false);

    final HttpClient httpClient = runTestOnContext.vertx().createHttpClient(options);

    final Async asyncOp = testContext.async();

    // issue a request on the custom server bind address and port, testing for compression
    httpClient.get(SERVER_PORT, SERVER_BIND_ADDRESS, "/hystrix-dashboard/")
              .setChunked(false)
              .putHeader(HttpHeaders.ACCEPT_ENCODING, HttpHeaders.DEFLATE_GZIP)
              .handler(resp -> {
                testContext.assertEquals(200, resp.statusCode(), "Should have fetched the index page with status 200");
                testContext.assertEquals("gzip", resp.getHeader(HttpHeaders.CONTENT_ENCODING));
              })
              .exceptionHandler(testContext::fail)
              .endHandler(event -> asyncOp.complete())
              .end();
  }

  @After
  public void tearDown(TestContext testContext) throws Exception {
    runTestOnContext.vertx().close();
  }
}
