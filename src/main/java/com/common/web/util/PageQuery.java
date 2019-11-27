package com.common.web.util;

/**
 * 分页查询对象
 *
 * @version V1.0
 **/
public class PageQuery<T> {
    private int pageNo;
    private int pageSize;

    private T query;

    public int getPageNo() {
        return pageNo;
    }

    public PageQuery<T> setPageNo(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public PageQuery<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public T getQuery() {
        return query;
    }

    public PageQuery<T> setQuery(T query) {
        this.query = query;
        return this;
    }

    @Override
    public String toString() {
        return "PageQuery{" +
                "pageNo=" + pageNo +
                ", pageSize=" + pageSize +
                ", query=" + query +
                '}';
    }
}
