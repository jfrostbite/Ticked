package com.kevin.ticked;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kevin.ticked.db.TickedDB;
import com.kevin.ticked.entity.TickedTag;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Administrator on 2017/10/31.
 * 接受点线集合，直接操作缓存，不做本地处理
 * <p>
 * 简化模块集成难度。
 * <p>
 * 思路： 模块需要传入模块定义点线实体，第三方调用需要实现该操作，中间有点与点的转换操作，降低效率，增加集成难度
 * <p>
 * 解决以上思路，利用反射定义位置类
 */

public class Ticked {

    private ExecutorService mExecutor;
    private TickedDB mDB;
    //补偿点
    private float[] mCompensation = new float[2];
    //配置文件
    private List<TickedTag> mTags;
    private OnTickedListener mListener;
    //题集
    public List<TickedTag> mQues = new ArrayList<>();

    private TickedInterceptor<String, List<TickedTag>> mSaltInterceptor;
    private long mFirstTime;

    private Ticked(Builder builder) {
        mSaltInterceptor = builder.mSaltInterceptor;
        mExecutor = Executors.newSingleThreadExecutor();
        mListener = builder.mListener;
        config();

    }

    /**
     * 获取平均点
     */
    private float[] validPoint(List<? extends Object> notePoints) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (notePoints == null || notePoints.isEmpty()) {
            return null;
        }
//        notePoints = notePoints.subList(notePoints.size() / 3, notePoints.size());
        int size = notePoints.size()/* > 10 ? 10 : notePoints.size()*/;
        float[] pxs = new float[size];
        float[] pys = new float[size];
        for (int i = 0; i < size; i++) {
            int anInt = new Random().nextInt(notePoints.size());
            Object point = notePoints.get(anInt);
            Method getPx = point.getClass().getDeclaredMethod(Constants.POINT_GET_PX);
            Method getPy = point.getClass().getDeclaredMethod(Constants.POINT_GET_PY);
            pxs[i] = (float) getPx.invoke(point);
            pys[i] = (float) getPy.invoke(point);
        }

        Arrays.sort(pxs);
        Arrays.sort(pys);

        RectF rectF = new RectF(pxs[0], pys[0], pxs[size - 1], pys[size - 1]);
        rectF.sort();

        return new float[]{rectF.centerX(), rectF.centerY()};
    }

    /**
     * 配置文件
     */
    private Ticked config(List<TickedTag> config) {
        mTags = config;
        return this;
    }

    /**
     * 配置文件
     */
    private void config() {

        Future<String[]> future = mExecutor.submit(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                String[] strings = new String[2];
                byte[] bytes = openAssets(getClass().getResourceAsStream(Constants.CONFIG_PATH + Constants.CONFIG_JSON));
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
                String line = "";
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                strings[0] = sb.toString();
                //配置数据库文件
                File db = new File(Constants.CONFIG_SD_PATH);
                if (!db.exists()) {
                    if (!db.mkdirs()) {
                        throw new FileNotFoundException(Constants.CONFIG_PATH + " create failed");
                    }
                }
                File file = new File(Constants.CONFIG_SD_DB);
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        throw new FileNotFoundException(Constants.CONFIG_DB + " create failed");
                    }
                }
                //强行更新文件
                FileOutputStream fos = new FileOutputStream(file);
                bytes = openAssets(getClass().getResourceAsStream(Constants.CONFIG_PATH + Constants.CONFIG_DB));
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                int len = -1;
                byte[] temp = new byte[1024];
                while ((len = bais.read(temp)) > -1) {
                    fos.write(temp, 0, len);
                }
                bais.close();
                fos.close();
                strings[1] = file.getPath();
                return strings;
            }
        });

        try {
            String[] strings = future.get();
            if (mSaltInterceptor != null) {
                //解析数据
                config(mSaltInterceptor.salt(strings[0].split(Constants.CONFIG_SPLIT)));
            }
            mDB = new TickedDB(strings[1]);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TICKED", e.getMessage());
            if (mListener != null) {
                mListener.onError("config is Failure" + e.getMessage());
            }
        }
    }

    private byte[] openAssets(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = -1;
        byte[] bytes = new byte[1024];
        while ((len = is.read(bytes)) > -1) {
            baos.write(bytes, 0, len);
        }
        is.close();
        baos.close();
        return baos.toByteArray();
    }

    public Ticked dispose(@NonNull final List<? extends Object> stroke) {
        if (stroke.isEmpty()) {
            if (mListener != null) {
                mListener.onError("stroke does not Empty");
            }
            return this;
        }
        if (mFirstTime == 0) {
            mFirstTime = System.currentTimeMillis();
        }

        Future<TickedTag> tickedTagFuture = mExecutor.submit(new Callable<TickedTag>() {
            @Override
            public TickedTag call() throws Exception {
                float[] point = validPoint(stroke);
                TickedTag result = null;
                for (TickedTag tag : mTags) {
                    result = mDB.query(point[0], point[1], tag.title, String.valueOf(tag.page));
                    if (result != null) {//匹配到结果
                        return result;
                    }
                }
                return null;
            }
        });

        try {
            TickedTag tickedTag = tickedTagFuture.get();
            if (tickedTag != null) {
                match(tickedTag);
            } else {
                if (mListener != null) {
                    mListener.onError("error range");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onError(e.getLocalizedMessage());
            }
        }
        return this;
    }

    /**
     * 实时匹配
     * 10     115    41  121
     *
     * @param result
     */
    private void match(TickedTag result) {
        if (Constants.SUBMIT_FLAG == result.loc) {
            Collections.sort(mQues, new Comparator<TickedTag>() {
                @Override
                public int compare(TickedTag tickedTag, TickedTag t1) {
                    return Integer.parseInt(tickedTag.title) - Integer.parseInt(t1.title);
                }
            });
            if (mListener != null) {
                mListener.onCompleted(mQues, System.currentTimeMillis() - mFirstTime);
                clean();
            }
            return;
        }
        //查找该题目是否存在，如果存在更新答案即可
        int index = mQues.indexOf(result);
        if (index > -1) {
            mQues.get(index).loc = result.loc;
            result = mQues.get(index);
        }
        //作答
        result.reply(index > -1);
        if (index < 0) {//
            mQues.add(result);
        }
        if (mListener != null) {
            mListener.onTicked(result);
        }
    }

    public void clean() {
        mQues.clear();
        mFirstTime = 0;
    }

    public void disCharge() {
        clean();
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
    }

    public interface OnTickedListener {
        void onTicked(TickedTag tag);

        void onError(String msg);

        void onCompleted(List<TickedTag> tags, long l);
    }

    /**
     * 配置
     */
    public static class Builder {

        private TickedInterceptor<String, List<TickedTag>> mSaltInterceptor;
        private OnTickedListener mListener;

        public Builder addSalt(TickedInterceptor<String, List<TickedTag>> interceptor) {
            mSaltInterceptor = interceptor;
            return this;
        }

        public Builder addListener(OnTickedListener listener) {

            mListener = listener;
            return this;
        }

        public Ticked build() {
            return new Ticked(this);
        }
    }

    public RectF takeSubRectF() {
        if (mTags == null || mTags.isEmpty()) {
            if (mListener != null) {
                mListener.onError("tag config is empty");
            }
            return null;
        }
        return mDB.query(mTags.get(0).page);
    }
}
