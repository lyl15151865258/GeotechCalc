package com.zhongbenshuo.geotechcalc;

import android.app.Application;
import android.content.Context;

import com.zhongbenshuo.geotechcalc.contentprovider.SPHelper;
import com.zhongbenshuo.geotechcalc.sqlite.DbHelper;
import com.zhongbenshuo.geotechcalc.utils.CrashHandler;

public class MyApplication extends Application {
    private static MyApplication instance;
    private Context mContext;
    private DbHelper mDbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mContext = getApplicationContext();
        SPHelper.init(this);
        // 捕捉异常
        CrashHandler.getInstance().init(this);
    }

    public static MyApplication getInstance() {
        if (instance == null) {
            instance = new MyApplication();
        }
        return instance;
    }

    public DbHelper getmDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = new DbHelper(mContext);
            mDbHelper.getDBHelper();
            mDbHelper.open();
        }
        return mDbHelper;
    }

    public void setmDbHelper(DbHelper dbHelper) {
        mDbHelper = dbHelper;
    }

}
