package com.crawler.base.utils;

import cn.hutool.core.util.CharUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;


/**
 * Created by Administrator on 2017/7/5.
 */
public class StringUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringUtils.class);

    public static String LOCK = "lock";

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public static String lowerFirstChar(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 用于求百分比方法，已考虑除数不为0的情况
     *
     * @param number1      除数
     * @param number2      被除数
     * @param formatNumber 保留几位小数
     * @return 百分比%
     */
    public static double getPercentage(double number1, double number2, int formatNumber) {
        double number100;
        if (number1 != 0 && number2 != 0) {
            double numberA = number1 / number2 * 100;//到报率求%
            number100 = new BigDecimal(numberA).setScale(formatNumber, BigDecimal.ROUND_HALF_UP).doubleValue();//四舍五入保留2位小数
            return number100;
        } else {
            number100 = 0.0;
            return number100;
        }
    }

    public static boolean isEmpty(Date n) {
        return n == null;
    }

    public static boolean isEmpty(Integer n) {
        return n == null;
    }

    public static boolean isEmpty(Long n) {
        return n == null;
    }

    public static boolean isEmpty(Double n) {
        return n == null;
    }

    public static boolean isEmpty(BigDecimal n) {
        return n == null;
    }



    /**
     * 获取文件后缀名
     *
     * @param fileName 文件全名
     * @return String
     */
    public static String getFilenameExtension(String fileName) {
        return getSuffixName(fileName);
    }

    /**
     * 获取去掉后缀的文件名
     *
     * @param fileName 文件全名
     * @return String
     */
    public static String getFilename(String fileName) {
        return getPrefixName(fileName);
    }

    public static Integer ifNull(Integer integer) {
        if (integer == null) {
            return 0;
        }
        return integer;
    }

    public static boolean startsWith(String str, String startsWithAStr) {
        if (isEmpty(str) || isEmpty(startsWithAStr)) {
            return false;
        }
        return str.startsWith(startsWithAStr);
    }

    public static <T> List<T> copyList(Object sourceList,Class<?> beanClass) throws Exception{
        List<Object> sList = (List<Object>) sourceList;
        List<Object> tList = new ArrayList<Object>();
        for (Object t : sList) {
            Object dto = beanClass.newInstance();
            BeanUtils.copyProperties(t, dto);
            tList.add(dto);
        }
        return (List<T>) tList;

    }



    public abstract static class Joint<T> {

        public abstract String map(T t);

    }

    public static boolean isEqual(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    /**
     * 描述: 替换所有空格（包含换行）
     *
     * @param str
     * @return
     * @auther 胡义振
     * @date 2014-9-23
     */
    public static String replaceAllBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s|\u3000|\t|\r\n|\r|\n|\n\r");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static Long LongValueOf(Long i, long defaultLong) {
        if (i == null) {
            return defaultLong;
        }
        return i;
    }

    public static Integer IntegerValueOf(Integer integer, int defaultInteger) {
        if (integer == null) {
            return defaultInteger;
        }
        return integer;
    }

    /**
     * 获取文件后缀名
     *
     * @param fileName 文件全名
     * @return String
     */
    public static String getSuffixName(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } catch (Exception er) {
            return fileName;
        }
    }

    /**
     * 获取文件后缀名
     *
     * @param fileName 文件全名
     * @return String
     */
    public static String getSuffixName2LowCase(String fileName) {
        if (isEmpty(fileName))
            return null;
        try {
            String string = fileName.substring(fileName.lastIndexOf(".") + 1);
            return string.toLowerCase();
        } catch (Exception er) {
            return fileName;
        }
    }

    /**
     * 获取去掉后缀的文件名
     *
     * @param fileName 文件全名
     * @return String
     */
    public static String getPrefixName(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * 检查字符串是否存在（如：allStr = "25,26,28,29"， ）
     *
     * @param allStr     所有字符串
     * @param checkedStr 检测的字符串
     * @return
     */
    public static boolean checkIncludeString(String allStr, String checkedStr) {
        try {
            if (allStr == null || allStr.trim().length() < 1) {
                return false;
            } else {
                allStr = allStr.replaceAll(" ", "");
                checkedStr = checkedStr.replaceAll(" ", "");
                String astr[] = allStr.split(",");
                for (int jj = 0; jj < astr.length; jj++) {
                    if (astr[jj].toLowerCase().equals(checkedStr.toLowerCase())) {
                        astr = null;
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception er) {
            return false;
        }
    }

    public static String MapToPathParam(Map map) {
        StringBuilder pathParam = new StringBuilder("?");
        Iterator<Entry> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            pathParam.append(next.getKey() + "=" + next.getValue() + "&");
        }
        return pathParam.toString();
    }

    public static String[] split(String arr, String regex) {
        if (isEmpty(arr)) {
            return new String[]{""};
        }
        arr = arr + " ";
        String[] split = arr.split(regex);
        String last = split[split.length - 1];
        split[split.length - 1] = last.substring(0, last.length() - 1);
        return split;
    }

    public static Set<String> splitSet(String arr, String regex) {
        if (isEmpty(arr)) {
            return new HashSet<>();
        }
        return new HashSet<>(splitList(arr, regex));
    }

    public static String join(Set<String> set, String split) {
        if (set == null || set.isEmpty())
            return "";
        else {
            StringBuilder sb = new StringBuilder();
            for (String s : set) {
                sb.append(s + split);
            }
            return sb.substring(0, sb.length() - split.length()).toString();
        }

    }

    public static String join(List<String> arr, String split) {
        if (arr == null || arr.isEmpty())
            return "";
        else {
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s + split);
            }
            return sb.substring(0, sb.length() - split.length()).toString();
        }

    }

    public static <T> String join(List<T> arr, String split, Joint joint) {
        if (arr == null || arr.isEmpty())
            return "";
        else {
            StringBuilder sb = new StringBuilder();
            for (T s : arr) {
                sb.append(joint.map(s) + split);
            }
            return sb.substring(0, sb.length() - split.length()).toString();
        }

    }

    public <T> String[] map(List<T> list, Joint joint) {
        if (list == null) return new String[0];
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = joint.map(list.get(i));
        }
        return arr;
    }

    ;

    public static String join(String[] arr, String split) {
        if (arr == null || arr.length == 0)
            return "";
        else {
            StringBuilder sb = new StringBuilder();
            for (String s : arr) {
                sb.append(s + split);
            }
            return sb.substring(0, sb.length() - split.length()).toString();
        }

    }

    public static byte[] gzipCompress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 压缩
        GZIPOutputStream gos = new GZIPOutputStream(baos);
        gos.write(data, 0, data.length);
        gos.finish();
        baos.flush();
        baos.close();
        return baos.toByteArray();
    }

    public static int searchPosition(String target, String object, String split) {
        if (isEmpty(target) || isEmpty(object))
            return -1;
        String[] arr = split(target, split);
        for (int i = 0; i < arr.length; i++) {
            if (object.equals(arr[i]))
                return i;
        }
        return -1;
    }

    public static String valueOf(Object o) {
        if (o == null)
            return null;
        else
            return String.valueOf(o);
    }

    public static String convert(String str, int length) {
        if (str.length() < length) {
            String temp = "";
            for (int i = 0; i < length - str.length(); i++) {
                temp += "0";
            }
            return temp + str;
        }
        return str;
    }

    public static String toBinaryString(int a, int length) {
        return convert(Integer.toBinaryString(a), length);
    }

    public static String toHexString1(byte[] b) {
        return toHexString1(b, " ");
    }

    public static String toHexString1(byte[] b, String split) {
        StringBuffer buffer = new StringBuffer();
        int n = 0;
        for (int i = 0; i < b.length; ++i) {
            buffer.append(toHexString1(b[i]));
            if (i < b.length - 1)
                buffer.append(split);
        }
        return buffer.toString();
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }

    //    public static byte[] hexStringToBytes(String hexString) {
//        if (hexString == null || hexString.equals("")) {
//            return null;
//        }
//        hexString = hexString.toUpperCase();
//        int length = hexString.length() / 2;
//        char[] hexChars = hexString.toCharArray();
//        byte[] d = new byte[length];
//        for (int i = 0; i < length; i++) {
//            int pos = i * 2;
//            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
//        }
//        return d;
//    }
    public static byte[] hexStringToBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 虚拟表（查询虚拟表）
     *
     * @param length   虚拟表行数
     * @param lineName 虚拟表列名
     * @return
     * @throws ParseException
     */
    public static String virtualTableSql(int length, String lineName) throws ParseException {
        String string = "select 1 as " + lineName;
        for (int i = 2; i <= length; i++) {
            string = string + " union all select " + i;
        }
        if (length < 1) {
            string = "";
        }
        return string;
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @return
     */
    public static String[] getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     *
     * @param inputString 原始字符串
     * @param length      指定长度
     * @param size        指定列表大小
     * @return
     */
    public static String[] getStrList(String inputString, int length,
                                      int size) {
        String[] list = new String[size];
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list[index] = childStr;
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     *
     * @param str 原始字符串
     * @param f   开始位置
     * @param t   结束位置
     * @return
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    public static String dateToString(Date date, SimpleDateFormat sdf) {
        String value = "";
        if (date != null) {
            if (sdf == null) {
                value = "";
            } else {
                value = sdf.format(date);
            }
        }
        return value;
    }

    public static String dateToString(Date date, String string) {
        SimpleDateFormat sdf = new SimpleDateFormat(string);
        return dateToString(date, sdf);
    }

    public static <T> T ifnull(T t, T o) {
        if (t == null)
            return o;
        return t;
    }

    public static String UnicodeToString(String srcText) throws UnsupportedEncodingException {
        String dst = "";
        String src = srcText;
        int len = srcText.length() / 4;
        for (int i = 0; i <= len - 1; i++) {
            String str = "";
            str = src.substring(0, 4);
            src = src.substring(4);
            byte[] bytes = new byte[2];
            bytes[1] = (byte) Integer.parseInt(str.substring(0, 2), 16);
            bytes[0] = (byte) Integer.parseInt(str.substring(2, 4), 16);
            dst += new String(bytes, "unicode");
        }
        return dst;
    }

    ;

    public static String randomNumber(int j) {
        String num = "";
        for (int i = 0; i < j; i++) {
            int n = (int) (Math.random() * 10);
            num += n;
        }
        return num;
    }

    public static String geneActivationCode() {
        StringBuilder time = new StringBuilder(StringUtils.valueOf(new Date().getTime())).reverse();
        return time + randomNumber(7);
    }

    public static String random4number() {
        String num = "";
        for (int i = 0; i < 4; i++) {
            int n = (int) (Math.random() * 10);
            num += n;
        }
        return num;
    }

    public static List<String> splitList(String arr, String regex) {
        arr = arr + " ";
        String[] split = arr.split(regex);
        String last = split[split.length - 1];
        split[split.length - 1] = last.substring(0, last.length() - 1);
        List<String> list = new ArrayList<>();
        for (String s : split) {
            list.add(s);
        }
        return list;
    }

    public static boolean operations(String str1, String expression, String str2) {
        boolean b = false;
        if (isEmpty(expression) || isEmpty(str1) || isEmpty(str2))
            return b;
        switch (expression.toUpperCase()) {
            case "NEQ":
                if (str1.equals(str2))
                    b = true;
                break;
            default:
                break;
        }

        return b;
    }

    public static String stringAddString(String str1, String str2) {
        String string = null;
        boolean empty1 = isEmpty(str1);
        boolean empty2 = isEmpty(str2);
        if (!empty1 && empty2)
            return str1;
        if (empty1 && !empty2)
            return str2;
        if (!empty1 && !empty2)
            return str1 + "," + str2;
        return string;
    }

    public static String MapToPath(Map map) {
        StringBuilder pathParam = new StringBuilder("?");
        Iterator<Entry> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            pathParam.append(next.getKey() + "={" + next.getKey() + "}&");
        }
        return pathParam.toString();
    }

    public static String object2String(Object o, String type) {
        if (o == null)
            return null;
        String string = null;
        if (type != null && "DATE".equals(type.toUpperCase())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            string = sdf.format(new Date((Long) o));
        } else {
            string = String.valueOf(o);
        }
        return string;
    }

    public static final String RGB = "^(rgb|RGB)\\(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\,([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\,([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\)$";
    public static final String RGBHEX = "^#[0-9a-fA-F]{6}$";

    public static boolean isRGB(String phone) {
        return Pattern.matches(RGB, phone);
    }

    public static boolean isRGBHex(String phone) {
        return Pattern.matches(RGBHEX, phone);
    }

    public static String convertRGBToHex(int r, int g, int b) {
        String rFString, rSString, gFString, gSString,
                bFString, bSString, result;
        int red, green, blue;
        int rred, rgreen, rblue;
        red = r / 16;
        rred = r % 16;
        if (red == 10) rFString = "A";
        else if (red == 11) rFString = "B";
        else if (red == 12) rFString = "C";
        else if (red == 13) rFString = "D";
        else if (red == 14) rFString = "E";
        else if (red == 15) rFString = "F";
        else rFString = String.valueOf(red);

        if (rred == 10) rSString = "A";
        else if (rred == 11) rSString = "B";
        else if (rred == 12) rSString = "C";
        else if (rred == 13) rSString = "D";
        else if (rred == 14) rSString = "E";
        else if (rred == 15) rSString = "F";
        else rSString = String.valueOf(rred);

        rFString = rFString + rSString;

        green = g / 16;
        rgreen = g % 16;

        if (green == 10) gFString = "A";
        else if (green == 11) gFString = "B";
        else if (green == 12) gFString = "C";
        else if (green == 13) gFString = "D";
        else if (green == 14) gFString = "E";
        else if (green == 15) gFString = "F";
        else gFString = String.valueOf(green);

        if (rgreen == 10) gSString = "A";
        else if (rgreen == 11) gSString = "B";
        else if (rgreen == 12) gSString = "C";
        else if (rgreen == 13) gSString = "D";
        else if (rgreen == 14) gSString = "E";
        else if (rgreen == 15) gSString = "F";
        else gSString = String.valueOf(rgreen);

        gFString = gFString + gSString;

        blue = b / 16;
        rblue = b % 16;

        if (blue == 10) bFString = "A";
        else if (blue == 11) bFString = "B";
        else if (blue == 12) bFString = "C";
        else if (blue == 13) bFString = "D";
        else if (blue == 14) bFString = "E";
        else if (blue == 15) bFString = "F";
        else bFString = String.valueOf(blue);

        if (rblue == 10) bSString = "A";
        else if (rblue == 11) bSString = "B";
        else if (rblue == 12) bSString = "C";
        else if (rblue == 13) bSString = "D";
        else if (rblue == 14) bSString = "E";
        else if (rblue == 15) bSString = "F";
        else bSString = String.valueOf(rblue);
        bFString = bFString + bSString;
        result = "#" + rFString + gFString + bFString;
        return result;

    }

    public static String getExceptionStackTraceMessage(Exception e) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString() + "\n");
        }
        return sb.toString();
    }

    public static String getExceptionStackTraceMessage(Exception e, Integer size) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString() + "\n");
        }
        if (sb.length() > size) {
            sb.setLength(size);
        }
        return sb.toString();
    }

    public static String getThreadStackTraceMessage() {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString() + "\n");
        }
        return sb.toString();
    }

    public static String getThreadStackTraceMessage(Integer size) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            sb.append(stackTraceElement.toString() + "\n");
        }
        if (sb.length() > size) {
            sb.setLength(size);
        }
        return sb.toString();
    }

    public static boolean compare(String s1, String s2) {
        boolean b = false;
        if (s1 == null && s2 == null) {
            b = true;
        } else if (s1 != null && s2 != null && s1.equals(s2)) {
            b = true;
        }
        return b;
    }

    /**
     * 空字符串
     */
    private static final String NULLSTR = "";

    /**
     * 下划线
     */
    private static final char SEPARATOR = '_';


    /**
     * @param phone 字符串类型的手机号
     *              传入手机号,判断后返回
     *              true为手机号,false相反
     */
    public static boolean isPhone(String phone) {
        String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            return m.matches();
        }
    }

    /**
     * 获取参数不为空值
     *
     * @param value defaultValue 要判断的value
     * @return value 返回值
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * * 判断一个Collection是否为空， 包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }

    /**
     * * 判断一个Collection是否非空，包含List，Set，Queue
     *
     * @param coll 要判断的Collection
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * * 判断一个对象数组是否为空
     *
     * @param objects 要判断的对象数组
     *                * @return true：为空 false：非空
     */
    public static boolean isEmpty(Object[] objects) {
        return isNull(objects) || (objects.length == 0);
    }

    /**
     * * 判断一个对象数组是否非空
     *
     * @param objects 要判断的对象数组
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：为空 false：非空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    /**
     * * 判断一个Map是否为空
     *
     * @param map 要判断的Map
     * @return true：非空 false：空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }


    /**
     * * 判断一个字符串是否为非空串
     *
     * @param str String
     * @return true：非空串 false：空串
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * * 判断一个对象是否为空
     *
     * @param object Object
     * @return true：为空 false：非空
     */
    public static boolean isNull(Object object) {
        return object == null;
    }

    /**
     * * 判断一个对象是否非空
     *
     * @param object Object
     * @return true：非空 false：空
     */
    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    /**
     * * 判断一个对象是否是数组类型（Java基本型别的数组）
     *
     * @param object 对象
     * @return true：是数组 false：不是数组
     */
    public static boolean isArray(Object object) {
        return isNotNull(object) && object.getClass().isArray();
    }

    /**
     * 去空格
     */
    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    /**
     * 截取字符串
     *
     * @param str   字符串
     * @param start 开始
     * @return 结果
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return NULLSTR;
        }

        if (start < 0) {
            start = str.length() + start;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return NULLSTR;
        }

        return str.substring(start);
    }


    public static String lowerFirst(String oldStr) {

        char[] chars = oldStr.toCharArray();

        chars[0] += 32;

        return String.valueOf(chars);

    }


    /**
     * 下划线转驼峰命名
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        // 前置字符是否大写
        boolean preCharIsUpperCase = true;
        // 当前字符是否大写
        boolean curreCharIsUpperCase = true;
        // 下一字符是否大写
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            } else {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1)) {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字符串
     *
     * @param str  验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式。如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        // 快速检查
        if (name == null || name.isEmpty()) {
            // 没必要转换
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线，仅将首字母大写
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        // 用下划线将原始字符串分割
        String[] camels = name.split("_");
        for (String camel : camels) {
            // 跳过原始字符串中开头、结尾的下换线或双重下划线
            if (camel.isEmpty()) {
                continue;
            }
            // 首字母大写
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 驼峰式命名法 例如：user_name->userName
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * 为空或者小于等于0 返回true     不等于空且大于0 返回false
     *
     * @param practiceOrderId
     * @return
     */
    public static boolean isEmptyLong(Long practiceOrderId) {
        try {
            if (practiceOrderId == null || practiceOrderId <= 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static Long valueOfLong(Long i) {
        if (i == null) {
            return 0L;
        }
        return i;
    }

    /*
     * 删除末尾字符串
     */
    public static String trimEnd(String inStr, String suffix) {
        while (inStr.endsWith(suffix)) {
            inStr = inStr.substring(0, inStr.length() - suffix.length());
        }
        return inStr;
    }


    public static boolean isInteger(String companyId) {
        // TODO Auto-generated method stub
        return false;
    }

    //判断字符串是否为全数字
    public static boolean isNumeric(String cs) {
        // 判断是否为空，如果为空则返回false
        if (isEmpty(cs)) {
            return false;
        }
        // 通过 length() 方法计算cs传入进来的字符串的长度，并将字符串长度存放到sz中
        int sz = cs.length();
        // 通过字符串长度循环
        for (int i = 0; i < sz; i++) {
            // 判断每一个字符是否为数字，如果其中有一个字符不满足，则返回false
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        // 验证全部通过则返回true
        return true;
    }
    public static String transSqlValue(String value) {
        if(value == null) {
            return null;
        }
        return "'"+value+"'";
    }
    public static String transSqlValue(Date date, SimpleDateFormat yyyyMMddHHmmss) {
        if(date == null) {
            return null;
        }
        return "'"+yyyyMMddHHmmss.format(date)+"'";
    }
    public static String transSqlValue(Date date) {
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(date == null) {
            return null;
        }
        return "'"+yyyyMMddHHmmss.format(date)+"'";
    }
    public static String transSqlValue(Integer value) {
        if(value == null) {
            return null;
        }
        return value.toString();
    }
    public static String transSqlValue(BigInteger value) {
        if(value == null) {
            return null;
        }
        return value.toString();
    }
    public static String transSqlValue(Long value) {
        if(value == null) {
            return null;
        }
        return value.toString();
    }
    public static String transSqlValue(Double value) {
        if(value == null) {
            return null;
        }
        return value.toString();
    }
    public static String montageInsertSql(String... ts) {
        StringBuilder sb = new StringBuilder("(");
        for(Object t:ts) {
            sb.append(t + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }
    /**
     * 判断字符串是否包含 emoji 或者 其他非文字类型的字符
     * @param source
     * @return
     */
    public static boolean containsEmoji(String source) {
        int len = source.length();
        boolean isEmoji = false;
        for (int i = 0; i < len; i++) {
            char hs = source.charAt(i);
            if (0xd800 <= hs && hs <= 0xdbff) {
                if (source.length() > 1) {
                    char ls = source.charAt(i + 1);
                    int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
                    if (0x1d000 <= uc && uc <= 0x1f77f) {
                        return true;
                    }
                }
            } else {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
                    return true;
                } else if (0x2B05 <= hs && hs <= 0x2b07) {
                    return true;
                } else if (0x2934 <= hs && hs <= 0x2935) {
                    return true;
                } else if (0x3297 <= hs && hs <= 0x3299) {
                    return true;
                } else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c
                        || hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
                    return true;
                }
                if (!isEmoji && source.length() > 1 && i < source.length() - 1) {
                    char ls = source.charAt(i + 1);
                    if (ls == 0x20e3) {
                        return true;
                    }
                }
            }
        }
        return isEmoji;
    }
    public static boolean isASCII(String s){
        boolean ret = true;
        for(int i = 0; i < s.length() ; i++) {
            if(s.charAt(i)>=128){
                ret = false;
                break;
            }
        }
        return ret;
    }
    private static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
                || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }
    // 纯数字
    private static String DIGIT_REGEX = "[0-9]+";
    // 含有数字
    private static String CONTAIN_DIGIT_REGEX = ".*[0-9].*";
    // 纯字母
    private static String LETTER_REGEX = "[a-zA-Z]+";
    // 包含字母
    private static String CONTAIN_LETTER_REGEX = ".*[a-zA-z].*";
    // 纯中文
    private static String CHINESE_REGEX = "[\u4e00-\u9fa5]";
    // 仅仅包含字母和数字
    private static String LETTER_DIGIT_REGEX = "^[a-z0-9A-Z]+$";
    public static boolean onlyLetter(String cardNum) {
        Matcher m=Pattern.compile(LETTER_REGEX).matcher(cardNum);
        return m.matches();
    }
    public static boolean containsLetter(String cardNum) {
        Matcher m=Pattern.compile(CONTAIN_LETTER_REGEX).matcher(cardNum);
        return m.matches();
    }
    public static boolean containsNumber(String cardNum) {
        Matcher m=Pattern.compile(CONTAIN_DIGIT_REGEX).matcher(cardNum);
        return m.matches();
    }
    /**
     * 字符串是否包含不可见字符
     * ""
     * @param str 被检测的字符串
     * @return 是否为空
     */
    public static boolean containInvisibles(String str) {
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (CharUtil.isBlankChar(str.charAt(i))) {
                return true;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String str = "\uD83D\uDD74\u200D♂.eth";
        System.out.println(containInvisibles(str));
    }
}
