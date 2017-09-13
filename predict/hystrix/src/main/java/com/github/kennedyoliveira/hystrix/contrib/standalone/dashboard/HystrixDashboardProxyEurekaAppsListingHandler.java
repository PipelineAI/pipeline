package com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static com.github.kennedyoliveira.hystrix.contrib.standalone.dashboard.StringUtils.isNullOrEmpty;

/**
 * Handler for proxying a request to eureka for listing apps.
 *
 * @author Kennedy Oliveira
 * @since 1.5.6
 */
@Slf4j
public class HystrixDashboardProxyEurekaAppsListingHandler implements Handler<RoutingContext> {

  private final HttpClient httpClient;

  HystrixDashboardProxyEurekaAppsListingHandler(Vertx vertx) {
    final HttpClientOptions httpClientOptions = new HttpClientOptions().setConnectTimeout(10000)
                                                                       .setTryUseCompression(true)
                                                                       .setIdleTimeout(60)
                                                                       .setKeepAlive(true)
                                                                       .setMaxPoolSize(10);

    this.httpClient = vertx.createHttpClient(httpClientOptions);
  }

  public static HystrixDashboardProxyEurekaAppsListingHandler create(Vertx vertx) {
    Objects.requireNonNull(vertx);
    return new HystrixDashboardProxyEurekaAppsListingHandler(vertx);
  }

  @Override
  public void handle(RoutingContext routingContext) {
    final HttpServerRequest request = routingContext.request();
    final HttpServerResponse response = routingContext.response();

    final String eurekaUrl = request.getParam("url");

    log.debug("Routing URL: [{}] to eureka", eurekaUrl);

    if (isNullOrEmpty(eurekaUrl)) {
      log.warn("URL is null for Eureka server...");

      response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
              .end("Error. You need supply a valid eureka URL.");
    } else {
      try {
        final URI uri = URI.create(eurekaUrl);

        final int port = Optional.ofNullable(uri.getPort()).orElse(80);
        final String host = uri.getHost();
        final String path = uri.getPath();

        httpClient.get(port, host, path)
                  .handler(clientResp -> clientResp.bodyHandler(respBuffer -> {
                    // proxied eureka request OK
                    if (clientResp.statusCode() == 200) {
                      response.putHeader(HttpHeaders.CONTENT_TYPE, clientResp.headers().get(HttpHeaders.CONTENT_TYPE))
                              .end(respBuffer);
                    } else {
                      log.error("Fetching eureka apps from url: [{}]\nResponse Status: [{}], Response: [{}]",
                                eurekaUrl,
                                clientResp.statusCode(),
                                respBuffer.toString(StandardCharsets.UTF_8));
                      internalServerError(response, "Error while fetching eureka apps from url " + eurekaUrl + ".");
                    }
                  }))
                  .exceptionHandler(ex -> {
                    log.error("Fetching eureka apps from url: [ " + eurekaUrl + "]", ex);
                    internalServerError(response, "Error while fetching eureka apps from url " + eurekaUrl + ".\n" + ex.getMessage());
                  })
                  .end();
      } catch (Exception e) {
        log.error("Fetching eureka apps", e);
        internalServerError(response, "Error while fetching eureka apps from url " + eurekaUrl + ".\n" + e.getMessage());
      }
    }
  }

  private void internalServerError(HttpServerResponse response, String message) {
    response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(message);
  }
}
