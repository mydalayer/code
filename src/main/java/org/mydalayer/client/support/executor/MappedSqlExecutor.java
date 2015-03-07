package org.mydalayer.client.support.executor;

import org.mydalayer.client.support.Configuration;
import org.mydalayer.client.support.MappedStatement;
import org.mydalayer.client.support.rowmapper.RowMapperFactory;
import org.mydalayer.util.DalUtils;
import org.mydalayer.util.ValueParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.GenericStoredProcedure;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * sql执行器.<br>
 * 最终调用NamedParameterJdbcTemplate中的数据操作方法
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class MappedSqlExecutor extends JdbcTemplate {

  // sql执行超过设置时间后，日志中打印的字符串
  public static final String SQL_AUDIT_LOGMESSAGE = "SQL Statement [{}] with parameter object [{}] "
      + "ran out of the normal time range, it consumed [{}] milliseconds.";

  private static Logger logger = LoggerFactory.getLogger(MappedSqlExecutor.class);

  protected Configuration configuration;

  protected NamedParameterJdbcTemplate execution = new NamedParameterJdbcTemplate(this);

  private String databaseUrl;
  private String databaseUserName;

  private String logPrefix;

  public DataSource getDataSource() {
    return super.getDataSource();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public String getLogPrefix() {
    return logPrefix;
  }

  public void setLogPrefix(String logPrefix) {
    this.logPrefix = logPrefix;
  }

  public String getDatabaseUrl() {
    return databaseUrl;
  }

  public String getDatabaseUserName() {
    return databaseUserName;
  }

  /**
   * 插入操作..<br>
   * 
   * @param entity 返回值: 数值类型
   * @return Number
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public Number persist(Object entity) {
    return persist(entity, Number.class);
  }

  /**
   * 插入操作.<br>
   * 输入参数：实体对象，主键类型
   * 
   * @param entity ,requiredType 返回值: 主键类型
   * @return T
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  @SuppressWarnings("unchecked")
  public <T> T persist(Object entity, Class<T> requiredType) {
    String insertSql = null;
    Map<String, Object> paramMap = null;

    try {
      Class<? extends Object> entityClass = entity.getClass();
      String sqlId = entityClass.getName() + ".insert";
      // 根据sqlId获取对应的sql描述
      MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
      // 将mappedStatement中的其他参数配置到JdbcTemplate
      this.applyStatementSettings(mappedStatement);
      // 将实体对象转换为map
      paramMap = ValueParser.parser(entity);
      insertSql = mappedStatement.getBoundSql(paramMap);
      KeyHolder keyHolder = new GeneratedKeyHolder();

      logMessage("persist", insertSql, paramMap);
      // 使用默认数据库来查询序列
      if (mappedStatement.getKeyGenerator() != null) { // 支持序列
        Object seq = queryBySequence(mappedStatement.getKeyGenerator(), false);
        paramMap.put(mappedStatement.getIdProperty(), seq);
      }

      if (mappedStatement.getIsGenerator()) {
        execution.update(insertSql, new MapSqlParameterSource(paramMap), keyHolder);
      } else {
        execution.update(insertSql, new MapSqlParameterSource(paramMap));
      }

      Object key = paramMap.get(mappedStatement.getIdProperty());
      if (key == null || (key instanceof Number && ((Number) key).doubleValue() == 0.0d)) {
        DalUtils.setProperty(entity, mappedStatement.getIdProperty(), keyHolder.getKey());
        key = keyHolder.getKey();
      }
      logMessage("persist", insertSql, paramMap);
      return (T) key;
    } catch (Exception e) {
      throwException(e);
      return null;// never return
    }

  }

  /**
   * 查询序列.
   * 
   * @param needUpdate ：DB2为false；MySql为true
   * @return Object
   */
  public Object queryBySequence(String sequence, boolean needUpdate) {
    if (needUpdate) {
      execution.update(sequence, new HashMap<String, Object>());
      Map<String, Object> result =
          execution.queryForMap("select last_insert_id() as seq", new HashMap<String, Object>());
      return result.get("seq");
    }
    Map<String, Object> result = execution.queryForMap(sequence, new HashMap<String, Object>());
    return result.get("1");// 获取第一列
  }

  /**
   * 修改操作.<br>
   * 输入参数：实体对象
   * 
   * @param entity 返回值: 执行成功的记录条数
   * @return int
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public int merge(Object entity) {
    Map<String, Object> paramMap = null;
    String updateSql = null;

    try {
      Class<? extends Object> entityClass = entity.getClass();
      String sqlId = entityClass.getName() + ".update";
      MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(mappedStatement);
      paramMap = ValueParser.parser(entity);
      updateSql = mappedStatement.getBoundSql(paramMap);
      logMessage("merge", updateSql, paramMap);
      int result = execution.update(updateSql, paramMap);
      logMessage("merge", updateSql, paramMap);
      return result;
    } catch (Exception e) {
      throwException(e);
      return 0;// never return
    }
  }

  /**
   * 动态修改操作.<br>
   * 输入参数：实体对象
   * 
   * @param entity 返回值: 执行成功的记录条数
   * @return int
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public int dynamicMerge(Object entity) {
    Map<String, Object> paramMap = null;
    String updateSql = null;
    try {
      Class<? extends Object> entityClass = entity.getClass();
      String sqlId = entityClass.getName() + ".updateDynamic";
      MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(mappedStatement);

      paramMap = ValueParser.parser(entity);
      updateSql = parserDynamicmergesql(mappedStatement.getBoundSql(paramMap));
      logMessage("dynamicMerge", updateSql, paramMap);
      int result = execution.update(updateSql, paramMap);
      logMessage("dynamicMerge", updateSql, paramMap);
      return result;
    } catch (Exception e) {
      throwException(e);
      return 0;// never return
    }

  }

  /**
   * 除去SQL语句中where前的逗号.<br>
   */
  public String parserDynamicmergesql(String sql) {
    String[] split = sql.split("WHERE");
    String sql2 = split[1];
    String sql1 = split[0];
    StringBuffer sb = new StringBuffer(sql1.trim());
    sb.deleteCharAt(sb.length() - 1);
    String newSql = sb.toString() + " WHERE " + sql2;
    return newSql;
  }

  /**
   * 删除操作.<br>
   * 输入参数：实体对象
   * 
   * @param entity 返回值: 执行成功的记录条数
   * @return int
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public int remove(Object entity) {

    Map<String, Object> paramMap = null;
    String removeSql = null;

    try {
      Class<? extends Object> entityClass = entity.getClass();
      String sqlId = entityClass.getName() + ".delete";
      MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(mappedStatement);

      paramMap = ValueParser.parser(entity);
      // processTableRoute(paramMap);
      removeSql = mappedStatement.getBoundSql(paramMap);
      logMessage("remove", removeSql, paramMap);
      int result = execution.update(removeSql, paramMap);
      logMessage("remove", removeSql, paramMap);

      return result;
    } catch (Exception e) {

      throwException(e);
      return 0;// never return
    }

  }

  /**
   * 查询操作.<br>
   * 输入参数：实体类型，实体类
   * 
   * @param entityClass ,entity 返回值: 实体类型
   * @return T
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public <T> T find(Class<T> entityClass, Object entity) {

    Map<String, Object> paramMap = null;
    String selectSql = null;

    try {
      String sqlId = entityClass.getName() + ".select";
      MappedStatement mappedStatement = configuration.getMappedStatement(sqlId, true);

      paramMap = ValueParser.parser(entity);
      selectSql = mappedStatement.getBoundSql(paramMap);

      logMessage("find", selectSql, paramMap);
      List<T> resultList = execution.query(selectSql, paramMap, new RowMapperFactory<T>(entityClass).getRowMapper());
      logMessage("find", selectSql, paramMap);

      return singleResult(resultList);
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }
  }

  /**
   * 根据sql查询对象.<br>
   * 输入参数：sqlId，实体参数，实体类型
   * 
   * @param sqlId , param，requiredType 返回值: 实体类型
   * @return T
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public <T> T queryForObject(String sqlId, Object param, Class<T> requiredType) {
    // 实体参数会先转为map，再调用重载方法
    return this.queryForObject(sqlId, DalUtils.convertToMap(param), requiredType);
  }

  /**
   * 根据sql查询对象.<br>
   * 输入参数：sqlId，map类型的参数，实体类型
   * 
   * @param sqlId , paramMap, requiredType 返回值: 实体类型
   * @return T
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public <T> T queryForObject(String sqlId, Map<String, Object> paramMap, Class<T> requiredType) {
    // 实体参数会先转为map，实体类型转为实体映射，再调用重载方法
    return this.queryForObject(sqlId, paramMap, new RowMapperFactory<T>(requiredType).getRowMapper());
  }

  /**
   * 根据sql查询对象.<br>
   * 输入参数：sqlId，实体对象，实体映射
   * 
   * @param sqlId , param, rowMapper 返回值: 实体类型
   * @return T
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public <T> T queryForObject(String sqlId, Object param, RowMapper<T> rowMapper) {
    // 实体类型转为实体映射，再调用重载方法
    return this.queryForObject(sqlId, DalUtils.convertToMap(param), rowMapper);
  }

  /**
   * 根据sql查询对象.<br>
   * 输入参数：sqlId，map类型的参数，实体映射
   * 
   * @param sqlId , paramMap, rowMapper 返回值: 实体类型
   * @return T
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public <T> T queryForObject(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper) {

    String sql = null;

    try {
      MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(stmt);
      sql = stmt.getBoundSql(paramMap);
      logMessage("queryForObject", sql, paramMap);
      List<T> resultList = execution.query(sql, paramMap, rowMapper);
      logMessage("queryForObject", sql, paramMap);

      return singleResult(resultList);
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }
  }

  /**
   * 根据sql查询map.<br>
   * 输入参数：sqlId，实体对象
   * 
   * @param sqlId ,param 返回值: Map类型
   * @return Map对象
   */
  public Map<String, Object> queryForMap(String sqlId, Object param) {
    return this.queryForMap(sqlId, DalUtils.convertToMap(param));
  }

  /**
   * 根据sql查询map.<br>
   * 
   * @param sqlId ,paramMap 返回值: Map类型
   * @return Map对象
   */
  public Map<String, Object> queryForMap(String sqlId, Map<String, Object> paramMap) {
    String sql = null;

    try {
      MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(stmt);
      sql = stmt.getBoundSql(paramMap);
      logMessage("queryForMap", sql, paramMap);
      Map<String, Object> map = singleResult(execution.queryForList(sql, paramMap));
      logMessage("queryForMap", sql, paramMap);

      return map;
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }

  }

  /**
   * 根据sql查询list，其中list中元素类型自由指定.<br>
   * 输入参数：sqlId，实体对象，实体类型
   * 
   * @param sqlId ,param，requiredType 返回值: list类型，元素为实体类型
   * @return 对象List
   */
  public <T> List<T> queryForList(String sqlId, Object param, Class<T> requiredType) {
    return queryForlist(sqlId, DalUtils.convertToMap(param), requiredType);
  }

  /**
   * 根据sql查询list，其中list中元素类型自由指定.<br>
   * 输入参数：sqlId，map类型的参数，实体类型
   * 
   * @param sqlId ,paramMap，requiredType 返回值: list类型，元素为实体类型
   * @return 对象List
   */
  public <T> List<T> queryForlist(String sqlId, Map<String, Object> paramMap, Class<T> requiredType) {
    return this.queryForlist(sqlId, paramMap, new RowMapperFactory<T>(requiredType).getRowMapper());
  }

  /**
   * 根据sql查询list，其中list中元素类型自由指定.<br>
   * 输入参数：sqlId，实体对象，实体映射
   * 
   * @param sqlId ,param，rowMapper 返回值: list类型，元素为实体类型
   * @return 对象List
   */
  public <T> List<T> queryForlist(String sqlId, Object param, RowMapper<T> rowMapper) {
    return queryForlist(sqlId, DalUtils.convertToMap(param), rowMapper);
  }

  /**
   * 根据sql查询list，其中list中元素类型自由指定.<br>
   * 输入参数：sqlId，map类型的参数，实体映射
   * 
   * @param sqlId ,paramMap，rowMapper 返回值: list类型，元素为实体类型
   * @return 对象List
   */
  public <T> List<T> queryForlist(String sqlId, Map<String, Object> paramMap, RowMapper<T> rowMapper) {

    String sql = null;

    try {
      MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(stmt);
      sql = stmt.getBoundSql(paramMap);

      logMessage("queryForList(3 paramter)", sql, paramMap);
      List<T> list = execution.query(sql, DalUtils.mapIfnull(paramMap), rowMapper);
      logMessage("queryForList(3 paramter)", sql, paramMap);

      return list;
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }

  }

  /**
   * 根据sql查询list，其中list中元素为map类型.<br>
   * 输入参数：sqlId，实体对象
   * 
   * @param sqlId ,param 返回值: list类型，元素为map类型
   * @return Map对象List
   */
  public List<Map<String, Object>> queryForlist(String sqlId, Object param) {
    return queryForlist(sqlId, DalUtils.convertToMap(param));
  }

  /**
   * 根据sql查询list，其中list中元素为map类型.<br>
   * 输入参数：sqlId，map类型的参数
   * 
   * @param sqlId ,paramMap 返回值: list类型，元素为map类型
   * @return Map对象List
   */
  public List<Map<String, Object>> queryForlist(String sqlId, Map<String, Object> paramMap) {

    String sql = null;

    try {
      MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(stmt);
      sql = stmt.getBoundSql(paramMap);

      logMessage("queryForList(2 paramter)", sql, paramMap);
      List<Map<String, Object>> list = execution.queryForList(sql, DalUtils.mapIfnull(paramMap));
      logMessage("queryForList(2 paramter)", sql, paramMap);

      return list;
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }

  }

  /**
   * 根据sql执行自定义操作，返回成功记录数.<br>
   * 输入参数：sqlId，实体参数
   * 
   * @param sqlId ,param 返回值: 执行成功的记录条数
   * @return int
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public int execute(String sqlId, Object param) {
    return this.execute(sqlId, DalUtils.convertToMap(param));
    // return this.execute(sqlId, ValueParser.parser(param));
  }

  /**
   * 根据sql执行自定义操作，返回成功记录数.<br>
   * 输入参数：sqlId，map类型的参数
   * 
   * @param sqlId ,paramMap 返回值: 执行成功的记录条数
   * @return int
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public int execute(String sqlId, Map<String, Object> paramMap) {
    return this.execute4PrimaryKey(sqlId, paramMap, null).intValue();
  }

  /**
   * 根据sql执行自定义操作，返回主键值.<br>
   * 输入参数：sqlId，map类型的参数
   * 
   * @param sqlId ,paramMap 返回值: 主键值
   * @return Numer
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public Number execute4PrimaryKey(String sqlId, Map<String, Object> paramMap) {
    return this.execute4PrimaryKey(sqlId, paramMap, new GeneratedKeyHolder());
  }

  /**
   * 根据sql执行自定义操作，返回成功记录数.<br>
   * 输入参数：sqlId，map类型的对象，keyHoler对象
   * 
   * @param sqlId ,paramMap，keyHolder 返回值: 执行成功的记录条数
   * @return Number
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  private Number execute4PrimaryKey(String sqlId, Map<String, Object> paramMap, KeyHolder keyHolder) {

    String sql = null;

    try {
      MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(stmt);
      sql = stmt.getBoundSql(paramMap);
      int result = 0;
      if (keyHolder != null) {
        execution.update(sql, new MapSqlParameterSource(DalUtils.mapIfnull(paramMap)), keyHolder);
        logMessage("execute", sql, paramMap);
        result = keyHolder.getKey() == null ? 0 : keyHolder.getKey().intValue();
      } else {
        result = execution.update(sql, DalUtils.mapIfnull(paramMap));
        logMessage("execute", sql, paramMap);

      }

      return result;
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }
  }

  /**
   * 根据sql批量操作.<br>
   * 输入参数：sqlId，map类型的对象数组
   * 
   * @param sqlId , batchValues 返回值: 执行成功的记录条数
   * @return int[]
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public int[] batchUpdate(String sqlId, Map<String, Object>[] batchValues) {

    String sql = null;

    try {
      Map<String, Object> paramMap = new HashMap<String, Object>();
      if (batchValues != null && batchValues.length != 0 && batchValues[0] != null) {
        paramMap = batchValues[0];
      }
      MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(stmt);
      sql = stmt.getBoundSql(paramMap);
      logMessage("batchUpdate", sql, String.valueOf(batchValues == null ? 0 : batchValues.length));
      int[] result = execution.batchUpdate(sql, batchValues);
      logMessage("batchUpdate", sql, String.valueOf(batchValues == null ? 0 : batchValues.length));

      return result;
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }

  }

  /**
   * 存储过程调用.<br>
   * 存储过程调用时，需要加上schema.<br>
   * 输入参数：sqlId，map类型的参数，list集合（元素为SqlParameter类型）
   * 
   * @param sqlId , paramMap，sqlParameters 返回值: map类型
   * @return Map执行结果
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  public Map<String, Object> call(String sqlId, Map<String, Object> paramMap, List<SqlParameter> sqlParameters) {
    String sql = null;

    try {
      Map<String, Object> paramMapTmp = DalUtils.mapIfnull(paramMap);
      MappedStatement stmt = configuration.getMappedStatement(sqlId, true);
      this.applyStatementSettings(stmt);
      sql = stmt.getBoundSql(paramMap);

      logMessage("call procedure", sql, paramMapTmp);
      GenericStoredProcedure storedProcedure = new GenericStoredProcedure();
      storedProcedure.setJdbcTemplate(this);
      storedProcedure.setSql(sql);
      for (SqlParameter sqlParameter : sqlParameters) {
        storedProcedure.declareParameter(sqlParameter);
      }
      logMessage("call", sql, paramMapTmp);
      Map<String, Object> result = storedProcedure.execute(paramMapTmp);

      return result;
    } catch (Exception e) {

      throwException(e);
      return null;// never return
    }

  }

  /**
   * 日志信息
   */
  protected void logMessage(String method, String sql, Object object) {
    if (logger.isDebugEnabled()) {
      String target = this.logPrefix == null ? "" : "execute the sql in " + this.logPrefix;
      logger.debug(method + " method {} SQL:{}", target, sql);
      logger.debug(method + " method {} parameter:[{}]", target, object);
    }
  }

  /**
   * 将MappedStatement中的其他参数配置到JdbcTemplate
   */
  protected void applyStatementSettings(MappedStatement stmt) {
    int fetchSize = stmt.getFetchSize();
    if (fetchSize > 0) {
      this.setFetchSize(fetchSize);
    }
    int timeout = stmt.getTimeout() > 0 ? stmt.getTimeout() : configuration.getDefaultStatementTimeout();
    this.setQueryTimeout(timeout);
    int maxRows = stmt.getMaxRows();
    if (maxRows > 0) {
      this.setMaxRows(maxRows);
    }
  }

  /**
   * 单一结果集.<br>
   * 如果结果集中存在多条记录，返回第一条.<br>
   * 输入参数：list集合，元素为T
   * 
   * @param resultList 返回值: T
   * @return T
   * @throw 异常描述
   * @see 需要参见的其它内容
   */
  private <T> T singleResult(List<T> resultList) {
    if (resultList != null) {
      int size = resultList.size();
      if (size > 0) {
        if (logger.isDebugEnabled() && size > 1) {
          logger.debug("Incorrect result size: expected " + 1 + ", actual " + size + " return the first element.");
        }
        return resultList.get(0);
      }
      if (size == 0) {
        if (logger.isDebugEnabled()) {
          logger.debug("Incorrect result size: expected " + 1 + ", actual " + size);
        }
        return null;
      }
    }
    return null;
  }

  protected void throwException(Exception e) {
    if (e instanceof DataAccessException) {
      throw (DataAccessException) e;
    } else if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    } else {
      throw new RuntimeException(e);
    }

  }

}
