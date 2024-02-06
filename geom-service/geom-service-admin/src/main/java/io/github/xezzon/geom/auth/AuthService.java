package io.github.xezzon.geom.auth;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import io.github.xezzon.geom.auth.constant.AuthConstants;
import io.github.xezzon.geom.crypto.service.DigestCryptoService;
import io.github.xezzon.geom.crypto.service.JwtCryptoService;
import io.github.xezzon.geom.crypto.service.SymmetricCryptoService;
import io.github.xezzon.geom.group.domain.Group;
import io.github.xezzon.geom.group.service.IGroupService4Auth;
import io.github.xezzon.geom.user.UserDTO;
import io.github.xezzon.geom.user.UserService;
import io.github.xezzon.geom.user.domain.User;
import io.github.xezzon.geom.user.domain.UserDTOConverter;
import io.github.xezzon.geom.user.service.IUserService4Auth;
import io.github.xezzon.tao.exception.ClientException;
import io.micronaut.context.annotation.Bean;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * @author xezzon
 */
@Bean
public class AuthService {

  private final transient IUserService4Auth userService;
  private final transient IGroupService4Auth groupService;
  private final transient JwtCryptoService jwtCryptoService;
  private final transient SymmetricCryptoService symmetricCryptoService;
  private final transient DigestCryptoService digestCryptoService;

  public AuthService(
      UserService userService,
      IGroupService4Auth groupService,
      JwtCryptoService jwtCryptoService,
      SymmetricCryptoService symmetricCryptoService,
      DigestCryptoService digestCryptoService
  ) {
    this.userService = userService;
    this.groupService = groupService;
    this.jwtCryptoService = jwtCryptoService;
    this.symmetricCryptoService = symmetricCryptoService;
    this.digestCryptoService = digestCryptoService;
  }

  protected void login(String username, String cipher) {
    if (StpUtil.isLogin()) {
      return;
    }
    User user = userService.getUserByUsername(username);
    if (user == null) {
      throw new ClientException("用户名或密码错误");
    }
    if (!BCrypt.checkpw(cipher, user.getCipher())) {
      throw new ClientException("用户名或密码错误");
    }
    /* 执行主流程 */
    StpUtil.login(user.getId());
    /* 将用户信息写入Session */
    UserDTO dto = UserDTOConverter.INSTANCE.from(user);
    StpUtil.getSession()
        .set(AuthConstants.SUBJECT, dto);
  }

  protected UserDTO getCurrentUser() {
    return StpUtil.getSession()
        .getModel(AuthConstants.SUBJECT, UserDTO.class);
  }

  protected String signJwt() {
    UserDTO currentUser = this.getCurrentUser();
    return jwtCryptoService.sign(currentUser);
  }

  protected byte[] decryptMessage(byte[] origin, String accessKey, String checksum, Instant date) {
    if (date.isAfter(Instant.now().minus(1, ChronoUnit.MINUTES))) {
      throw new ClientException("请求超时");
    }
    Group group = new Group();
    group.setAccessKey(accessKey);
    String secretKey = groupService.getSecretKey(group.getId());
    byte[] target = symmetricCryptoService.symmetricDecrypt(origin, secretKey);
    boolean checked = digestCryptoService.verifyDigest(target, checksum);
    if (!checked) {
      throw new ClientException("无效的请求");
    }
    return target;
  }
}
