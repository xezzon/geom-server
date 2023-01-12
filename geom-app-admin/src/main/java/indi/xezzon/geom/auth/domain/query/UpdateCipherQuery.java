package indi.xezzon.geom.auth.domain.query;

import static indi.xezzon.geom.constant.PatternConstants.PASSWORD_PATTERN;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author xezzon
 */
@Getter
@Setter
@ToString
public class UpdateCipherQuery {

  /**
   * 原密码
   */
  @NotNull(message = "密码不能为空")
  private String oldCipher;
  /**
   * 新密码
   */
  @Pattern(
      regexp = PASSWORD_PATTERN,
      message = "密码由至少8位有效字符构成，且包含 大小写字符/数字/特殊字符(@!#$%^&*) 中至少两类"
  )
  private String newCipher;
}
