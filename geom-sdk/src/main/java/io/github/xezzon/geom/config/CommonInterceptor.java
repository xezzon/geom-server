package io.github.xezzon.geom.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @author xezzon
 */
public class CommonInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    requestTemplate
        .header("Content-Type", "application/json");
  }
}
