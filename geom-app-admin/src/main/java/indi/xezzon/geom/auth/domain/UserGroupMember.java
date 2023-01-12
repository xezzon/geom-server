package indi.xezzon.geom.auth.domain;

import static indi.xezzon.geom.auth.domain.UserGroupMember.GROUP_ID;
import static indi.xezzon.geom.auth.domain.UserGroupMember.USER_ID;

import indi.xezzon.geom.core.constant.DatabaseConstant;
import indi.xezzon.geom.core.manager.HibernateIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.GenericGenerator;

/**
 * 用户组成员
 * @author xezzon
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@Table(
    name = "system_user_group_member",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {GROUP_ID, USER_ID})
    }
)
@Entity
public class UserGroupMember {

  static final String GROUP_ID = "group_id";
  static final String USER_ID = "user_id";

  /**
   * 用户组成员主键
   */
  @Id
  @Column(name = "id", nullable = false, updatable = false, length = DatabaseConstant.ID_LENGTH)
  @GenericGenerator(
      name = HibernateIdGenerator.GENERATOR_NAME,
      strategy = HibernateIdGenerator.GENERATOR_STRATEGY
  )
  @GeneratedValue(generator = HibernateIdGenerator.GENERATOR_NAME)
  private String id;
  /**
   * 用户组主键
   */
  @Column(name = GROUP_ID, nullable = false, updatable = false, length = DatabaseConstant.ID_LENGTH)
  private String groupId;
  /**
   * 用户主键
   */
  @Column(name = USER_ID, nullable = false, updatable = false, length = DatabaseConstant.ID_LENGTH)
  private String userId;

  /**
   * 用户组信息
   */
  @Transient
  private UserGroup group;
  /**
   * 用户信息
   */
  @Transient
  private User user;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserGroupMember that = (UserGroupMember) o;
    return groupId.equals(that.groupId) && userId.equals(that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, userId);
  }

  /**
   * 用户组成员主键
   */
  public static class UserGroupMemberId {

    private String groupId;
    private String userId;
  }
}
