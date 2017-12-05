package com.kevin.ticked.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.RectF;

import com.kevin.ticked.entity.TickedTag;

/**
 * Created by Administrator on 2017/10/31.
 */

public abstract class DB {

    private DBHelper mHelper;
    final SQLiteDatabase mSQLiteDatabase;

    public DB(Context context){
        mHelper = new DBHelper(context);
        mSQLiteDatabase = mHelper.getWritableDatabase();
    }

    public DB(String path) {
        mSQLiteDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * 查询点loc
     * @param x
     * @param y
     * @return
     */
    public abstract TickedTag query(float x, float y, String... values);

    /**
     * 获取提交范围
     * @param pageIndex
     * @return
     */
    public abstract RectF query(int pageIndex);
}
