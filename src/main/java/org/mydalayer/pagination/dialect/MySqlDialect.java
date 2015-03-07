package org.mydalayer.pagination.dialect;

/**
 * MYSQL分页查询实现
 *
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class MySqlDialect implements IDialect {

  private static final String COUNT_ALIAS = "_COUNT_BLOCK_";

  @Override
  public String getLimitSqlString(String sql, int offset, int limit) {
    StringBuilder sb = new StringBuilder(sql.length() + 20);
    sb.append(sql);
    if (offset > 0) {
      sb.append(" LIMIT ").append(offset).append(',').append(limit);
    } else {
      sb.append(" LIMIT ").append(limit);
    }
    return sb.toString();
  }

  @Override
  public String getCountSqlString(String sql) {
    StringBuilder sb = new StringBuilder(sql.length() + 20);
    sb.append("SELECT COUNT(1) FROM (");
    sb.append(sql);
    sb.append(") ");
    sb.append(COUNT_ALIAS);
    return sb.toString();
  }

}
