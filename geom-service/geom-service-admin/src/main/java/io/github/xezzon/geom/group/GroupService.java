package io.github.xezzon.geom.group;

import io.github.xezzon.geom.crypto.service.SymmetricCryptoService;
import io.github.xezzon.geom.group.domain.Group;
import io.github.xezzon.geom.group.domain.GroupMember;
import io.github.xezzon.geom.group.domain.GroupMemberUser;
import io.github.xezzon.geom.group.service.IGroupService4Auth;
import io.github.xezzon.geom.user.domain.User;
import io.github.xezzon.geom.user.service.IUserService4Group;
import io.github.xezzon.tao.exception.ClientException;
import io.github.xezzon.tao.exception.ServerException;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xezzon
 */
@Singleton
public class GroupService implements IGroupService4Auth {

  private final transient GroupDAO groupDAO;
  private final transient GroupMemberDAO groupMemberDAO;
  private final transient IUserService4Group userService;
  private final transient SymmetricCryptoService symmetricCryptoService;

  public GroupService(
      GroupDAO groupDAO,
      GroupMemberDAO groupMemberDAO,
      IUserService4Group userService,
      SymmetricCryptoService symmetricCryptoService
  ) {
    this.groupDAO = groupDAO;
    this.groupMemberDAO = groupMemberDAO;
    this.userService = userService;
    this.symmetricCryptoService = symmetricCryptoService;
  }

  protected List<Group> listGroupByUserId(String userId) {
    if (userId == null) {
      return Collections.emptyList();
    }
    List<GroupMember> members = groupMemberDAO.get().findByUserId(userId);
    if (members.isEmpty()) {
      return Collections.emptyList();
    }
    Set<String> groupsId = members.parallelStream()
        .map(GroupMember::getGroupId)
        .collect(Collectors.toSet());
    return groupDAO.get().findByIdIn(groupsId);
  }

  protected void addGroup(Group group) {
    // 检查重复项
    if (groupDAO.get().existsByCodeAndOwnerId(group.getCode(), group.getOwnerId())) {
      throw new ClientException("用户组" + group.getCode() + "已存在");
    }

    groupDAO.get().save(group);
    /* 后置处理 */
    this.joinGroup(group.getId(), Collections.singleton(group.getOwnerId()));
  }

  protected void joinGroup(String groupId, Collection<String> usersId) {
    List<GroupMember> members = usersId.parallelStream()
        .map(userId -> {
          GroupMember member = new GroupMember();
          member.setGroupId(groupId);
          member.setUserId(userId);
          return member;
        })
        .toList();
    groupMemberDAO.insertIfNotExisted(members);
  }

  protected String generateSecretKey(String groupId) {
    String secretKey = symmetricCryptoService.generateSymmetricSecretKey();
    Group group = new Group();
    group.setId(groupId);
    group.setSecretKey(secretKey);
    boolean updated = groupDAO.update(group);
    if (!updated) {
      throw new ServerException("用户组不存在");
    }
    return secretKey;
  }

  protected Page<GroupMemberUser> listGroupMember(String groupId, int pageNum, short pageSize) {
    Page<GroupMember> page = groupMemberDAO.get()
        .findByGroupId(groupId, Pageable.from(pageNum, pageSize));
    List<GroupMemberUser> memberUsers = page.getContent().parallelStream()
        .map(member -> {
          User user = userService.getUserById(member.getUserId());
          if (user == null) {
            return null;
          }
          Group group = new Group();
          group.setId(member.getGroupId());
          return GroupMemberUser.build(group, user);
        })
        .filter(Objects::nonNull)
        .toList();
    return Page.of(memberUsers, page.getPageable(), page.getTotalSize());
  }

  protected int removeMember(String groupId, Collection<String> membersId) {
    Group group = groupDAO.get().findById(groupId)
        .orElseThrow(() -> new ClientException("用户组不存在"));
    return groupMemberDAO.get().deleteByIdInAndGroupIdAndUserIdNot(
        membersId, groupId, group.getOwnerId()
    );
  }

  @Override
  public String getSecretKey(String groupId) {
    return groupDAO.get().findById(groupId)
        .map(Group::getSecretKey)
        .orElseThrow(() -> new ClientException("获取不到密钥"));
  }
}
