package io.github.xezzon.geom.admin;

import feign.Target.HardCodedTarget;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author xezzon
 */
class GeomAdminHttpClientTest {

  private static final DockerImageName MOCKSERVER_IMAGE = DockerImageName
      .parse("mockserver/mockserver")
      .withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion());
  private static final MockServerContainer MOCK_SERVER = new MockServerContainer(MOCKSERVER_IMAGE);

  @Test
  void decryptOpenapiBody() {
    try (MockServerContainer mockServer = new MockServerContainer(MOCKSERVER_IMAGE)) {
      mockServer.start();
      String expectedBody = "{\"result\":\"Success\"}";
      try (MockServerClient mockClient = new MockServerClient(
          mockServer.getHost(), mockServer.getServerPort()
      )) {
        mockClient
            .when(HttpRequest.request("/openapi/decrypt")
                .withMethod("POST")
                .withHeader("X-TIMESTAMP")
                .withHeader("X-ACCESS-KEY")
                .withHeader("X-CHECKSUM")
            )
            .respond(HttpResponse.response().withBody(expectedBody));

        GeomAdminHttpClient client = GeomAdminHttpClient.getInstance(new HardCodedTarget<>(
            GeomAdminHttpClient.class, mockServer.getEndpoint()
        ));
        byte[] message = "{\"hello\":\"world\"}".getBytes(StandardCharsets.UTF_8);
        byte[] result = client.decryptOpenapiBody(message, Instant.now(), "accessKey", "checksum");
        Assertions.assertArrayEquals(expectedBody.getBytes(StandardCharsets.UTF_8), result);
      }
    }
  }
}
