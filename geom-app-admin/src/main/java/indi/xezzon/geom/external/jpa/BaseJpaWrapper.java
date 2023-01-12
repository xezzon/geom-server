package indi.xezzon.geom.external.jpa;

import cn.hutool.core.util.ReflectUtil;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import indi.xezzon.tao.retrieval.CommonQuery;
import jakarta.annotation.Resource;
import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.hibernate.AnnotationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xezzon
 */
public abstract class BaseJpaWrapper<T, D extends EntityPathBase<T>, M extends JpaRepository<T, ?> & QuerydslPredicateExecutor<T>>
    implements JpaWrapper<T, M> {

  protected final transient M dao;
  @Resource
  protected transient JPAQueryFactory queryFactory;

  protected BaseJpaWrapper(M dao) {
    this.dao = dao;
  }

  @Override
  public M get() {
    return this.dao;
  }

  /**
   * 获取DO对象
   * @return DO对象
   */
  protected abstract D getQuery();

  /**
   * 获取标准实体模型的类
   * @return 类
   */
  protected abstract Class<T> getBeanClass();

  @Override
  public Page<T> query(CommonQuery params) {
    BooleanExpression queryClause =
        JpaUtil.getQueryClause(params, this.getQuery(), this.getBeanClass());
    Pageable pageable = JpaUtil.getPageable(params);
    return this.get().findAll(queryClause, pageable);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  @Transactional(rollbackFor = {Exception.class})
  public boolean update(T t) {
    JPAUpdateClause clause =
        JpaUtil.getUpdateClause(t, queryFactory, this.getQuery());
    Field idField = Arrays.stream(this.getBeanClass().getDeclaredFields())
        .filter(field -> field.isAnnotationPresent(Id.class))
        .findAny()
        .orElseThrow(() -> new AnnotationException("No identifier specified for entity"));
    SimpleExpression column =
        (SimpleExpression) ReflectUtil.getFieldValue(this.getQuery(), idField.getName());
    Object value = ReflectUtil.getFieldValue(t, idField);
    clause.where(column.eq(value));
    long affected = clause.execute();
    return affected > 0;
  }
}
