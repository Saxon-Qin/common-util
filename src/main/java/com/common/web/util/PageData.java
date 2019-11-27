package com.common.web.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页数据
 *
 * @version V1.0
 **/
public class PageData<T> {
    private int totalCount;
    private List<T> data;

    public int getTotalCount() {
        return totalCount;
    }

    public static <T> PageData<T> of(int totalCount) {
        return new PageData<T>().setTotalCount(totalCount);
    }

    public static <T> PageData<T> of(int totalCount, List<T> dataList) {
        return new PageData<T>().setTotalCount(totalCount).setData(dataList);
    }

    public PageData<T> setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public List<T> getData() {
        return Optional.ofNullable(data).orElse(new ArrayList<>(0));
    }

    public PageData<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    public <R> PageData<R> map(Function<T, R> function) {
        PageData<R> pageData = new PageData<>();
        pageData.setTotalCount(this.totalCount);
        if (null != data) {
            pageData.setData(data.stream().map(function).collect(Collectors.toList()));
        }
        return pageData;
    }

    public PageData<T> peek(Consumer<T> consumer) {
        data.forEach(consumer);
        return this;
    }

    @Override
    public String toString() {
        return "PageData{" +
                "totalCount=" + totalCount +
                ", data=" + data +
                '}';
    }
}
