package com.is.bookrecommender.dto;

import java.util.ArrayList;
import java.util.List;

public class PageResponseDto<T> {
    private List<T> content = new ArrayList<>();
    private boolean last;
    private Integer totalPage;

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }
}
