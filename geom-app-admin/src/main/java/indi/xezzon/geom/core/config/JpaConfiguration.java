package indi.xezzon.geom.core.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xezzon
 */
@Configuration
public class JpaConfiguration {

  @Autowired
  public JpaConfiguration() {
  }

  @Bean
  public JPAQueryFactory queryFactory(EntityManager em) {
    return new JPAQueryFactory(em);
  }
}
