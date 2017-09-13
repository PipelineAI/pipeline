package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

import io.vertx.core.http.*;
import io.vertx.ext.web.RoutingContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Kennedy Oliveira
 */
public class HystrixDashboardProxyConnectionHandlerTest {

  private final HystrixDashboardProxyConnectionHandler proxyConnectionHandler = new HystrixDashboardProxyConnectionHandler();
  private RoutingContext routingContext;
  private HttpServerRequest serverRequest;
  private HttpServerResponse serverResponse;

  @Before
  public void setUp() throws Exception {
    routingContext = mock(RoutingContext.class);
    serverRequest = mock(HttpServerRequest.class);
    serverResponse = mock(HttpServerResponse.class);

    when(routingContext.request()).thenReturn(serverRequest);
    when(routingContext.response()).thenReturn(serverResponse);
  }

  @Test
  public void testProxyWithoutOriginShouldFail() throws Exception {
    when(serverResponse.setStatusCode(anyInt())).thenReturn(serverResponse);
    final Optional<String> proxyUrl = proxyConnectionHandler.getProxyUrl(routingContext);

    assertThat(proxyUrl.isPresent(), is(false));
    verify(serverResponse, times(1)).setStatusCode(500);
  }

  @Test
  public void testProxyWithOriginShouldSuccess() throws Exception {
    when(serverResponse.setStatusCode(anyInt())).thenReturn(serverResponse);
    when(serverRequest.getParam("origin")).thenReturn("http://localhost:8080");
    when(serverRequest.params()).thenReturn(new CaseInsensitiveHeaders());

    final Optional<String> proxyUrl = proxyConnectionHandler.getProxyUrl(routingContext);

    assertThat(proxyUrl.isPresent(), is(true));
    assertThat(proxyUrl.get(), is("http://localhost:8080"));
    verify(serverResponse, never()).setStatusCode(anyInt());
  }

  @Test
  public void testOriginWithoutSchemaShouldAddHttp() throws Exception {
    when(serverResponse.setStatusCode(anyInt())).thenReturn(serverResponse);
    when(serverRequest.getParam("origin")).thenReturn("localhost:8080");
    when(serverRequest.params()).thenReturn(new CaseInsensitiveHeaders());

    final Optional<String> proxyUrl = proxyConnectionHandler.getProxyUrl(routingContext);

    assertThat(proxyUrl.isPresent(), is(true));
    assertThat(proxyUrl.get(), is("http://localhost:8080"));
    verify(serverResponse, never()).setStatusCode(anyInt());
  }

  @Test
  public void testOriginWithQueryParamsShouldAddToProxyUrl() throws Exception {
    when(serverResponse.setStatusCode(anyInt())).thenReturn(serverResponse);
    when(serverRequest.getParam("origin")).thenReturn("http://localhost:8080");
    when(serverRequest.params()).thenReturn(new CaseInsensitiveHeaders().add("queryParam1", "test").add("queryParam2", "test2"));

    final Optional<String> proxyUrl = proxyConnectionHandler.getProxyUrl(routingContext);

    assertThat(proxyUrl.isPresent(), is(true));

    assertThat(proxyUrl.get(), is("http://localhost:8080?queryParam1=test&queryParam2=test2"));
    verify(serverResponse, never()).setStatusCode(anyInt());
  }

  @Test
  public void testProxyQueryParamOriginAndAuthorizationShouldBeRomovedFromFinalUrl() throws Exception {
    when(serverResponse.setStatusCode(anyInt())).thenReturn(serverResponse);
    String url = "http://localhost:8080";
    when(serverRequest.getParam("origin")).thenReturn(url);
    when(serverRequest.params()).thenReturn(new CaseInsensitiveHeaders().add("queryParam1", "test")
                                                                        .add("queryParam2", "test2")
                                                                        .add("authorization", "blablabla")
                                                                        .add("origin", "http://localhost:8080?authorization=Basicblablabla&queryParam1=test&queryParam2=test2"));

    final Optional<String> proxyUrl = proxyConnectionHandler.getProxyUrl(routingContext);

    assertThat(proxyUrl.isPresent(), is(true));
    assertThat(proxyUrl.get(), is("http://localhost:8080?queryParam1=test&queryParam2=test2"));
    verify(serverResponse, never()).setStatusCode(anyInt());
  }

  @Test
  public void testShouldSetupBasicAuthIfPresent() throws Exception {
    final HttpClientRequest httpClient = mock(HttpClientRequest.class);
    when(serverRequest.getParam("authorization")).thenReturn("zupao");

    proxyConnectionHandler.configureBasicAuth(serverRequest, httpClient);
    verify(httpClient, times(1)).putHeader(HttpHeaders.AUTHORIZATION, "zupao");
  }
}