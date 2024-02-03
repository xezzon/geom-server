package io.github.xezzon.geom.openapi.model;

import io.github.xezzon.geom.openapi.Openapi;
import io.github.xezzon.tao.trait.From;
import io.github.xezzon.tao.trait.Into;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 新增对外接口的请求
 * @author xezzon
 */
@Data
public class AddOpenapiQuery implements Into<Openapi> {

  @NotBlank
  private String name;
  @Pattern(regexp = "^[\\w-]+$")
  private String code;
  @NotBlank
  private String path;

  public Openapi into() {
    return Converter.INSTANCE.from(this);
  }

  @Mapper
  interface Converter extends From<AddOpenapiQuery, Openapi> {

    Converter INSTANCE = Mappers.getMapper(Converter.class);

    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleteTime", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Override
    Openapi from(AddOpenapiQuery query);
  }
}
