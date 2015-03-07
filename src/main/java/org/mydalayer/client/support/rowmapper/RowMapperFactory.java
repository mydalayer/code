package org.mydalayer.client.support.rowmapper;


import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.util.Date;

/**
 * 描述：映射类型工厂
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class RowMapperFactory<T> {
  private Class<T> requiredType;

  public RowMapperFactory(Class<T> requiredType) {
    this.requiredType = requiredType;
  }

  /**
   * 获取Spring RowMapper实现.
   * 
   * @param @return
   * @return RowMapper实现
   */
  public RowMapper<T> getRowMapper() {
    if (requiredType.equals(String.class) || Number.class.isAssignableFrom(requiredType)
        || requiredType.equals(Date.class)) {
      return new SingleColumnRowMapper<T>(requiredType);
    } else {
      return new BeanPropertyRowMapper<T>(requiredType);
    }
  }
}
