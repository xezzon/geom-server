package io.github.xezzon.geom.role;

import cn.hutool.core.util.RandomUtil;
import io.github.xezzon.geom.role.domain.AddRoleQuery;
import io.github.xezzon.geom.role.domain.Role;
import io.github.xezzon.geom.role.repository.RoleRepository;
import io.github.xezzon.tao.exception.ClientException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@MicronautTest(transactional = false)
@TestInstance(Lifecycle.PER_CLASS)
class RoleServiceTest {

  @Inject
  transient RoleService service;
  @Inject
  transient RoleRepository repository;

  @BeforeAll
  void setUp() {
    final List<Role> DATASET = new ArrayList<>();
    for (int i = 0, cnt = Byte.MAX_VALUE; i < cnt; i++) {
      Role role = new Role();
      role.setCode(RandomUtil.randomString(9));
      role.setName(RandomUtil.randomString(9));
      role.setInheritable(true);
      String parentId = DATASET.isEmpty() ? "0" : RandomUtil.randomEle(DATASET).getId();
      role.setParentId(parentId);
      repository.save(role);
      DATASET.add(role);
    }
    for (int i = 0, cnt = Byte.MAX_VALUE; i < cnt; i++) {
      Role role = new Role();
      role.setCode(RandomUtil.randomString(9));
      role.setName(RandomUtil.randomString(9));
      role.setInheritable(false);
      String parentId = RandomUtil.randomEle(DATASET).getId();
      role.setParentId(parentId);
      repository.save(role);
      DATASET.add(role);
    }
  }

  @Test
  void addRole() {
    final List<Role> dataset = repository.findAll();
    Role parent = dataset.parallelStream()
        .filter(Role::getInheritable)
        .findAny().get();

    AddRoleQuery query = new AddRoleQuery();
    query.setCode(RandomUtil.randomString(8));
    query.setName(RandomUtil.randomString(9));
    query.setInheritable(RandomUtil.randomBoolean());
    query.setParentId(parent.getId());
    service.addRole(query.into());

    Assertions.assertEquals(dataset.size() + 1, repository.count());
  }

  @Test
  void addRole_not_inheritable() {
    final List<Role> dataset = repository.findAll();
    Role parent = dataset.parallelStream()
        .filter((o) -> !o.getInheritable())
        .findAny().get();

    AddRoleQuery query = new AddRoleQuery();
    query.setCode(RandomUtil.randomString(8));
    query.setName(RandomUtil.randomString(9));
    query.setInheritable(RandomUtil.randomBoolean());
    query.setParentId(parent.getId());
    Assertions.assertThrows(ClientException.class, () ->
        service.addRole(query.into())
    );
  }

  @Test
  void addRole_repeat() {
    final List<Role> dataset = repository.findAll();
    Role exist = dataset.parallelStream()
        .filter((o) -> !o.getInheritable())
        .findAny().get();

    AddRoleQuery query = new AddRoleQuery();
    query.setCode(exist.getCode());
    query.setName(RandomUtil.randomString(9));
    query.setInheritable(RandomUtil.randomBoolean());
    query.setParentId("");
    Assertions.assertThrows(ClientException.class, () ->
        service.addRole(query.into())
    );
  }

  @Test
  void removeRole() {
    final List<Role> dataset = repository.findAll();
    Role exist = dataset.parallelStream()
        .findAny().get();
    service.removeRole(exist.getId());
    List<Role> toRemove = Collections.singletonList(exist);
    while (!toRemove.isEmpty()) {
      dataset.remove(exist);
      Set<String> parentIds = toRemove.parallelStream()
          .map(Role::getId)
          .collect(Collectors.toSet());
      toRemove = dataset.parallelStream()
          .filter((o) -> parentIds.contains(o.getParentId()))
          .toList();
    }
    Assertions.assertLinesMatch(
        dataset.parallelStream().map(Role::getId).toList(),
        repository.findAll().parallelStream().map(Role::getId).toList()
    );
  }
}
