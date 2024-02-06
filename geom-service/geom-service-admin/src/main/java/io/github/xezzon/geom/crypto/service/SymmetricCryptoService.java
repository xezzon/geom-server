package io.github.xezzon.geom.crypto.service;

/**
 * 对称加密
 * @author xezzon
 */
public interface SymmetricCryptoService {

  /**
   * 生成对称加密密钥
   * @return Base64格式的密钥
   */
  String generateSymmetricSecretKey();

  /**
   * 解密
   * @param origin 密文
   * @param secretKey Base64格式的密钥
   * @return 明文
   */
  byte[] symmetricDecrypt(byte[] origin, String secretKey);
}
