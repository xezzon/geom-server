package indi.xezzon.geom.auth.adaptor;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import indi.xezzon.geom.auth.domain.User;
import indi.xezzon.geom.auth.domain.convert.UserConvert;
import indi.xezzon.geom.auth.domain.query.LoginQuery;
import indi.xezzon.geom.auth.domain.query.RegisterQuery;
import indi.xezzon.geom.auth.service.AuthService;
import indi.xezzon.geom.auth.service.UserService;
import indi.xezzon.tao.logger.LogRecord;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 * @author xezzon
 */
@RestController
@RequestMapping
public class AuthController {

  private transient final UserService userService;
  private transient final AuthService authService;

  @Autowired
  public AuthController(UserService userService, AuthService authService) {
    this.userService = userService;
    this.authService = authService;
  }

  /**
   * 用户注册
   * @param user 用户名 密码 等
   * @return 用户ID 用户昵称
   */
  @PostMapping("/register")
  public User register(@RequestBody @Valid RegisterQuery user) {
    return userService.register(UserConvert.INSTANCE.from(user));
  }

  /**
   * 用户登录
   * @param query 用户名 密码
   */
  @PostMapping("/login")
  @LogRecord
  public SaTokenInfo login(@RequestBody LoginQuery query) {
    authService.login(query.getUsername(), query.getCipher());
    return StpUtil.getTokenInfo();
  }

  /**
   * 查询当前登录用户
   * @return 当前用户账号信息
   */
  @GetMapping("/me")
  @SaCheckLogin
  public User getCurrentUser() {
    StpUtil.checkLogin();
    return userService.getById(StpUtil.getLoginId(null));
  }

  /**
   * 检查登录状态
   * @return 是否已登录
   */
  @GetMapping("/check-login")
  public boolean checkLogin() {
    return StpUtil.isLogin();
  }

  /**
   * 注销登录
   */
  @PostMapping("/logout")
  public void logout() {
    StpUtil.logout();
  }
}
