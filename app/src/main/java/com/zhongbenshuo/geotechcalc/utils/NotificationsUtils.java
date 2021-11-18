package com.zhongbenshuo.geotechcalc.utils;

import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

public class NotificationsUtils {

    public static boolean isNotificationEnabled(Context context) {
        //API 19以下的版本无法获得通知栏权限，该方法默认会返回true
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }
}
