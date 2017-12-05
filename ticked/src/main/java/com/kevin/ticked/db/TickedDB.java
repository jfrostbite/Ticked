package com.kevin.ticked.db;

import android.content.Context;
import android.database.Cursor;
import android.graphics.RectF;

import com.kevin.ticked.Constants;
import com.kevin.ticked.entity.Question;
import com.kevin.ticked.entity.TickedTag;


/**
 * Created by Administrator on 2017/10/31.
 */

public class TickedDB extends DB {

    public TickedDB(Context context) {
        super(context);
    }

    /**
     * 打开数据库
     *
     * @param path
     */
    public TickedDB(String path) {
        super(path);
    }

    @Override
    public TickedTag query(float x, float y, String... values) {
        int loc = -1;
        TickedTag tag = null;
        if (mSQLiteDatabase != null) {
            Cursor cursor = mSQLiteDatabase.query(Constants.TAB_NAME, new String[]{}, Constants.POINT_TITLE + "= ? and " + Constants.POINT_PAGE + "= ?", values, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                float minX = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MINX));
                float minY = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MINY));
                float maxX = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MAXX));
                float maxY = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MAXY));
                if (new RectF(minX, minY, maxX, maxY).contains(x, y)) {
                    tag = new Question();
                    tag.title = values[0];
                    tag.page = Integer.parseInt(values[1]);
                    tag.loc = cursor.getInt(cursor.getColumnIndex(Constants.POINT_LOC));
                    break;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return tag;
    }

    @Override
    public RectF query(int pageIndex) {
        RectF rectF = null;
        if (mSQLiteDatabase != null) {
            Cursor cursor = mSQLiteDatabase.query(Constants.TAB_NAME, new String[]{}, Constants.POINT_PAGE + "= ? and " + Constants.POINT_LOC + "= ?", new String[]{String.valueOf(pageIndex), String.valueOf(Constants.SUBMIT_FLAG)}, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                float minX = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MINX));
                float minY = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MINY));
                float maxX = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MAXX));
                float maxY = cursor.getFloat(cursor.getColumnIndex(Constants.POINT_MAXY));
                rectF = new RectF(minX, minY, maxX, maxY);
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return rectF;
    }
}
