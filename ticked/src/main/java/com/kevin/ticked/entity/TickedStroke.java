package com.kevin.ticked.entity;

import java.util.List;

/**
 * Created by Administrator on 2017/10/31.
 */

public interface TickedStroke {

    Long getId();
    void setId(Long id);
    int getStrokeColor();
    void setStrokeColor(int strokeColor);
    Integer getPageIndex();
    void setPageIndex(Integer pageIndex);
    List<? extends TickedPoint> getPointList();
    void setPointList(List<? extends TickedPoint> points);
    void resetPointList();
    void delete();
    void refresh();
    void update();
}
