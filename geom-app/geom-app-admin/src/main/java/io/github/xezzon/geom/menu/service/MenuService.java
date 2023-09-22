package io.github.xezzon.geom.menu.service;

import io.github.xezzon.geom.menu.domain.Menu;
import java.util.List;

/**
 * @author xezzon
 */
public interface MenuService {

  /**
   * 获取菜单树
   * @param parentId 上级菜单ID
   * @return 菜单（树形结构）
   */
  List<Menu> menuTree(String parentId);
}