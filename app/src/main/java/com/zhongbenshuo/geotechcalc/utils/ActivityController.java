package com.zhongbenshuo.geotechcalc.utils;

import androidx.appcompat.app.AppCompatActivity;

import com.zhongbenshuo.geotechcalc.MyApplication;
import com.zhongbenshuo.geotechcalc.sqlite.DbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Activity栈队列管理类
 * Activity管理类，获取当前显示的Activity实例
 * Created by LiYuliang on 2017/9/18.
 *
 * @author LiYuliang
 * @version 2017/12/09
 */

public class ActivityController {

    private static ActivityController sInstance = new ActivityController();
    /**
     * 采用弱引用持有 Activity ，避免造成 内存泄露
     */
    private WeakReference<AppCompatActivity> sCurrentActivityWeakRef;

    /**
     * Activity栈队列，存放activity对象
     */
    public static List<AppCompatActivity> activities = new LinkedList<>();

    private ActivityController() {

    }

    public static ActivityController getInstance() {
        return sInstance;
    }

    /**
     * 添加Activity对象到集合中
     *
     * @param activity Activity对象
     */
    public static void addActivity(AppCompatActivity activity) {
        activities.add(activity);
        LogUtils.d("ActivityController", "addActivity：" + activity.getLocalClassName() + ",当前Activity栈队列中activity对象共有" + activities.size() + "个");
    }

    /**
     * 从集合中删除Activity对象
     *
     * @param activity Activity对象
     */
    public static void removeActivity(AppCompatActivity activity) {
        if (activities.contains(activity)) {
            activities.remove(activity);
        }
        LogUtils.d("ActivityController", "removeActivity：" + activity.getLocalClassName() + ",当前Activity栈队列中activity对象共有" + activities.size() + "个");
    }

    /**
     * 从集合中删除并关闭Activity对象
     *
     * @param activity Activity对象
     */
    public static void finishActivity(AppCompatActivity activity) {
        if (activities.contains(activity)) {
            activities.remove(activity);
            activity.finish();
        }
        LogUtils.d("ActivityController", "finishActivity：" + activity.getLocalClassName() + ",当前Activity栈队列中activity对象共有" + activities.size() + "个");
    }

    /**
     * 关闭并删除除了本Activity以外的所有Activity
     *
     * @param currentActivity 当前Activity对象（防止误操作把当前Activity也finish掉）
     *                        不能在对一个List进行遍历的时候将其中的元素删除掉，否则报异常：java.util.ConcurrentModificationException
     */
    public static void finishOtherActivity(AppCompatActivity currentActivity) {
        //用来装需要删除的Activity对象
        List<AppCompatActivity> delList = new ArrayList<>();
        for (AppCompatActivity activity : activities) {
            if (!activity.isFinishing() && currentActivity != activity) {
                delList.add(activity);
                activity.finish();
            }
        }
        activities.removeAll(delList);
    }

    /**
     * 退出程序时清空账号和版本对象，并删除所有Activity
     */
    public static void exit() {
        DbHelper dbHelper = MyApplication.getInstance().getmDbHelper();
        if (dbHelper != null) {
            dbHelper.close();
            MyApplication.getInstance().setmDbHelper(null);
        }
        List<AppCompatActivity> delList = new ArrayList<>();
        for (AppCompatActivity activity : activities) {
            if (!activity.isFinishing()) {
                delList.add(activity);
                activity.finish();
            }
        }
        activities.removeAll(delList);
    }

    public AppCompatActivity getCurrentActivity() {
        AppCompatActivity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    public void setCurrentActivity(AppCompatActivity activity) {
        sCurrentActivityWeakRef = new WeakReference<>(activity);
    }
}
