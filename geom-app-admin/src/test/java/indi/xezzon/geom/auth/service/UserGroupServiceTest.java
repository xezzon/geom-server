package indi.xezzon.geom.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import indi.xezzon.geom.auth.dao.UserGroupDAO;
import indi.xezzon.geom.auth.domain.QUserGroup;
import indi.xezzon.geom.auth.domain.User;
import indi.xezzon.geom.auth.domain.UserGroup;
import indi.xezzon.geom.auth.domain.dataset.UserGroupTestDataset;
import indi.xezzon.geom.auth.domain.dataset.UserTestDataset;
import indi.xezzon.tao.exception.ClientException;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserGroupServiceTest {

  private final transient User currentUser = UserTestDataset.find(Objects::nonNull);
  @Resource
  private transient UserGroupService userGroupService;
  @Resource
  private transient UserGroupDAO userGroupDAO;
  @Resource
  private transient UserService userService;
  @Resource
  private transient AuthService authService;

  @BeforeEach
  void setUp() {
    authService.login(currentUser.getUsername(), currentUser.getPlaintext());
  }

  @Test
  @Transactional
  void insert() {
    UserGroup userGroup = new UserGroup()
        .setCode(RandomUtil.randomString(6))
        .setName(RandomUtil.randomString(6));
    userGroupService.insert(userGroup);
    UserGroup userGroup1 = userGroupDAO.getById(userGroup.getId());
    Assertions.assertNotNull(userGroup1);
  }

  @Test
  @Transactional
  void handleUserRegisterObservation() {
    User user = new User()
        .setUsername(RandomUtil.randomString(6))
        .setPlaintext(RandomUtil.randomString(6));
    User register = userService.register(user);
    Optional<UserGroup> userGroup = userGroupDAO.findOne(
        QUserGroup.userGroup.code.eq(user.getUsername())
    );
    Assertions.assertTrue(userGroup.isPresent());
    Assertions.assertEquals(register.getId(), userGroup.get().getOwnerId());
  }

  @Test
  @Transactional
  void transfer() {
    /* 数据准备 */
    final User receiver = UserTestDataset.find(
        (user) -> !Objects.equals(user.getId(), currentUser.getId())
    );
    final UserGroup userGroup = UserGroupTestDataset.find(
        (o) -> Objects.equals(o.getOwnerId(), currentUser.getId())
    );
    /* 正常流程 */
    Assertions.assertDoesNotThrow(
        () -> userGroupService.transfer(userGroup.getId(), receiver.getId())
    );
    UserGroup userGroup1 = userGroupDAO.findById(userGroup.getId()).get();
    Assertions.assertEquals(receiver.getId(), userGroup1.getOwnerId());
    /* 预期异常 */
    // 无权转让
    Assertions.assertThrows(ClientException.class,
        () -> userGroupService.transfer(userGroup.getId(), currentUser.getId())
    );
    StpUtil.switchTo(receiver.getId());
    // 用户组不存在
    Assertions.assertThrows(ClientException.class,
        () -> userGroupService.transfer(RandomUtil.randomString(6), currentUser.getId())
    );
    // 受转让者不存在
    Assertions.assertThrows(ClientException.class,
        () -> userGroupService.transfer(userGroup.getId(), RandomUtil.randomString(6))
    );
    StpUtil.endSwitch();
  }

  @Test
  @Transactional
  void addMember() {
    /* 数据准备 */
    final User member = UserTestDataset.find(
        (user) -> !Objects.equals(user.getId(), currentUser.getId())
    );
    final UserGroup userGroup = UserGroupTestDataset.find(Objects::nonNull);
    /* 正常流程 */
    Assertions.assertDoesNotThrow(
        () -> userGroupService.addMember(userGroup.getId(), member.getId())
    );
    // 重复添加
    Assertions.assertDoesNotThrow(
        () -> userGroupService.addMember(userGroup.getId(), member.getId())
    );
  }

  @Test
  @Transactional
  void removeMember() {
    /* 数据准备 */
    final UserGroup userGroup = UserGroupTestDataset.find(
        (o) -> Objects.equals(o.getOwnerId(), currentUser.getId())
    );
    final User member = UserTestDataset.find(
        (o) -> !Objects.equals(o.getId(), currentUser.getId())
    );
    userGroupService.addMember(userGroup.getId(), member.getId());
    /* 正常流程 */
    Assertions.assertDoesNotThrow(
        () -> userGroupService.removeMember(userGroup.getId(), member.getId())
    );
    // 重复删除
    Assertions.assertDoesNotThrow(
        () -> userGroupService.removeMember(userGroup.getId(), member.getId())
    );
    /* 预期异常 */
    // 用户组不存在
    Assertions.assertThrows(ClientException.class,
        () -> userGroupService.removeMember(RandomUtil.randomString(6), member.getId())
    );
    // 移除所有者
    Assertions.assertThrows(ClientException.class,
        () -> userGroupService.removeMember(userGroup.getId(), currentUser.getId())
    );
  }
}