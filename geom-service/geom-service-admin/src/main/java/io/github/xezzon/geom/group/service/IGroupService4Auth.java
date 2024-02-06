package io.github.xezzon.geom.group.service;

/**
 * @author xezzon
 */
public interface IGroupService4Auth {

  /**
   * 获取指定用户组的密钥
   * @param groupId 用户组ID
   * @return Base64格式的密钥
   */
  String getSecretKey(String groupId);
}
