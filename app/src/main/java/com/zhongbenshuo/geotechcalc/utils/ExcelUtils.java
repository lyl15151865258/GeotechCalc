package com.zhongbenshuo.geotechcalc.utils;

import com.zhongbenshuo.geotechcalc.bean.Data;
import com.zhongbenshuo.geotechcalc.contentprovider.SPHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExcelUtils {
    private static WritableCellFormat titleStyle = null;
    private static WritableCellFormat firstRowStyle = null;
    private static WritableCellFormat contentStyle = null;
    private final static String UTF8_ENCODING = "UTF-8";

    public static void format() {
        try {
            //标题格式化
            WritableFont arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(Colour.LIGHT_BLUE);
            titleStyle = new WritableCellFormat(arial14font);
            titleStyle.setAlignment(jxl.format.Alignment.CENTRE);
            titleStyle.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            titleStyle.setBackground(Colour.VERY_LIGHT_YELLOW);
            //第一行标题格式化
            WritableFont arial10font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);
            firstRowStyle = new WritableCellFormat(arial10font);
            firstRowStyle.setAlignment(jxl.format.Alignment.CENTRE);
            firstRowStyle.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            firstRowStyle.setBackground(Colour.LIGHT_GREEN);
            //内容格式化
            WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12);
            contentStyle = new WritableCellFormat(arial12font);
            contentStyle.setAlignment(jxl.format.Alignment.CENTRE);
            contentStyle.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    public static void initExcel(int i, String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                boolean result = file.createNewFile();
                LogUtils.d("文件夹", "创建" + fileName + result);
            }
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("历史记录", 0);
            setColumnStyle(i, sheet);
            sheet.addCell(new Label(0, 0, fileName, titleStyle));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], firstRowStyle));
            }
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //设置列宽
    private static void setColumnStyle(int i, WritableSheet sheet) {
        switch (i) {
            case 1:
                //Excel列宽
                sheet.setColumnView(0, 30);         //公式
                sheet.setColumnView(1, 10);         //阈值
                sheet.setColumnView(2, 10);         //变量值
                sheet.setColumnView(3, 15);         //压实度
                sheet.setColumnView(4, 10);         //是否有效
                sheet.setColumnView(5, 30);         //记录时间
                break;
            default:
                break;
        }
    }

    // 导出历史数据
    public static <T> boolean writeHistoryDataToExcel(List<T> objList, String fileName) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writableWorkbook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(fileName);
                Workbook workbook = Workbook.getWorkbook(in);
                writableWorkbook = Workbook.createWorkbook(new File(fileName), workbook);
                WritableSheet sheet = writableWorkbook.getSheet(0);
                for (int j = 0; j < objList.size(); j++) {
                    Data data = (Data) objList.get(j);
                    ArrayList<String> list = new ArrayList<>();
                    list.add(data.getFormula());
                    list.add(data.getThreshold());
                    list.add(data.getTimes());
                    list.add(data.getResult());
                    list.add(data.getEffective());
                    list.add(data.getDatetime());
                    for (int i = 0; i < list.size(); i++) {
                        sheet.addCell(new Label(i, j + 1, list.get(i), contentStyle));
                    }
                }
                writableWorkbook.write();
                LogUtils.d("文件夹", "生成文件：" + fileName);
                SPHelper.save("directory", fileName);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writableWorkbook != null) {
                    try {
                        writableWorkbook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }
        return false;
    }

    public static Object getValueByRef(Class cls, String fieldName) {
        Object value = null;
        fieldName = fieldName.replaceFirst(fieldName.substring(0, 1), fieldName
                .substring(0, 1).toUpperCase());
        String getMethodName = "get" + fieldName;
        try {
            Method method = cls.getMethod(getMethodName);
            value = method.invoke(cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
