package indi.xezzon.geom.core.manager;

import static indi.xezzon.geom.core.constant.SpringConstants.BEAN_PREFIX;
import static indi.xezzon.geom.core.constant.SpringConstants.ID_GENERATOR;

import indi.xezzon.tao.exception.ThirdPartyException;
import indi.xezzon.tao.manager.IdGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author xezzon
 */
@Component
@ConditionalOnProperty(prefix = BEAN_PREFIX, name = ID_GENERATOR, havingValue = "redis")
public class RedisIdGenerator implements IdGenerator {

  private static final String GLOBAL_ID_KEY = "global-id";
  private static final String INIT_ID = String.valueOf(10000);
  private final transient RedisTemplate<String, String> redisTemplate;

  @Autowired
  public RedisIdGenerator(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @PostConstruct
  public void init() {
    redisTemplate.opsForValue().setIfAbsent(GLOBAL_ID_KEY, INIT_ID);
  }

  @Override
  public String nextId() {
    Long globalId = redisTemplate.opsForValue().increment(GLOBAL_ID_KEY);
    if (globalId == null) {
      throw new ThirdPartyException("无法从 Redis 中读取全局主键");
    }
    return String.valueOf(globalId);
  }
}
