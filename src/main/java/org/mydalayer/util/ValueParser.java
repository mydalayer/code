package org.mydalayer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;


/**
 * 对象解析
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class ValueParser {
  private static Logger logger = LoggerFactory.getLogger(ValueParser.class);

  /**
   * 解析方法.<br>
   * 根据实体类中的Column注解转为map
   * 
   * @param entity 转换对象
   * @return 属性-值Map
   */
  public static Map<String, Object> parser(Object entity) {
    Map<String, Object> values = new HashMap<String, Object>();
    Method[] methods = entity.getClass().getMethods();
    for (Method method : methods) {
      if (method.isAnnotationPresent(Column.class)) {
        Column column = method.getAnnotation(Column.class);
        PropertyDescriptor descriptor = BeanUtils.findPropertyForMethod(method);
        String key = descriptor.getName();
        Object value = null;
        try {
          value = method.invoke(entity, new Object[] {});
          if (value instanceof java.util.Date) {
            value = dateFormat(column, (Date) value);
          }
        } catch (Exception e) {
          logger.debug("reflect error.[" + method + "]", e);
        }
        values.put(key, value);
      }
    }

    return values;
  }

  /**
   * 日期类型属性转换.<br>
   * 根据注解的格式
   * 
   * @param column 属性
   * @param date 属性值
   * @return Object
   */
  private static Object dateFormat(Column column, Date date) {
    if (date != null && !"".equals(column.columnDefinition())) {
      SimpleDateFormat format = new SimpleDateFormat(column.columnDefinition());
      return format.format(date);
    }
    return date;
  }
}
