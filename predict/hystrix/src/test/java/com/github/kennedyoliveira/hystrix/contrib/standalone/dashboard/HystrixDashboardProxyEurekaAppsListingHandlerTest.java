package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author Kennedy Oliveira
 */
public class HystrixDashboardProxyEurekaAppsListingHandlerTest {

  private static HystrixDashboardProxyEurekaAppsListingHandler proxyEurekaAppsListingHandler;
  private static RoutingContext routingContext;
  private static HttpServerRequest serverRequest;
  private static HttpServerResponse serverResponse;
  private static Vertx vertx;

  @BeforeClass
  public static void setUp() throws Exception {
    vertx = Vertx.vertx();
    proxyEurekaAppsListingHandler = HystrixDashboardProxyEurekaAppsListingHandler.create(vertx);
    routingContext = mock(RoutingContext.class);
    serverRequest = mock(HttpServerRequest.class);
    serverResponse = mock(HttpServerResponse.class);

    when(routingContext.request()).thenReturn(serverRequest);
    when(routingContext.response()).thenReturn(serverResponse);
  }

  @Test
  public void testShouldFailOnEmptyEurekaUrl() throws Exception {
    when(serverRequest.getParam("url")).thenReturn("");
    when(serverResponse.setStatusCode(anyInt())).thenReturn(serverResponse);

    proxyEurekaAppsListingHandler.handle(routingContext);

    verify(serverResponse, times(1)).setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
  }

  @Test
  public void testShouldFailOnInvalidEurekaUrl() throws Exception {
    when(serverRequest.getParam("url")).thenReturn("http:localhost:8080");
    when(serverResponse.setStatusCode(anyInt())).thenReturn(serverResponse);

    proxyEurekaAppsListingHandler.handle(routingContext);

    verify(serverResponse, times(1)).setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
  }

  @AfterClass
  public static void tearDown() throws Exception {
    if(vertx != null) {
      vertx.close();
    }
  }
}