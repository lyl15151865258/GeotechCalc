package com.zhongbenshuo.geotechcalc.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.zhongbenshuo.geotechcalc.R;
import com.zhongbenshuo.geotechcalc.utils.ActivityController;
import com.zhongbenshuo.geotechcalc.utils.LogUtils;
import com.zhongbenshuo.geotechcalc.utils.PermissionUtil;
import com.zhongbenshuo.geotechcalc.utils.StatusBarUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Logo页面
 * Created at 2019/9/24 18:21
 *
 * @author LiYuliang
 * @version 1.0
 */

public class LogoActivity extends BaseActivity {

    private AppCompatTextView tvSkip;
    private ExecutorService executorService;
    private int leftTime = 5;
    private boolean alreadyOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        tvSkip = findViewById(R.id.tvSkip);
        tvSkip.setOnClickListener(onClickListener);
        executorService = Executors.newFixedThreadPool(1);
        checkPermission();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(this);
    }

    private final View.OnClickListener onClickListener = (v) -> {
        if (v.getId() == R.id.tvSkip) {
            // 跳过首页
            alreadyOpen = true;
            openActivity(MainActivity.class);
            ActivityController.finishActivity(this);
        }
    };

    private void doAsyncCode() {
        LogUtils.d("倒计时", "执行倒计时");
        executorService.submit(() -> {
            while (leftTime >= 0) {
                doOnUiCode();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                leftTime--;
            }
        });
    }

    private void doOnUiCode() {
        Handler uiThread = new Handler(Looper.getMainLooper());
        uiThread.post(() -> {
            tvSkip.setText(String.format(getString(R.string.SkipAd), leftTime));
            tvSkip.setVisibility(View.VISIBLE);
            // 提前1秒执行跳转，跳转耗时
            if (leftTime == 0 && !alreadyOpen) {
                openActivity(MainActivity.class);
                ActivityController.finishActivity(this);
            }
        });
    }

    /**
     * 检查权限
     */
    private void checkPermission() {
        if (PermissionUtil.isNeedRequestPermission(this)) {
            // 开始权限检查
            PermissionUtil.requestPermission(this);
        } else {
            // 执行倒计时
            doAsyncCode();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtil.isNeedRequestPermission(this)) {
            // 权限被拒绝，提示用户到设置页面授予权限（防止用户点击了“不再提示”后，无法通过弹窗申请权限）
            showToSettings();
        } else {
            // 执行倒计时
            doAsyncCode();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {
            // 从设置页面返回来
            if (PermissionUtil.isNeedRequestPermission(this)) {
                // 权限被拒绝，提示用户到设置页面授予权限（防止用户点击了“不再提示”后，无法通过弹窗申请权限）
                showToSettings();
            }
        }
    }

    /**
     * 提示到设置页面授予权限
     */
    private void showToSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 500);
    }

    /**
     * Logo页面不允许退出
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

}
