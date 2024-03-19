package io.github.xezzon.geom.role.repository;

import io.github.xezzon.geom.role.domain.Role;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

  Optional<Role> findByCode(String code);

  List<Role> findByParentIdIn(Collection<String> parentIds);
}
