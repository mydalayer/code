package org.mydalayer.parsing.builder;

import org.mydalayer.client.support.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public abstract class BaseBuilder {
  protected final Configuration configuration;

  public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * String "true"/"false" to Boolean Obj.<br>
   * 
   * @return Boolean
   */
  protected Boolean booleanValueOf(String value, Boolean defaultValue) {
    return value == null ? defaultValue : Boolean.valueOf(value);
  }

  /**
   * String number to Integer Obj.<br>
   * 
   * @return Integer
   */
  protected Integer integerValueOf(String value, Integer defaultValue) {
    return value == null ? defaultValue : Integer.valueOf(value);
  }

  /**
   * String str split by "," to Set Obj.<br>
   * 
   * @return Set type String
   */
  protected Set<String> stringSetValueOf(String value, String defaultValue) {
    String value1 = value == null ? defaultValue : value;
    return new HashSet<String>(Arrays.asList(value1.split(",")));
  }

}
