package io.github.xezzon.geom.auth;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import java.time.Instant;

/**
 * @author xezzon
 */
@Controller("/openapi")
public class OpenapiController {

  private final AuthService authService;

  public OpenapiController(AuthService authService) {
    this.authService = authService;
  }

  @Post("/decrypt")
  public byte[] decryptMessage(
      @Header("X-TIMESTAMP") Instant date,
      @Header("X-ACCESS-KEY") String accessKey,
      @Header("X-CHECKSUM") String checksum,
      @Body byte[] origin
  ) {
    return authService.decryptMessage(origin, accessKey, checksum, date);
  }
}
