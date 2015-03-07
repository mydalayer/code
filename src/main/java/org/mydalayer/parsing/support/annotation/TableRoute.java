package org.mydalayer.parsing.support.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 分表路由
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface TableRoute {

  /** 表名. */
  String tableName();

}
