package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;

/**
 * @author Kennedy Oliveira
 */
@Slf4j
@RunWith(VertxUnitRunner.class)
public class HystrixDashboardProxyEurekaTest {

  private static final int FAKE_EUREKA_SERVER_PORT = 9999;
  private static final String DASHBOARD_EUREKA_PROXY_URL = "/hystrix-dashboard/eureka?url=";
  private static Vertx vertx;
  private static HttpClient httpClient;

  @BeforeClass
  public static void setUp(TestContext testContext) throws Exception {
    vertx = Vertx.vertx();

    deployFakeEurekaVerticle().compose(i -> deployDashboard())
                              .compose(i -> initializeHttpClientForDashboard())
                              .setHandler(testContext.asyncAssertSuccess());
  }

  @Test
  public void testShouldFetchDataWithHeaders(TestContext testContext) throws Exception {
    final String fakeEurekaServerUrl = "http://localhost:" + FAKE_EUREKA_SERVER_PORT + "/eureka/v2/apps";
    final String dashboardProxyUrl = DASHBOARD_EUREKA_PROXY_URL + fakeEurekaServerUrl;

    final Async fetchData = testContext.async();

    httpClient.getNow(dashboardProxyUrl, resp -> resp.bodyHandler(buffer -> {
      final String responseData = buffer.toString(StandardCharsets.UTF_8);

      if (resp.statusCode() != 200) {
        testContext.fail("Response Status => " + resp.statusCode() + "\nResponse: " + responseData);
      } else {
        testContext.assertTrue("application/xml".equals(resp.getHeader(HttpHeaderNames.CONTENT_TYPE)));

        testContext.assertTrue(responseData.contains("<apps__hashcode>UP_2_</apps__hashcode>"));
        testContext.assertTrue(responseData.contains("<registrationTimestamp>1472352522224</registrationTimestamp>"));

        fetchData.complete();
      }
    }));

    fetchData.awaitSuccess(5000L);
  }

  @Test
  public void testShouldFailOnOfflineEureka(TestContext testContext) throws Exception {
    final String proxyUrl = DASHBOARD_EUREKA_PROXY_URL + "http://localhost:54321/eureka/v2/apps";
    final Async proxyRequest = testContext.async();

    httpClient.getNow(proxyUrl, resp -> {
      testContext.assertTrue(resp.statusCode() == HttpResponseStatus.INTERNAL_SERVER_ERROR.code());

      resp.bodyHandler(bodyBuffer -> {
        final String body = bodyBuffer.toString(StandardCharsets.UTF_8);
        log.info("Response Body: {}", body);
        testContext.assertTrue(body.contains("Connection refused"));
        proxyRequest.complete();
      });
    });

    proxyRequest.awaitSuccess(5000L);
  }

  private static Future<Void> deployFakeEurekaVerticle() {
    final Future<String> handler = Future.future();

    vertx.deployVerticle(fakeEurekaVerticle(), handler.completer());

    return handler.map(i -> null);
  }

  private static Future<Void> deployDashboard() {
    final Future<String> future = Future.future();

    vertx.deployVerticle(HystrixDashboardVerticle.class.getName(), future.completer());

    return future.map(i -> null);
  }

  private static Future<Void> initializeHttpClientForDashboard() {
    httpClient = vertx.createHttpClient(new HttpClientOptions()
                                          .setDefaultHost("localhost")
                                          .setDefaultPort(7979));

    return Future.succeededFuture();
  }

  private static AbstractVerticle fakeEurekaVerticle() {
    return new AbstractVerticle() {

      @Override
      public void start(Future<Void> startFuture) throws Exception {
        final Router router = Router.router(vertx);

        router.get("/eureka/v2/apps").handler(context -> {
          final HttpServerResponse response = context.response();

          vertx.fileSystem()
               .readFile("eureka-data.xml", res -> {
                 if (res.succeeded()) {
                   response.putHeader(HttpHeaders.CONTENT_TYPE, "application/xml")
                           .end(res.result());
                 } else {
                   response.setStatusCode(500)
                           .end("Error while reading eureka data from classpath");
                 }
               });
        });

        vertx.createHttpServer(new HttpServerOptions())
             .requestHandler(router::accept)
             .listen(FAKE_EUREKA_SERVER_PORT, res -> {
               if (res.succeeded()) {
                 startFuture.complete();
               } else {
                 startFuture.fail(res.cause());
               }
             });
      }
    };
  }

  @AfterClass
  public static void tearDown(TestContext testContext) throws Exception {
    if (vertx != null) {
      vertx.close(testContext.asyncAssertSuccess());
    }
  }
}