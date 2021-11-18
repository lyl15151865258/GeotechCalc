package com.zhongbenshuo.geotechcalc.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongbenshuo.geotechcalc.R;
import com.zhongbenshuo.geotechcalc.utils.ActivityController;
import com.zhongbenshuo.geotechcalc.utils.LogUtils;
import com.zhongbenshuo.geotechcalc.utils.StatusBarUtil;
import com.zhongbenshuo.geotechcalc.widget.LoadingDialog;

/**
 * 父类activity
 * Created by Li Yuliang on 2017/2/13 0013.
 *
 * @author LiYuliang
 * @version 2017/10/27
 */

public abstract class BaseActivity extends AppCompatActivity {

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onCreate() ");
        ActivityController.addActivity(this);
        //不允许截屏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        //保持屏幕常亮（禁止休眠）
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        WebView.setWebContentsDebuggingEnabled(true);
        loadingDialog = new LoadingDialog(this, R.style.loading_dialog);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    /**
     * 设置状态栏颜色和Toolbar颜色一致
     */
    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorGreenPrimary));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (mInputMethodManager != null) {
                return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onStart() ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onRestart() ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onResume() ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //取消显示dialog
        cancelDialog();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onPause() ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onStop() ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onDestroy() ");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtils.d(LogUtils.TAG, getClass().getSimpleName() + "onConfigurationChanged() ");
    }

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    /**
     * Toast
     *
     * @param msg 需要显示的文本
     */
    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void openActivity(Class<?> pClass) {
        openActivity(pClass, null, null);
    }

    public void openActivity(Class<?> pClass, Bundle bundle) {
        openActivity(pClass, bundle, null);
    }

    public void openActivity(Class<?> pClass, Bundle bundle, Uri uri) {
        Intent intent = new Intent(this, pClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
    }

    public void openActivity(String action) {
        openActivity(action, null, null);
    }

    public void openActivity(String action, Bundle bundle) {
        openActivity(action, bundle, null);
    }

    public void openActivity(String action, Bundle bundle, Uri uri) {
        Intent intent = new Intent(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (uri != null) {
            intent.setData(uri);
        }
        startActivity(intent);
    }

    /**
     * 点击除了EditText以外的所有地方都可以隐藏软键盘
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if ((v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom);
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (null != im) {
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 显示加载的dialogs
     *
     * @param context    Context对象
     * @param msg        显示的信息
     * @param cancelable 是否可取消
     */
    public void showLoadingDialog(Context context, String msg, boolean cancelable) {
        if (!loadingDialog.isShowing()) {
            loadingDialog = new LoadingDialog(this, R.style.loading_dialog);
            loadingDialog.setCancelable(cancelable);

            if (!((AppCompatActivity) context).isFinishing()) {
                //显示dialog
                loadingDialog.show();
                loadingDialog.setMessage(msg);
            }
        }
    }

    /**
     * 显示加载的dialogs
     *
     * @param context    Context对象
     * @param cancelable 是否可取消
     */
    public void showLoadingDialog(Context context, boolean cancelable) {
        if (!loadingDialog.isShowing()) {
            loadingDialog = new LoadingDialog(this, R.style.loading_dialog);
            loadingDialog.setCancelable(cancelable);

            if (!((AppCompatActivity) context).isFinishing()) {
                //显示dialog
                loadingDialog.show();
            }
        }
    }

    /**
     * 取消dialog显示
     */
    public void cancelDialog() {
        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this instanceof MainActivity) {
                // 改返回键为Home键，返回手机主界面，不退出APP
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                return true;
            } else {
                //如果activity队列还包含本activity以外的activity，则删除本activity，回退到前一个activity
                ActivityController.finishActivity(this);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
