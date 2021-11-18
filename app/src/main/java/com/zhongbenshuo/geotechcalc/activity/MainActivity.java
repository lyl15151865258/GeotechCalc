package com.zhongbenshuo.geotechcalc.activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongbenshuo.geotechcalc.BuildConfig;
import com.zhongbenshuo.geotechcalc.MyApplication;
import com.zhongbenshuo.geotechcalc.R;
import com.zhongbenshuo.geotechcalc.adapter.HistoryAdapter;
import com.zhongbenshuo.geotechcalc.bean.Data;
import com.zhongbenshuo.geotechcalc.contentprovider.SPHelper;
import com.zhongbenshuo.geotechcalc.sqlite.DbHelper;
import com.zhongbenshuo.geotechcalc.sqlite.table.Table;
import com.zhongbenshuo.geotechcalc.utils.ExcelUtils;
import com.zhongbenshuo.geotechcalc.utils.LogUtils;
import com.zhongbenshuo.geotechcalc.utils.PermissionUtil;
import com.zhongbenshuo.geotechcalc.utils.StringUtil;
import com.zhongbenshuo.geotechcalc.utils.TimeUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity {

    private Context mContext;
    private final String[] excelTitle = {"公式", "阈值(%)", "变量值", "压实度(%)", "是否有效", "记录时间"};
    private final String[] formula = {"线性：y=ax+b", "二次：y=ax²+bx+c", "指数：y=ae^(bx)", "对数：y=aln(x)+b", "乘幂：y=ax^b"};
    private final List<Data> historyList = new ArrayList<>();
    // 压实度阈值输入框变化
    private final TextWatcher textWatcher2 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            textColor();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private AppCompatEditText etA, etB, etC, etThreshold, etTimes;
    private AppCompatTextView tvFormula, tvResult;
    private final AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            dataChange(position);
            calc(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private AppCompatSpinner spinner;
    private final TextWatcher textWatcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            dataChange(spinner.getSelectedItemPosition());
            calc(spinner.getSelectedItemPosition());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    // 变量X输入框变化
    private final TextWatcher textWatcher3 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(etTimes.getText())) {
                calc(spinner.getSelectedItemPosition());
            } else {
                tvResult.setText("");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private AppCompatButton btnClean, btnSave, btnClear, btnExport;
    private DbHelper mDbHelper;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private final MyHandler myHandler = new MyHandler(this);
    private ExecutorService executorService;

    private static class MyHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = mActivity.get();
            switch (msg.what) {
                case 1:
                    mainActivity.cancelDialog();
                    mainActivity.showToast("导出历史记录成功");
                    // 启用分享
                    String filePath = SPHelper.getString("directory", "");
                    if (!filePath.equals("")) {
                        shareFile(mainActivity.mContext, filePath);
                    }
                    break;
                case 2:
                    mainActivity.cancelDialog();
                    mainActivity.showToast("导出历史记录失败");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        spinner = findViewById(R.id.sp1);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.item_select, formula);//设置数组适配器的布局样式
        arrayAdapter.setDropDownViewResource(R.layout.item_dropdown);
        //设置下拉框的标题，不设置就没有难看的标题了
        spinner.setPrompt("请选择公式类型");
        //设置下拉框的数组适配器
        spinner.setAdapter(arrayAdapter);
        //设置下拉框默认的显示第一项
        spinner.setSelection(0);
        //给下拉框设置选择监听器，一旦用户选中某一项，就触发监听器的onItemSelected方法
        spinner.setOnItemSelectedListener(onItemSelectedListener);

        etA = findViewById(R.id.etA);
        etB = findViewById(R.id.etB);
        etC = findViewById(R.id.etC);
        etThreshold = findViewById(R.id.etThreshold);
        etTimes = findViewById(R.id.etTimes);
        tvFormula = findViewById(R.id.tvFormula);
        tvResult = findViewById(R.id.tvResult);
        btnClean = findViewById(R.id.btnClean);
        btnSave = findViewById(R.id.btnSave);
        btnClear = findViewById(R.id.btnClear);
        btnExport = findViewById(R.id.btnExport);
        etA.addTextChangedListener(textWatcher1);
        etB.addTextChangedListener(textWatcher1);
        etC.addTextChangedListener(textWatcher1);
        etThreshold.addTextChangedListener(textWatcher2);
        etTimes.addTextChangedListener(textWatcher3);
        btnClean.setOnClickListener(onClickListener);
        btnSave.setOnClickListener(onClickListener);
        btnClear.setOnClickListener(onClickListener);
        btnExport.setOnClickListener(onClickListener);
        mDbHelper = MyApplication.getInstance().getmDbHelper();
        rvHistory = findViewById(R.id.rvHistory);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvHistory.setLayoutManager(linearLayoutManager);
        historyAdapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(historyAdapter);
        executorService = Executors.newFixedThreadPool(1);
        // 查询历史数据
        queryData();
        // 每次进来检查一下权限
        if (PermissionUtil.isNeedRequestPermission(this)) {
            PermissionUtil.requestPermission(MainActivity.this);
        }
    }

    private final View.OnClickListener onClickListener = v -> {
        if (v.getId() == R.id.btnClean) {
            // 清除内容
            etA.setText("");
            etB.setText("");
            etC.setText("");
            etThreshold.setText("");
            etTimes.setText("");
        } else if (v.getId() == R.id.btnSave) {
            // 保存数据
            saveData();
        } else if (v.getId() == R.id.btnExport) {
            // 导出数据
            new AlertDialog.Builder(MainActivity.this).setTitle("提示")//设置对话框标题
                    .setMessage("是否导出所有记录到Excel？")
                    .setPositiveButton("是", (dialog, which) -> exportData())
                    .setNegativeButton("否", (dialog, which) -> {
                    }).show();
        } else if (v.getId() == R.id.btnClear) {
            // 清除数据
            new AlertDialog.Builder(MainActivity.this).setTitle("警告")//设置对话框标题
                    .setMessage("是否删除所有历史记录？")
                    .setPositiveButton("是", (dialog, which) -> clearData())
                    .setNegativeButton("否", (dialog, which) -> {
                    }).show();
        }
    };

    // 判断文本显示颜色
    private void textColor() {
        if (!TextUtils.isEmpty(etThreshold.getText()) && !TextUtils.isEmpty(tvResult.getText())) {
            double threshold = Double.parseDouble(String.valueOf(etThreshold.getText()));
            double result = Double.parseDouble(String.valueOf(tvResult.getText()));
            if (result > threshold) {
                tvResult.setTextColor(Color.GREEN);
            } else {
                tvResult.setTextColor(Color.RED);
            }
        } else if (TextUtils.isEmpty(etThreshold.getText())) {
            tvResult.setTextColor(Color.BLACK);
        }
    }

    private void dataChange(int checkedId) {
        if (checkedId == 0) {
            if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText())) {
                tvFormula.setText("y=" + etA.getText() + "x+" + etB.getText());
            } else {
                tvFormula.setText("");
            }
        } else if (checkedId == 1) {
            if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText()) && !TextUtils.isEmpty(etC.getText())) {
                tvFormula.setText("y=" + etA.getText() + "x²+" + etB.getText() + "x+" + etC.getText());
            } else {
                tvFormula.setText("");
            }
        } else if (checkedId == 2) {
            if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText())) {
                tvFormula.setText("y=" + etA.getText() + "e^(" + etB.getText() + "x)");
            } else {
                tvFormula.setText("");
            }
        } else if (checkedId == 3) {
            if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText())) {
                tvFormula.setText("y=" + etA.getText() + "ln(x)+" + etB.getText());
            } else {
                tvFormula.setText("");
            }
        } else if (checkedId == 4) {
            if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText())) {
                tvFormula.setText("y=" + etA.getText() + "x^" + etB.getText());
            } else {
                tvFormula.setText("");
            }
        }
    }

    private void calc(int checkedId) {
        try {
            double a = 0, b = 0, c = 0, x = 0;
            if (!TextUtils.isEmpty(etA.getText())) {
                a = Double.parseDouble(String.valueOf(etA.getText()));
            }
            if (!TextUtils.isEmpty(etB.getText())) {
                b = Double.parseDouble(String.valueOf(etB.getText()));
            }
            if (!TextUtils.isEmpty(etC.getText())) {
                c = Double.parseDouble(String.valueOf(etC.getText()));
            }
            if (!TextUtils.isEmpty(etTimes.getText())) {
                x = Double.parseDouble(String.valueOf(etTimes.getText()));
            }
            if (checkedId == 0) {
                if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText()) && !TextUtils.isEmpty(etTimes.getText())) {
                    double y = a * x + b;
                    tvResult.setText(StringUtil.removeZero(String.valueOf(y)));
                    textColor();
                } else {
                    tvResult.setText("");
                }
            } else if (checkedId == 1) {
                if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText()) && !TextUtils.isEmpty(etC.getText()) && !TextUtils.isEmpty(etTimes.getText())) {
                    double y = a * x * x + b * x + c;
                    tvResult.setText(StringUtil.removeZero(String.valueOf(y)));
                    textColor();
                } else {
                    tvResult.setText("");
                }
            } else if (checkedId == 2) {
                if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText()) && !TextUtils.isEmpty(etTimes.getText())) {
                    double y = a * Math.pow(Math.E, b * x);
                    tvResult.setText(StringUtil.removeZero(String.valueOf(y)));
                    textColor();
                } else {
                    tvResult.setText("");
                }
            } else if (checkedId == 3) {
                if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText()) && !TextUtils.isEmpty(etTimes.getText())) {
                    double y = a * Math.log(x) + b;
                    tvResult.setText(StringUtil.removeZero(String.valueOf(y)));
                    textColor();
                } else {
                    tvResult.setText("");
                }
            } else if (checkedId == 4) {
                if (!TextUtils.isEmpty(etA.getText()) && !TextUtils.isEmpty(etB.getText()) && !TextUtils.isEmpty(etTimes.getText())) {
                    double y = a * Math.pow(x, b);
                    tvResult.setText(StringUtil.removeZero(String.valueOf(y)));
                    textColor();
                } else {
                    tvResult.setText("");
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // 保存数据到数据库
    private void saveData() {
        String formula = tvFormula.getText().toString();
        double threshold = 0;
        String times = String.valueOf(etTimes.getText());
        double result = 0;
        String effective = "";

        if (!TextUtils.isEmpty(etThreshold.getText()) && !TextUtils.isEmpty(tvResult.getText())) {
            threshold = Double.parseDouble(String.valueOf(etThreshold.getText()));
            result = Double.parseDouble(String.valueOf(tvResult.getText()));
            if (result > threshold) {
                effective = "有效";
            } else {
                effective = "无效";
            }
        }

        if (TextUtils.isEmpty(formula) || TextUtils.isEmpty(times) || TextUtils.isEmpty(String.valueOf(tvResult.getText()))) {
            showToast("数据不完整");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Table.HistoryTable.FORMULA, formula);
        values.put(Table.HistoryTable.THRESHOLD, StringUtil.removeZero(String.valueOf(threshold)));
        values.put(Table.HistoryTable.TIMES, times);
        values.put(Table.HistoryTable.RESULT, StringUtil.removeZero(String.valueOf(result)));
        values.put(Table.HistoryTable.EFFECTIVE, effective);
        values.put(Table.HistoryTable.DATETIME, TimeUtils.getCurrentDateTime());
        String tableName = Table.HistoryTable.TABLE_NAME;
        if (mDbHelper.insert(tableName, values) != -1) {
            showToast("数据保存成功");
            queryData();
        } else {
            showToast("数据保存失败");
        }
    }

    // 清空记录
    private void clearData() {
        mDbHelper.delete(Table.HistoryTable.TABLE_NAME, null, null);
        showToast("数据删除成功");
        queryData();
    }

    // 导出数据
    private void exportData() {
        if (historyList.size() == 0) {
            showToast("没有历史记录");
        } else {
            showLoadingDialog(mContext, "生成中", true);
            Runnable runnable = () -> {
                boolean result = exportToExcel("Record", TimeUtils.getCurrentDateTime() + "钎探计算记录");
                if (result) {
                    myHandler.sendMessage(myHandler.obtainMessage(1));
                } else {
                    myHandler.sendMessage(myHandler.obtainMessage(2));
                }
            };
            executorService.submit(runnable);
        }
    }

    // 查询历史数据
    private void queryData() {
        Cursor cursor = mDbHelper.findList(Table.HistoryTable.TABLE_NAME, null, null, null, null, null, "id desc");
        // 清空list
        historyList.clear();
        // 查询到的数据添加到list集合
        while (cursor.moveToNext()) {
            Data data = new Data();
            data.setFormula(cursor.getString(1));
            data.setThreshold(cursor.getString(2));
            data.setTimes(cursor.getString(3));
            data.setResult(cursor.getString(4));
            data.setEffective(cursor.getString(5));
            data.setDatetime(cursor.getString(6));
            historyList.add(data);
        }
        historyAdapter.notifyDataSetChanged();
        if (historyList.size() == 0) {
            rvHistory.setVisibility(View.GONE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
        }
        cursor.close();
    }

    /**
     * 导出到Excel文件
     *
     * @param folderName 文件夹名称
     * @param fileName   文件名称
     * @return 导出结果
     */
    public boolean exportToExcel(String folderName, String fileName) {
        File file = new File(getSDPath(mContext) + "/" + folderName);
        makeDir(file);
        ExcelUtils.initExcel(1, file.toString() + "/" + fileName + ".xls", excelTitle);
        return ExcelUtils.writeHistoryDataToExcel(historyList, file.toString() + "/" + fileName + ".xls");
    }

    public boolean makeDir(File dir) {
        if (!Objects.requireNonNull(dir.getParentFile()).exists()) {
            boolean r = makeDir(dir.getParentFile());
            LogUtils.d("文件夹", "创建父文件夹：" + dir.getParentFile().toString() + "--" + r);
        }
        boolean result = dir.mkdir();
        LogUtils.d("文件夹", "创建子文件夹：" + dir.toString() + "--" + result);
        return result;
    }

    public static String getSDPath(Context context) {
        File sdDir;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
        if (sdCardExist) {
            if (Build.VERSION.SDK_INT >= 29) {
                // Android10之后
                sdDir = context.getExternalFilesDir(null);
            } else {
                // 获取SD卡根目录
                sdDir = Environment.getExternalStorageDirectory();
            }
        } else {
            sdDir = Environment.getRootDirectory();// 获取根目录
        }
        return sdDir.toString();
    }

    public static void shareFile(Context context, String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            Intent share = new Intent(Intent.ACTION_SEND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
                share.putExtra(Intent.EXTRA_STREAM, contentUri);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            }
            share.setType("application/vnd.ms-excel");//此处可发送多种文件
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(share, "分享文件"));
        } else {
            Toast.makeText(context, "分享文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}