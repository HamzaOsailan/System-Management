package com.sahab.demo.response;

import java.util.List;

public class ApiResponse<T> {

    private List<T> data;
    private int page;
    private int size;
    private long totalElements;

    public ApiResponse(List<T> data, int page, int size, long totalElements) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
    }

    public List<T> getData() {
        return data;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }
}