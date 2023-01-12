package indi.xezzon.geom.auth.domain;

import indi.xezzon.geom.core.constant.DatabaseConstant;
import indi.xezzon.geom.core.manager.HibernateIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

/**
 * 用户组
 * @author xezzon
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Entity
@Table(name = "system_user_group")
public class UserGroup {

  /**
   * 用户组主键
   */
  @Id
  @Column(name = "id", nullable = false, updatable = false, length = 64)
  @GenericGenerator(
      name = HibernateIdGenerator.GENERATOR_NAME,
      strategy = HibernateIdGenerator.GENERATOR_STRATEGY
  )
  @GeneratedValue(generator = HibernateIdGenerator.GENERATOR_NAME)
  private String id;
  /**
   * 用户组编码
   */
  @Column(name = "code", nullable = false, unique = true)
  private String code;
  /**
   * 用户组名称
   */
  @Column(name = "name", nullable = false)
  private String name;
  /**
   * 所属用户ID
   */
  @Column(name = "owner_id", nullable = false, length = DatabaseConstant.ID_LENGTH)
  private String ownerId;

  @Transient
  private Set<UserGroupMember> members;
}
