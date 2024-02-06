package io.github.xezzon.geom.admin;

import feign.Feign;
import feign.Headers;
import feign.Logger.Level;
import feign.Param;
import feign.RequestLine;
import feign.Target;
import feign.okhttp.OkHttpClient;
import io.github.xezzon.geom.config.CommonInterceptor;
import java.time.Instant;

/**
 * @author xezzon
 */
public interface GeomAdminHttpClient {

  static GeomAdminHttpClient getInstance(Target<GeomAdminHttpClient> target) {
    return Feign.builder()
        .requestInterceptor(new CommonInterceptor())
        .client(new OkHttpClient())
        .logLevel(Level.BASIC)
        .target(target);
  }

  @RequestLine("POST /openapi/decrypt")
  @Headers({
      "X-TIMESTAMP: {date}",
      "X-ACCESS-KEY: {accessKey}",
      "X-CHECKSUM: {checksum}",
  })
  byte[] decryptOpenapiBody(byte[] body, @Param("date") Instant date, @Param("accessKey") String accessKey, @Param("checksum") String checksum);
}
