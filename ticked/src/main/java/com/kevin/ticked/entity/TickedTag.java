package com.kevin.ticked.entity;

import java.util.List;

/**
 * Created by Administrator on 2017/11/1.
 */

public abstract class TickedTag {
    public String title;
    public int loc, page;
    public abstract void reply(boolean append);
    public abstract List<String> answer();
}
