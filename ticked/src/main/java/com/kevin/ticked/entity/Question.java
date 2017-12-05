package com.kevin.ticked.entity;


import com.kevin.ticked.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/1.
 */

public class Question extends TickedTag {
    private final String[] ANSWERS = {"A","B","C","D","E"};

    private List<String> answerList = new ArrayList<>();
//    private List<TickedStroke> strokes;

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Question) {
            Question question = (Question) obj;
            return title.equals(question.title);
        }
        return false;
    }

    /**
     * 更新loc确定答案
     * @param append 是否添加答案，判定多选
     */
    @Override
    public void reply(boolean append) {
       /* if (!append) {
            answerList = new ArrayList<>();
        } else {
        }*/
        if (loc == Constants.REELECT) {
            answerList.clear();
        } else {
            answerList.add(ANSWERS[loc]);
        }
    }

    @Override
    public List<String> answer() {
        return answerList;
    }
}
