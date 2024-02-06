package io.github.xezzon.geom.crypto.service;

/**
 * 摘要算法
 * @author xezzon
 */
public interface DigestCryptoService {

  /**
   * 验证摘要
   * @param data 原数据
   * @param digest 摘要
   * @return 是否验证通过
   */
  boolean verifyDigest(byte[] data, String digest);

  /**
   * 验证摘要
   * @param data 原数据
   * @param digest 摘要
   * @param salt 盐
   * @return 是否验证通过
   */
  boolean verifyDigest(byte[] data, String digest, String salt);
}
