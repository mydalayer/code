package org.mydalayer.pagination.dialect;

/**
 * 分页SQL方言接口
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public interface Dialect {

	public String getLimitSqlString(String sql, int offset, int limit);

	public String getCountSqlString(String sql);

}
