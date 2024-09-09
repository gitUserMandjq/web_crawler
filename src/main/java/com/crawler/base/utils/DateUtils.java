package com.crawler.base.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Locale;

public class DateUtils {
	public final static String HOUR = "hour";
	public final static String MINUTE = "minute";
	public final static String SECOND = "second";
	public static final Date MINDATE;
	public static final Date MAXDATE;
	static {
		Date mindate = null;
		Date maxdate = null;
		SimpleDateFormat yyyy_MM_dd = new SimpleDateFormat("yyyy-MM-dd");
		try {
			mindate = yyyy_MM_dd.parse("1999-1-1");
			maxdate = yyyy_MM_dd.parse("2199-1-1");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		MINDATE = mindate;
		MAXDATE = maxdate;
	}
    public static Date addDays(Date date, int add) {
    	if(date == null)
    		return null;
        Calendar c=Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE,add);
        return c.getTime();
    }
    public static Date add(Date date, int add, int type) {
    	if(date == null)
    		return null;
        Calendar c=Calendar.getInstance();
        c.setTime(date);
        c.add(type,add);
        return c.getTime();
    }
    public static Double getDiffTime(Date begin, Date end , String type) {
    	if(HOUR.equals(type)) {
    		double between=(end.getTime()-begin.getTime())/1000.0;//除以1000是为了转换成秒
    		double hour=between/60.0/60.0;
    		return hour;
    	}else if(MINUTE.equals(type)) {
    		double between=(end.getTime()-begin.getTime())/1000.0;//除以1000是为了转换成秒
    		double min=between/60.0;
    		return min;
    	}else if(SECOND.equals(type)) {
    		double second=(end.getTime()-begin.getTime())/1000.0;
    		return second;
    	}
    	return null;
    }
    public static Integer getDaysInMonth(Integer year, Integer month) {
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.YEAR, year);
    	c.set(Calendar.MONTH, month-1);
    	return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    public static Integer getDaysInYear(Integer year) {
    	Calendar c = Calendar.getInstance();
    	c.set(Calendar.YEAR, year);
    	return c.getActualMaximum(Calendar.DAY_OF_YEAR);
    }
    public static Integer getDayInWeek(Date date) {
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	return c.get(Calendar.DAY_OF_WEEK);
    }
    public static String GetWeekDay(int dayOfWeek) {
    	switch (dayOfWeek)
        {
            case 2:
                return "1";
            case 3:
                return "2";
            case 4:
                return "3";
            case 5:
                return "4";
            case 6:
                return "5";
            case 7:
                return "6";
            case 1:
                return "0";
            default:
                return "";
        }
    }
    public static String getDateString(Date date, String pattern) {
    	if(date == null)
    		return null;
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	return sdf.format(date);
    }
    public static Date format(String date, String pattern) throws ParseException {
    	if(date == null || "".equals(date.trim()))
    		return null;
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	return sdf.parse(date);
    }
    public static Date format(String date, SimpleDateFormat sdf) throws ParseException {
    	if(date == null || "".equals(date.trim()))
    		return null;
    	return sdf.parse(date);
    }
    public static String getDateString(Date date, SimpleDateFormat sdf) {
    	if(date == null)
    		return null;
    	return sdf.format(date);
    }
    public static boolean compare(String date1, String date2, String pattern) throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	return sdf.parse(date1).getTime() > sdf.parse(date2).getTime();
    }
    public static boolean compare(String date1, String date2, SimpleDateFormat sdf) throws ParseException {
    	return sdf.parse(date1).getTime() > sdf.parse(date2).getTime();
    }
    public static double getTourHour(String date, String pattern) throws ParseException{
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	return getTourHour(date, sdf);
    }
    public static double getTourHour(String date, SimpleDateFormat sdf) throws ParseException{
    	Date parse = sdf.parse(date);
    	Calendar c = Calendar.getInstance();
    	c.setTime(parse);
    	return c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE)/60.0;
    }
    public static double getTourHour(Date date) throws ParseException{
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	return c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE)/60.0;
    }
	public static Integer getTourMinute(String date, SimpleDateFormat sdf) throws ParseException{
		Date parse = sdf.parse(date);
		Calendar c = Calendar.getInstance();
		c.setTime(parse);
		return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
	}
	public static Integer getTourMinute(Date date) throws ParseException{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
	}
    public static void main(String[] args) throws ParseException {
		System.out.println(getTourHour(new Date()));
	}
    public static Date longToDate(Long dateLong) {
    	if(dateLong != null)
    		return new Date(dateLong);
    	return null;
    }

	public static String getMonthLastDay( SimpleDateFormat simpleDateFormat) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		return simpleDateFormat.format(calendar.getTime());
	}



	public static void convertToDayHourOrHourMinOrSeconds4(StringBuilder time, Integer second) {
		if (second == null || second == 0) {
			return;
		}
		if (second >= 60*60*24) {
			int day = second/(60*60*24);
			time.append(day + "天");
			second = second%(60*60*24);
			convertToDayHourOrHourMinOrSeconds4(time, second);
		} else if(second >= 60*60) {
			int hour = second/(60*60);
			time.append(hour+"小时");
			second = second%(60*60);
			convertToDayHourOrHourMinOrSeconds4(time, second);
		} else if(second >= 60) {
			int minute = second/60;
			time.append(minute+"分钟");
			second = second%60;
			convertToDayHourOrHourMinOrSeconds4(time, second);
		} else {
			time.append(second + "秒");
			return;
		}
	}
    public static double getMinutes(Date date) {
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	return (c.get(Calendar.HOUR_OF_DAY) * 60.0) + c.get(Calendar.MINUTE);
    }
    public static BigDecimal getTourAddTime(String startTime) throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    	Date date = sdf.parse(startTime);
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	return new BigDecimal(c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE)/60.0);
    }
	public static Integer getTourAddTimeMinute(String startTime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date date = sdf.parse(startTime);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
	}
    public static Double getTourTime(String time, BigDecimal addTime) throws ParseException {
    	Double tourTime = getTourAddTime(time).doubleValue();
    	if(tourTime > addTime.doubleValue())
    		return tourTime;
    	else
    		return tourTime + 24.0;
    }

	/**
	 * 获取当月的最后一天
	 * @return
	 * @throws ParseException
	 */
	public static Date getMonthLast() throws ParseException {
		//当月的最后一天
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
		ca.set(Calendar.HOUR, 11);
		ca.set(Calendar.MINUTE, 59);
		ca.set(Calendar.SECOND, 59);
		ca.set(Calendar.MILLISECOND, 999);
		return ca.getTime();
	}

	public static Date getToDayLast() throws ParseException {
		//当天最后时间
		Calendar calendar2 = Calendar.getInstance();

		calendar2.set(calendar2.get(Calendar.YEAR), calendar2.get(Calendar.MONTH), calendar2.get(Calendar.DAY_OF_MONTH),
				23, 59, 59);
		Date endOfDate = calendar2.getTime();
		return endOfDate;
	}

	public static Date getWeekLast() throws ParseException {
		//当周的最后一天
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
		ca.set(Calendar.HOUR, 11);
		ca.set(Calendar.MINUTE, 59);
		ca.set(Calendar.SECOND, 59);
		ca.set(Calendar.MILLISECOND, 999);
		return ca.getTime();
	}

	/**
	 * 根据传入日期判断星期日多少
	 * @param data
	 * @return
	 * @throws ParseException
	 */
	public static Date getWeekLastByData(Date data) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //设置时间格式

		Calendar cal = Calendar.getInstance();
		Date time = sdf.parse(sdf.format(data));
		cal.setTime(time);
//		System.out.println("要计算日期为:" + sdf.format(cal.getTime())); //输出要计算日期
//判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
		int dayWeek = cal.get(Calendar.DAY_OF_WEEK);//获得当前日期是一个星期的第几天
		if (1 == dayWeek) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		cal.setFirstDayOfWeek(Calendar.MONDAY);//设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
		int day = cal.get(Calendar.DAY_OF_WEEK);//获得当前日期是一个星期的第几天
		cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);//根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
	//	System.out.println("所在周星期一的日期：" + sdf.format(cal.getTime()));
		System.out.println(cal.getFirstDayOfWeek() + "-" + day + "+6=" + (cal.getFirstDayOfWeek() - day + 6));
		cal.add(Calendar.DATE, 6);
		System.out.println("所在周星期日的日期：" + sdf.format(cal.getTime()));
		cal.set(Calendar.HOUR, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal.getTime();
	}


    public static Date parseAndFormat(Date date, String sdf) {
    	if(StringUtils.isEmpty(sdf))
    		return null;
    	return parseAndFormat(date, new SimpleDateFormat(sdf));
    }
    public static Date parseAndFormat(Date date, SimpleDateFormat sdf) {
    	try {
    		return sdf.parse(sdf.format(date));
		} catch (Exception e) {
			return null;
		}
    }


    //以下来自于alert
	public static Logger logger = LoggerFactory.getLogger(DateUtils.class);

    public static String format(Date date, String pattern) {
    	if(date == null)
    		return null;
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	return sdf.format(date);
    }
    public static Date parse(String date, String pattern) throws ParseException {
    	if(date == null || "".equals(date.trim()))
    		return null;
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	return sdf.parse(date);
    }
    public static String format(Date date, SimpleDateFormat sdf) {
    	if(date == null)
    		return null;
    	return sdf.format(date);
    }

    public static boolean betweenHourMinute(String startTime, String endTime, Date date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		Date sTime = sdf.parse(startTime);
		Date eTime = sdf.parse(endTime);
		if(eTime.getTime() <= sTime.getTime()) {
			eTime = addDays(eTime, 1);
		}
		date = parseAndFormat(date, sdf);
		if(date.getTime() <= sTime.getTime()) {
			date = addDays(date, 1);
		}
		if(date.getTime() >= sTime.getTime() && date.getTime() <= eTime.getTime())
			return true;
		else
			return false;
    }

    //

    public static boolean equals(Date a, Date b, String pattern) {
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	if(a == null && b == null) {
    		return true;
    	}else if(a == null || b == null) {
    		return false;
    	}
    	if(sdf.format(a).equals(sdf.format(b))) {
    		return true;
    	}
    	return false;
    }


    private static long getWeeOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

	/**
	 *
	 * @param nowTime   欧安的时间
	 * @param startTime	开始时间
	 * @param endTime   结束时间
	 * @return
	 * @author sunran   判断当前时间在时间区间内
	 */
	public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
		if (nowTime.getTime() == startTime.getTime()
				|| nowTime.getTime() == endTime.getTime()) {
			return true;
		}

		Calendar date = Calendar.getInstance();
		date.setTime(nowTime);

		Calendar begin = Calendar.getInstance();
		begin.setTime(startTime);

		Calendar end = Calendar.getInstance();
		end.setTime(endTime);

		if (date.after(begin) && date.before(end)) {
			return true;
		} else {
			return false;
		}
	}

    public enum DateSpanEnum{
    	STARTTIME,ENDTIME;
    }
    public static EnumMap<DateSpanEnum, Date> getDateSpan(Integer year){
    	return getDateSpan(StringUtils.valueOf(year));
    }
    public static EnumMap<DateSpanEnum, Date> getDateSpan(String year){
    	EnumMap<DateSpanEnum, Date> enumMap = new EnumMap<>(DateSpanEnum.class);
    	if(year == null) {
    		return enumMap;
    	}
    	Calendar c = Calendar.getInstance();
    	try {
			c.setTime(format(year, "yyyy"));
		} catch (ParseException e) {
			return enumMap;
		}
    	enumMap.put(DateSpanEnum.STARTTIME, c.getTime());
    	c.add(Calendar.YEAR, 1);
    	enumMap.put(DateSpanEnum.ENDTIME, c.getTime());
    	return enumMap;
    }
    public static EnumMap<DateSpanEnum, Date> getDateSpan(Integer year, Integer month){
    	return getDateSpan(StringUtils.valueOf(year), StringUtils.valueOf(month));
    }
    public static EnumMap<DateSpanEnum, Date> getDateSpan(String year, String month){
    	EnumMap<DateSpanEnum, Date> enumMap = new EnumMap<>(DateSpanEnum.class);
    	if(year == null || month == null) {
    		return enumMap;
    	}
    	Calendar c = Calendar.getInstance();
    	try {
			c.setTime(format(year+"-"+month, "yyyy-MM"));
		} catch (ParseException e) {
			return enumMap;
		}
    	enumMap.put(DateSpanEnum.STARTTIME, c.getTime());
    	c.add(Calendar.MONTH, 1);
    	enumMap.put(DateSpanEnum.ENDTIME, c.getTime());
    	return enumMap;
    }
    public static Integer getYearWeek(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	return getYearWeek(calendar);
    }
    public static Integer getYearWeek(Calendar calendar) {
    	calendar.setFirstDayOfWeek(Calendar.MONDAY);
    	calendar.setMinimalDaysInFirstWeek(4);
    	int week = calendar.get(Calendar.WEEK_OF_YEAR);
    	int year = calendar.get(Calendar.YEAR);
    	calendar.add(Calendar.DATE, -7);//获得上一周的周数
    	int lastWeek = calendar.get(Calendar.WEEK_OF_YEAR);
    	int lastYear = calendar.get(Calendar.YEAR);
    	if(week < lastWeek && year == lastYear) {//周数小于上一周，年数不变说明时间在年末，但是周数已经算到明年，年数需要+1
    		year += 1;
    	}
    	if(week > lastWeek && year > lastYear && lastWeek != 1) {//周数大于上周，年数也大于上周，并且上周不等于1，说明时间在新年的一月，但是周数还是去年的，年数需要-1
    		year -= 1;
    	}
    	return year * 100 + week;
    }
    public static Calendar initCalendar(Date date) {
    	if(date == null)
    		return null;
        Calendar c=Calendar.getInstance();
        c.setTime(date);
        return c;
    }
    public static Date getSameTermStartDate(Date date) {
    	Calendar calendar = initCalendar(date);
    	int dayNo = calendar.get(Calendar.DATE);
    	calendar.add(Calendar.MONTH, -1);
    	if(dayNo > calendar.get(Calendar.DATE)) {
    		calendar.add(Calendar.DATE, 1);
    	}
    	return calendar.getTime();
    }
    public static Date getSameTermEndDate(Date date) {
    	Calendar calendar = initCalendar(date);
    	calendar.add(Calendar.MONTH, -1);
    	return calendar.getTime();
    }

	public static Date getStartTime() {
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		return todayStart.getTime();
	}

	public static Date getEndTime() {
		Calendar todayEnd = Calendar.getInstance();
		todayEnd.set(Calendar.HOUR_OF_DAY,23);
		todayEnd.set(Calendar.MINUTE, 59);
		todayEnd.set(Calendar.SECOND, 59);
		todayEnd.set(Calendar.MILLISECOND, 999);
		return todayEnd.getTime();
	}
	/**
	 * 将时间处理成以0或者5结尾
	 * @param startTime
	 * @return
	 */
	public static Date getTimeMinutesEndWith0or5(Date startTime) {
		Calendar initCalendar = initCalendar(startTime);
		int minutes = initCalendar.get(Calendar.MINUTE);
		minutes = minutes - minutes%5;
		initCalendar.set(Calendar.MINUTE, minutes);
		initCalendar.set(Calendar.SECOND, 0);
		initCalendar.set(Calendar.MILLISECOND, 0);
		return initCalendar.getTime();
	}

	//时间取整到日
	public static Date roundToDay(Date d) {
		Long createTime = d.getTime();
		Long time = createTime - ((createTime + 28800000) % (86400000));
		return new Date(time);
	}

	/**
	 * 消除毫秒
	 * @param date
	 * @return
	 */
	public static Date removeMillisecond(Date date){
		if(date == null){
			return null;
		}
		Calendar calendar = initCalendar(date);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
