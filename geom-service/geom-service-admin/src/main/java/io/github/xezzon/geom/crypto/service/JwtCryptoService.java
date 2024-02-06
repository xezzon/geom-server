package io.github.xezzon.geom.crypto.service;

/**
 * JWT相关
 * @author xezzon
 */
public interface JwtCryptoService {

  /**
   * 签发JWT
   * @param subject bean对象
   * @return JWT字符串
   */
  String sign(Object subject);
}
