package com.zhongbenshuo.geotechcalc.utils;

import android.app.AlarmManager;
import android.text.TextUtils;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by george.yang on 2016-4-27.
 */
public class StringUtil {

    private final static Pattern emailer = Pattern
            .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

    private final static Pattern IMG_URL = Pattern
            .compile(".*?(gif|jpeg|png|jpg|bmp)");

    private final static Pattern URL = Pattern
            .compile("^(https|http)://.*?$(net|com|.com.cn|org|me|)");

    private final static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
    };

    private final static ThreadLocal<SimpleDateFormat> YYYYMMDD = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
    };

    private final static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        }
    };

    private static final Pattern UniversalDatePattern = Pattern.compile(
            "([0-9]{4})-([0-9]{2})-([0-9]{2})[\\s]+([0-9]{2}):([0-9]{2}):([0-9]{2})"
    );


    public static int string2int(String str) {
        return string2int(str, 0);
    }

    public static int string2int(String str, int def) {
        try {
            return Integer.valueOf(str);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param sdate string date that's type like YYYY-MM-DD HH:mm:ss
     * @return {@link Date}
     */
    public static Date toDate(String sdate) {
        return toDate(sdate, YYYYMMDDHHMMSS.get());
    }

    public static Date toDate(String sdate, SimpleDateFormat formatter) {
        try {
            return formatter.parse(sdate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * YYYY-MM-DD HH:mm:ss?????????????????????????????????{@link Calendar}??????
     *
     * @param str YYYY-MM-DD HH:mm:ss???????????????
     * @return {@link Calendar}
     */
    public static Calendar parseCalendar(String str) {
        Matcher matcher = UniversalDatePattern.matcher(str);
        Calendar calendar = Calendar.getInstance();
        if (!matcher.find()) {
            return null;
        }
        calendar.set(
                matcher.group(1) == null ? 0 : toInt(matcher.group(1)),
                matcher.group(2) == null ? 0 : toInt(matcher.group(2)) - 1,
                matcher.group(3) == null ? 0 : toInt(matcher.group(3)),
                matcher.group(4) == null ? 0 : toInt(matcher.group(4)),
                matcher.group(5) == null ? 0 : toInt(matcher.group(5)),
                matcher.group(6) == null ? 0 : toInt(matcher.group(6))
        );
        return calendar;
    }

    /**
     * transform date to string that's type like YYYY-MM-DD HH:mm:ss
     *
     * @param date {@link Date}
     * @return
     */
    public static String getDateString(Date date) {
        return YYYYMMDDHHMMSS.get().format(date);
    }

    /**
     * transform date to string that's type like YYYY-MM-DD HH:mm
     *
     * @param sdate
     * @return
     */
    public static String getDateString(String sdate) {
        if (TextUtils.isEmpty(sdate)) return "";
        return YYYYMMDDHHMM.get().format(toDate(sdate));
    }


    public static String formatYearMonthDay(String st) {
        Matcher matcher = UniversalDatePattern.matcher(st);
        if (!matcher.find()) {
            return st;
        }
        return String.format("%s???%s???%s???",
                matcher.group(1) == null ? 0 : matcher.group(1),
                matcher.group(2) == null ? 0 : matcher.group(2),
                matcher.group(3) == null ? 0 : matcher.group(3));
    }

    public static String formatYearMonthDayNew(String st) {
        Matcher matcher = UniversalDatePattern.matcher(st);
        if (!matcher.find()) {
            return st;
        }
        return String.format("%s/%s/%s",
                matcher.group(1) == null ? 0 : matcher.group(1),
                matcher.group(2) == null ? 0 : matcher.group(2),
                matcher.group(3) == null ? 0 : matcher.group(3));
    }

    /**
     * format time friendly
     *
     * @param sdate YYYY-MM-DD HH:mm:ss
     * @return n?????????, n?????????, ??????, ??????, n??????, n?????????
     */
    public static String formatSomeAgo(String sdate) {
        if (sdate == null) {
            return "";
        }
        Calendar calendar = parseCalendar(sdate);
        if (calendar == null) {
            return sdate;
        }

        Calendar mCurrentDate = Calendar.getInstance();
        long crim = mCurrentDate.getTimeInMillis(); // current
        long trim = calendar.getTimeInMillis(); // target
        long diff = crim - trim;

        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);

        if (diff < 60 * 1000) {
            return "??????";
        }
        if (diff >= 60 * 1000 && diff < AlarmManager.INTERVAL_HOUR) {
            return String.format("%s?????????", diff / 60 / 1000);
        }
        mCurrentDate.set(year, month, day, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return String.format("%s?????????", diff / AlarmManager.INTERVAL_HOUR);
        }
        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "??????";
        }
        mCurrentDate.set(year, month, day - 2, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "??????";
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30) {
            return String.format("%s??????", diff / AlarmManager.INTERVAL_DAY);
        }
        if (diff < AlarmManager.INTERVAL_DAY * 30 * 12) {
            return String.format("%s??????", diff / (AlarmManager.INTERVAL_DAY * 30));
        }
        return String.format("%s??????", mCurrentDate.get(Calendar.YEAR) - calendar.get(Calendar.YEAR));
    }

    /**
     * @param str YYYY-MM-DD HH:mm:ss string
     * @return ??????, ??????, ??????, n??????
     */
    public static String formatSomeDay(String str) {
        return formatSomeDay(parseCalendar(str));
    }

    /**
     * @param calendar {@link Calendar}
     * @return ??????, ??????, ??????, n??????
     */
    public static String formatSomeDay(Calendar calendar) {
        if (calendar == null) {
            return "???????";
        }
        Calendar mCurrentDate = Calendar.getInstance();
        long crim = mCurrentDate.getTimeInMillis(); // current
        long trim = calendar.getTimeInMillis(); // target
        long diff = crim - trim;

        int year = mCurrentDate.get(Calendar.YEAR);
        int month = mCurrentDate.get(Calendar.MONTH);
        int day = mCurrentDate.get(Calendar.DATE);

        mCurrentDate.set(year, month, day, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "??????";
        }
        mCurrentDate.set(year, month, day - 1, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "??????";
        }
        mCurrentDate.set(year, month, day - 2, 0, 0, 0);
        if (trim >= mCurrentDate.getTimeInMillis()) {
            return "??????";
        }
        return String.format("%s??????", diff / AlarmManager.INTERVAL_DAY);
    }

    /**
     * @param calendar {@link Calendar}
     * @return ??????n
     */
    public static String formatWeek(Calendar calendar) {
        if (calendar == null) {
            return "???????";
        }
        return new String[]{"?????????", "?????????", "?????????", "?????????", "?????????", "?????????", "?????????"}[calendar.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * @param str YYYY-MM-DD HH:mm:ss string
     * @return ??????n
     */
    public static String formatWeek(String str) {
        return formatWeek(parseCalendar(str));
    }

    /**
     * @param sdate YYYY-MM-DD HH:mm:ss string
     * @return
     */
    public static String formatDayWeek(String sdate) {
        Calendar calendar = parseCalendar(sdate);
        if (calendar == null) {
            return "??/?? ???????";
        }
        Calendar mCurrentDate = Calendar.getInstance();
        String ws = formatWeek(calendar);
        int diff = mCurrentDate.get(Calendar.DATE) - calendar.get(Calendar.DATE);
        if (diff == 0) {
            return "?????? / " + ws;
        }
        if (diff == 1) {
            return "?????? / " + ws;
        }
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DATE);
        return String.format("%s/%s / %s", formatInt(m), formatInt(d), ws);
    }

    /**
     * format to HH
     *
     * @param i integer
     * @return HH
     */
    public static String formatInt(int i) {
        return (i < 10 ? "0" : "") + i;
    }


    /**
     * ???????????????
     */
    public static String friendly_time3(String sdate) {
        Calendar calendar = parseCalendar(sdate);
        if (calendar == null) {
            return sdate;
        }

        Calendar mCurrentDate = Calendar.getInstance();
        SimpleDateFormat formatter = YYYYMMDDHHMMSS.get();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String s = hour >= 0 && hour < 12 ? "??????" : "??????";
        s += " HH:mm";

        if (mCurrentDate.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
            formatter.applyPattern(s);
        } else if (mCurrentDate.get(Calendar.DATE) - calendar.get(Calendar.DATE) == 1) {
            formatter.applyPattern("?????? " + s);
        } else if (mCurrentDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            formatter.applyPattern("MM-dd " + s);
        } else {
            formatter.applyPattern("YYYY-MM-dd " + s);
        }
        return formatter.format(calendar.getTime());
    }


    /**
     * ??????????????????????????????????????????
     *
     * @param sdate
     * @return boolean
     */
    public static boolean isToday(String sdate) {
        Date time = toDate(sdate);
        Date today = new Date();
        if (time != null) {
            String nowDate = YYYYMMDD.get().format(today);
            String timeDate = YYYYMMDD.get().format(time);
            return nowDate.equals(timeDate);
        }
        return false;
    }

    /**
     * ????????????????????????
     *
     * @param sdate1 sdate1
     * @param sdate2 sdate2
     * @return
     */
    public static boolean isSameDay(String sdate1, String sdate2) {
        if (TextUtils.isEmpty(sdate1) || TextUtils.isEmpty(sdate2)) {
            return false;
        }
        Date date1 = toDate(sdate1);
        Date date2 = toDate(sdate2);
        if (date1 != null && date2 != null) {
            String d1 = YYYYMMDD.get().format(date1);
            String d2 = YYYYMMDD.get().format(date2);
            return d1.equals(d2);
        }
        return false;
    }

    public static String getCurrentTimeStr() {
        return YYYYMMDDHHMMSS.get().format(new Date());
    }

    /***
     * ??????????????????????????????????????????s
     *
     * @param date1
     * @param date2
     * @return
     * @author ?????? 2015-2-9 ??????4:50:06
     */
    public static long calDateDifferent(String date1, String date2) {
        try {
            Date d1 = YYYYMMDDHHMMSS.get().parse(date1);
            Date d2 = YYYYMMDDHHMMSS.get().parse(date2);
            // ??????ms
            long diff = d2.getTime() - d1.getTime();
            return diff / 1000;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * ??????????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????? ?????????????????????null????????????????????????true
     *
     * @param input
     * @return boolean
     */
    @Deprecated
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input)) {
            return true;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0) {
            return false;
        }
        return emailer.matcher(email).matches();
    }

    /**
     * ????????????url???????????????url
     *
     * @param url
     * @return
     */
    public static boolean isImgUrl(String url) {
        if (url == null || url.trim().length() == 0) {
            return false;
        }
        return IMG_URL.matcher(url).matches();
    }

    /**
     * ??????????????????????????????url??????
     *
     * @param str
     * @return
     */
    public static boolean isUrl(String str) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        return URL.matcher(str).matches();
    }

    /**
     * ??????????????????
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /**
     * ???????????????
     *
     * @param obj
     * @return ?????????????????? 0
     */
    public static int toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        return toInt(obj.toString(), 0);
    }

    /**
     * ???????????????
     *
     * @param obj
     * @return ?????????????????? 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * ?????????????????????
     *
     * @param b
     * @return ?????????????????? false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    public static String getString(String s) {
        return s == null ? "" : s;
    }

    /**
     * ?????????InputStream?????????????????????
     *
     * @param is
     * @return
     */
    public static String toConvertString(InputStream is) {
        StringBuilder res = new StringBuilder();
        BufferedReader read = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = read.readLine()) != null) {
                res.append(line).append("<br>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res.toString();
    }

    /***
     * ???????????????
     *
     * @param start ??????????????????0??????
     * @param num   ???????????????
     * @param str   ??????????????????
     * @return
     */
    public static String getSubString(int start, int num, String str) {
        if (str == null) {
            return "";
        }
        int length = str.length();
        if (start < 0) {
            start = 0;
        }
        if (start > length) {
            start = length;
        }
        if (num < 0) {
            num = 1;
        }
        int end = start + num;
        if (end > length) {
            end = length;
        }
        return str.substring(start, end);
    }

    /**
     * ????????????????????????????????????
     *
     * @return
     */
    public static int getWeekOfYear() {
        return getWeekOfYear(new Date());
    }

    /**
     * ????????????????????????????????????
     *
     * @param date
     * @return
     */
    public static int getWeekOfYear(Date date) {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        int week = c.get(Calendar.WEEK_OF_YEAR) - 1;
        week = week == 0 ? 52 : week;
        return week > 0 ? week : 1;
    }

    public static int[] getCurrentDate() {
        int[] dateBundle = new int[3];
        String[] temp = getDataTime("yyyy-MM-dd").split("-");

        for (int i = 0; i < 3; i++) {
            try {
                dateBundle[i] = Integer.parseInt(temp[i]);
            } catch (Exception e) {
                dateBundle[i] = 0;
            }
        }
        return dateBundle;
    }

    /**
     * ????????????????????????
     */
    public static String getDataTime(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }


    public enum JSON_TYPE {
        /**
         * JSONObject
         */
        JSON_TYPE_OBJECT,
        /**
         * JSONArray
         */
        JSON_TYPE_ARRAY,
        /**
         * ??????JSON??????????????????
         */
        JSON_TYPE_ERROR
    }

    /***
     *
     * ??????JSON??????
     * ????????????
     * ??????????????????????????????{???[ ??????????????????????????????JSON???????????????
     *
     * @param str
     * @return
     */
    public static JSON_TYPE getJSONType(String str) {
        if (TextUtils.isEmpty(str)) {
            return JSON_TYPE.JSON_TYPE_ERROR;
        }

        final char[] strChar = str.substring(0, 1).toCharArray();
        final char firstChar = strChar[0];

        //LogUtils.d(JSONUtil.class, "getJSONType", " firstChar = "+firstChar);

        if (firstChar == '{') {
            return JSON_TYPE.JSON_TYPE_OBJECT;
        } else if (firstChar == '[') {
            return JSON_TYPE.JSON_TYPE_ARRAY;
        } else {
            return JSON_TYPE.JSON_TYPE_ERROR;
        }

    }

    public static int HexS1ToInt(char ch) {
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        throw new IllegalArgumentException(String.valueOf(ch));
    }

    /**
     * String???16??????int??????2??????
     *
     * @param S
     * @return
     */
    public static int HexS2ToInt(String S) {
        int r = 0;
        char a[] = S.toCharArray();
        r = HexS1ToInt(a[0]) * 16 + HexS1ToInt(a[1]);
        return r;
    }

    /**
     * ??????CS?????????
     *
     * @param s
     * @param start
     * @return
     */
    public static String getSum(String s, int start) {
        String r = "";
        int sumL = 0;
        for (int i = (start * 2); i < s.length(); i = i + 2) {
            sumL = (sumL + HexS2ToInt(s.substring(i, i + 2))) % 256;
        }
        r = Integer.toHexString(sumL / 16) + Integer.toHexString(sumL % 16);
        r = r.toUpperCase();
        return r;
    }

    /**
     * ??????????????????????????????
     *
     * @param s ??????????????????????????????
     * @return ?????????????????????
     */
    public static String getNumber(String s) {
        String string = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= 48 && s.charAt(i) <= 57) {
                string += s.charAt(i);
            }
        }
        return string;
    }

    /**
     * ????????????????????????json??????
     *
     * @param value ?????????
     * @return ?????????json?????????
     */
    public static boolean isJson(String value) {
        try {
            new org.json.JSONObject(value);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }

    /**
     * ??????String??????????????????
     *
     * @param args
     * @return
     */
    public static boolean isAllNumber(String args) {
        Pattern p = Pattern.compile("[\\d]*");
        Matcher m = p.matcher(args);
        return m.matches();
    }

    /**
     * ??????String???????????????
     *
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ??????String???????????????
     *
     * @param str
     * @return
     */
    public static boolean isDouble(String str) {
        try {
            Float.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ???????????????????????????0
     *
     * @param number ??????????????????
     * @return ???0???????????????
     */
    public static String removeZero(String number) {
        if (number.indexOf(".") > 0) {
            //????????????
            //????????????????????????
            number = number.replaceAll("0+?$", "");
            //?????????????????????????????????????????????
            number = number.replaceAll("[.]$", "");
        }
        return number;
    }

    /**
     * ????????????????????????????????????????????????????????????
     *
     * @param message ????????????
     * @return ?????????????????????
     */
    public static String changeCode(String message) {
        StringBuilder stringBuilder = new StringBuilder();
        //??????message?????????
        int length = message.length();
        for (int i = length - 2; i >= 0; i = i - 2) {
            stringBuilder.append(message.substring(i, i + 2));
        }
        return stringBuilder.toString();
    }

    /**
     * ????????????String???????????????0????????????????????????
     *
     * @param number String???????????????
     * @return ???????????????0?????????
     */
    public static String getPrettyNumber(String number) {
        return BigDecimal.valueOf(Double.parseDouble(number)).stripTrailingZeros().toPlainString();
    }

    /**
     * ??????double???????????????0????????????????????????
     *
     * @param number double???????????????
     * @return ???????????????0?????????
     */
    public static String getPrettyNumber(double number) {
        return BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
    }

    /**
     * ??????float???????????????0????????????????????????
     *
     * @param number float???????????????
     * @return ???????????????0?????????
     */
    public static String getPrettyNumber(float number) {
        return BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
    }

    /**
     * ???????????????????????????????????????
     *
     * @param content
     * @return
     */
    public static boolean hasNumber(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    /**
     * ???????????????????????????????????????5???
     *
     * @return ?????????
     */
    public static String formatCoordinate(String degree) {
        if (degree == null) {
            return "";
        } else if (degree.contains(".") && degree.length() - degree.indexOf(".") > 7) {
            return degree.substring(0, degree.indexOf(".") + 7);
        } else {
            return degree;
        }
    }

    /**
     * UTF-8?????? ?????????????????? ??????
     * <p>
     * URLEncoder.encode("??????", "UTF-8") ---> %E4%B8%8A%E6%B5%B7
     * URLDecoder.decode("%E4%B8%8A%E6%B5%B7", "UTF-8") --> ??? ???
     * <p>
     * convertUTF8ToString("E4B88AE6B5B7")
     * E4B88AE6B5B7 --> ??????
     *
     * @param s
     * @return
     */
    public static String convertUTF8ToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }

        try {
            s = s.toUpperCase();

            int total = s.length() / 2;
            int pos = 0;

            byte[] buffer = new byte[total];
            for (int i = 0; i < total; i++) {

                int start = i * 2;

                buffer[i] = (byte) Integer.parseInt(
                        s.substring(start, start + 2), 16);
                pos++;
            }

            return new String(buffer, 0, pos, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * ??????????????????????????????UTF8????????????,????????????????????????????????????????????????.
     *
     * @param s ??????
     * @return
     */
    public static String convertStringToUTF8(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        try {
            char c;
            for (int i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                if (c >= 0 && c <= 255) {
                    sb.append(c);
                } else {
                    byte[] b;

                    b = Character.toString(c).getBytes("utf-8");

                    for (int j = 0; j < b.length; j++) {
                        int k = b[j];
                        if (k < 0)
                            k += 256;
                        sb.append(Integer.toHexString(k).toUpperCase());
                        // sb.append("%" +Integer.toHexString(k).toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * ?????????????????????
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param str
     * @return
     */
    public static String stringFilter(String str) {
        str = str.replaceAll("???", "[").replaceAll("???", "]")
                .replaceAll("???", "!").replaceAll("???", ":");// ??????????????????
        String regEx = "[??????]"; // ?????????????????????
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}
