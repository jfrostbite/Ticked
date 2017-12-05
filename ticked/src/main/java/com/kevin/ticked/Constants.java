package com.kevin.ticked;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2017/10/31.
 */

public interface Constants {

    //重选标记
    int REELECT = 5;
    //提交标记
    int SUBMIT_FLAG = 100;
    //分割标记
    String CONFIG_SPLIT = "&&";
    int DB_VERSION = 1;
    String CONFIG_DB = "ticked.db";
    String CONFIG_JSON = "config.json";
    String CONFIG_PATH = "/assets/ticked_config/";
    String CONFIG_SD_PATH = new File(Environment.getExternalStorageDirectory(), "ticked_config").getPath();
    String CONFIG_SD_DB = new File(CONFIG_SD_PATH, CONFIG_DB).getPath();

    String TAB_NAME = "POINT_RANGE_";
    String POINT_ID = "_id";
    String POINT_TITLE = "tag";
    String POINT_LOC = "loc";
    String POINT_MINX = "minX";
    String POINT_MINY = "minY";
    String POINT_MAXX = "maxX";
    String POINT_MAXY = "maxY";
    String POINT_PAGE = "page";

    String POINT_SET_PX = "setPX";
    String POINT_SET_PY = "setPY";
    String POINT_GET_PX = "getPX";
    String POINT_GET_PY = "getPY";

    String DB_CREATE = "create table '" + TAB_NAME + "' ('"
            + POINT_ID + "' integer primary key autoincrement unique, '"
            + POINT_TITLE + "' text, '"
            + POINT_LOC + "' integer, '"
            + POINT_MINX + "' text, '"
            + POINT_MINY + "' text, '"
            + POINT_MAXX + "' text, '"
            + POINT_MAXY + "' text, '"
            + POINT_PAGE + "' integer);";
}
