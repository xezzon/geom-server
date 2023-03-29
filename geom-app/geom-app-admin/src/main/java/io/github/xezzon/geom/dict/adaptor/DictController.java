package io.github.xezzon.geom.dict.adaptor;

import io.github.xezzon.geom.dict.domain.Dict;
import io.github.xezzon.tao.retrieval.CommonQuery;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xezzon
 */
@RestController
@RequestMapping("/dict")
public class DictController {

  /**
   * 分页查询字典目
   */
  @GetMapping("/tag")
  public Page<Dict> dictTagPage(CommonQuery params) {
    return Page.empty();
  }

  /**
   * 新增字典/字典目
   * @param dict 字典信息
   */
  @PostMapping("")
  public void addDict(Dict dict) {

  }

  /**
   * 查询字典目下的字典集合
   */
  @GetMapping("/{tag}")
  public List<Dict> dictListByTag(@PathVariable String tag) {
    return Collections.emptyList();
  }

  /**
   * 通过字典目与字典码查询字典
   * @param tag 字典目
   * @param code 字典码
   * @return 字典信息
   */
  @GetMapping("/{tag}/{code}")
  public Dict dictByTagAndCode(@PathVariable String tag, @PathVariable String code) {
    return null;
  }
}
