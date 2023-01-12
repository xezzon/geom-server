package indi.xezzon.geom.core.domain;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 数据集抽象类 用于定义初始化数据（含正式数据及测试数据）的抽象类
 * @author xezzon
 */
public abstract class AbstractDataset<T> {

  protected final transient Collection<T> dataset;
  private final transient JpaRepository<T, ?> repository;

  protected AbstractDataset(Collection<T> dataset, JpaRepository<T, ?> repository) {
    this.dataset = dataset;
    this.repository = repository;
  }

  /**
   * 初始化数据集
   */
  @PostConstruct
  public void initialize() {
    if (dataset.isEmpty()) {
      return;
    }
    repository.saveAll(dataset);
  }
}
