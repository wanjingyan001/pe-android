package com.framework.util;

public class SearchInfo {
    String search;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    int pageNum;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public SearchInfo(String search) {
        this.search = search;
    }

    public SearchInfo() {
    }
}