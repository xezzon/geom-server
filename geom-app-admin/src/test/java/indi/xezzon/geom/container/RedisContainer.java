package indi.xezzon.geom.container;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;

@Component
@Order(0)
@Profile("test")
public class RedisContainer {

  private static final int PORT = 6379;
  private final transient GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
      .withExposedPorts(PORT);

  @PostConstruct
  public void postConstruct() {
    redis.start();
    System.setProperty("spring.redis.host", redis.getHost());
    System.setProperty("spring.redis.port", redis.getMappedPort(PORT).toString());
  }

  @PreDestroy
  public void preDestroy() {
    redis.stop();
  }
}
