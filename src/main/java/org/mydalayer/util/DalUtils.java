package org.mydalayer.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * DAL工具类.
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class DalUtils {

  private static final int NUM0 = 0;
  private static final int NUM1 = 1;
  private static final int NUM2 = 2;
  private static final int NUM3 = 3;
  private static final int NUM4 = 4;
  private static final int NUM8 = 8;
  private static final int NUM16 = 16;
  private static final int NUM24 = 24;
  private static final int NUM_0XFF = 0xFF;

  private static Logger LOGGER = LoggerFactory.getLogger(DalUtils.class);

  /**
   * 把参数转成对象数组.
   * 
   * @param parameter 输入对象
   * @return Object数组
   */
  @Deprecated
  public static Object[] convertToObjectArray(Object parameter) {
    Object[] retObject = null;
    if (parameter instanceof Object[]) {
      retObject = (Object[]) parameter;
    } else {
      retObject = new Object[] {parameter};
    }
    return retObject;
  }

  public static Map<String, Object> convertToMap(Object arg) {
    return new HashMap<String, Object>();

  }

  /**
   * 判断map是否为空.
   * 
   * @param map 目标map
   */
  public static Map<String, Object> mapIfnull(Map<String, Object> map) {
    if (map == null) {
      return new HashMap<String, Object>();
    }
    return map;
  }

  /**
   * 反射设置对象属性的值.
   * 
   * @param targetObject 目标对象
   * @param propertyName 目标属性
   * @param propertyValue 目标值
   */
  public static void setProperty(Object targetObject, String propertyName, Object propertyValue) {
    try {
      Field field = findField(targetObject.getClass(), propertyName);
      if (field == null) {
        throw new IllegalArgumentException(propertyName + " can not be found.");
      }
      field.setAccessible(true);
      if (propertyValue instanceof BigDecimal) {
        if (field.getType().isAssignableFrom(Integer.class) || field.getType().isAssignableFrom(int.class)) {
          field.set(targetObject, ((BigDecimal) propertyValue).intValue());
        }
        if (field.getType().isAssignableFrom(Long.class) || field.getType().isAssignableFrom(long.class)) {
          field.set(targetObject, ((BigDecimal) propertyValue).longValue());
        }
        if (field.getType().isAssignableFrom(Double.class) || field.getType().isAssignableFrom(double.class)) {
          field.set(targetObject, ((BigDecimal) propertyValue).doubleValue());
        }
        if (field.getType().isAssignableFrom(Float.class) || field.getType().isAssignableFrom(float.class)) {
          field.set(targetObject, ((BigDecimal) propertyValue).floatValue());
        }
        if (field.getType().isAssignableFrom(Byte.class) || field.getType().isAssignableFrom(byte.class)) {
          field.set(targetObject, ((BigDecimal) propertyValue).byteValue());
        }
      } else {
        field.set(targetObject, propertyValue);
      }
    } catch (SecurityException e) {
      LOGGER.warn(e.getMessage(), e);
    } catch (IllegalArgumentException e) {
      LOGGER.warn(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      LOGGER.warn(e.getMessage(), e);
    }
  }

  private static Field findField(Class<?> clazz, String name) {
    if (clazz == null) {
      throw new IllegalArgumentException("Class must not be null");
    }
    if (name == null) {
      throw new IllegalArgumentException("field must not be null");
    }
    Class<?> searchType = clazz;
    while (!Object.class.equals(searchType) && searchType != null) {
      Field[] fields = searchType.getDeclaredFields();
      for (Field field : fields) {
        if (name.equals(field.getName())) {
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  /**
   * 计算MD5.
   * 
   * @param str 输入字符串
   * @return byte[]
   */
  public static byte[] computeMd5(String str) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(str.getBytes("UTF-8"));
      return md5.digest();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 not supported", e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Unknown string :" + str, e);
    }
  }

  /**
   * 一致性HASH算法.
   */
  public static enum HashAlgorithm {
    /**
     * MD5-based hash algorithm used by ketama.
     */
    KETAMA_HASH;
    /**
     * hash操作.
     * 
     * @param digest 输入数组
     * @param ntime XX
     * @return HASH结果Long值
     */
    public long hash(byte[] digest, int ntime) {
      long rv =
          ((long) (digest[NUM3 + ntime * NUM4] & NUM_0XFF) << NUM24)
              | ((long) (digest[NUM2 + ntime * NUM4] & NUM_0XFF) << NUM16)
              | ((long) (digest[NUM1 + ntime * NUM4] & NUM_0XFF) << NUM8) | (digest[NUM0 + ntime * NUM4] & NUM_0XFF);

      return rv & 0xffffffffL; /* Truncate to 32-bits */
    }
  }
}
