package org.mydalayer.pagination;

import java.util.List;

/**
 * 封装分页查询结果
 * 
 * @author mydalayer#gmail.com
 * @version 1.0.0
 */
public class PaginationResult<T> {

  private final List<T> result;

  private final int count;

  public PaginationResult(List<T> result, int count) {
    super();
    this.result = result;
    this.count = count;
  }

  public List<T> getResult() {
    return result;
  }

  public int getCount() {
    return count;
  }

}
