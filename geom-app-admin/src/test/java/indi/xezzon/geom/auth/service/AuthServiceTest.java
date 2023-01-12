package indi.xezzon.geom.auth.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import indi.xezzon.geom.auth.constant.SessionConstant;
import indi.xezzon.geom.auth.dao.UserDAO;
import indi.xezzon.geom.auth.domain.User;
import indi.xezzon.geom.auth.domain.UserGroup;
import indi.xezzon.geom.auth.domain.dataset.UserTestDataset;
import indi.xezzon.tao.exception.BaseException;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xezzon
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

  private final transient User currentUser = UserTestDataset.find(Objects::nonNull);
  @Resource
  private transient AuthService authService;
  @Resource
  private transient UserDAO userDAO;
  @Resource
  private transient UserGroupService userGroupService;

  @Test
  @Transactional
  void login() {
    /* 正常流程测试 */
    authService.login(currentUser.getUsername(), currentUser.getPlaintext());
    Assertions.assertTrue(StpUtil.isLogin());
    UserGroup userGroup = StpUtil.getTokenSession()
        .get(SessionConstant.CURRENT_GROUP, null);
    Assertions.assertNotNull(userGroup);
    Assertions.assertEquals(currentUser.getUsername(), userGroup.getCode());
    // 注销
    authService.logout(currentUser.getId());
    Assertions.assertFalse(StpUtil.isLogin());
    Assertions.assertThrows(NotLoginException.class,
        () -> StpUtil.getTokenSession().get(SessionConstant.CURRENT_GROUP)
    );
    /* 预期异常测试 */
    Assertions.assertThrows(BaseException.class,
        () -> authService.login(RandomUtil.randomString(6), currentUser.getPlaintext())
    );
    Assertions.assertThrows(BaseException.class,
        () -> authService.login(currentUser.getUsername(), RandomUtil.randomString(6))
    );
    currentUser.setActivateTime(LocalDateTime.now().plusMonths(1));
    userDAO.save(currentUser);
    Assertions.assertThrows(BaseException.class,
        () -> authService.login(currentUser.getUsername(), currentUser.getPlaintext())
    );
  }

  @Test
  @jakarta.transaction.Transactional
  void checkCipher() {
    /* 正常流程 */
    StpUtil.login(currentUser.getId());
    Assertions.assertTrue(authService.checkCipher(currentUser.getPlaintext()));
    Assertions.assertFalse(authService.checkCipher(RandomUtil.randomString(6)));
    authService.logout(currentUser.getId());
    Assertions.assertFalse(authService.checkCipher(currentUser.getPlaintext()));
  }

  @Test
  void switchGroup() {
    authService.login(currentUser.getUsername(), currentUser.getPlaintext());
    /* 正常流程 */
    List<UserGroup> userGroups = userGroupService.listByUserId(StpUtil.getLoginId(null));
    Assertions.assertNotEquals(0, userGroups.size());
    Assertions.assertDoesNotThrow(
        () -> authService.switchGroup(userGroups.stream().findAny().get().getCode())
    );
    /* 预期异常 */
    StpUtil.logout();
    Assertions.assertThrows(NotLoginException.class,
        () -> authService.switchGroup(userGroups.stream().findAny().get().getCode())
    );
  }

  @Test
  void getCurrentGroup() {
    authService.login(currentUser.getUsername(), currentUser.getPlaintext());
    UserGroup currentGroup = authService.getCurrentGroup();
    Assertions.assertEquals(currentUser.getUsername(), currentGroup.getCode());
  }
}