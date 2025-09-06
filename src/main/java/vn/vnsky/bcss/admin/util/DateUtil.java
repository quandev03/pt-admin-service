package vn.vnsky.bcss.admin.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

@Slf4j
@UtilityClass
public class DateUtil {

    public static Date convertStringToDate(String date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            return dateFormat.parse(date);

        } catch (ParseException e) {
            log.error("convertStringToDate", e);
        }
        return null;
    }

    public String convertDateToString(Date date, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.format(date);
        } catch (Exception ex) {
            log.error("DateUtils.convertDateToString ERR" + ex.getMessage(), ex);
            return null;
        }
    }

    public Date formatDate(Date date, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            return format.parse(format.format(date));
        } catch (Exception ex) {
            log.error("DateUtils.convertDateToString ERR" + ex.getMessage(), ex);
            return null;
        }
    }

    public LocalDateTime convertDateToLocalDateTime(Date date) {
        try {
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception ex) {
            log.error("convertDateToLocalDateTime ERR: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public LocalDate convertDateToLocalDate(Date date) {
        try {
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime().toLocalDate();
        } catch (Exception ex) {
            log.error("convertDateToLocalDate ERR" + ex.getMessage(), ex);
            return null;
        }
    }

    public String convertToString(String date, String patternFrom, String patternTo) {
        try {
            SimpleDateFormat formatFrom = new SimpleDateFormat(patternFrom);
            SimpleDateFormat formatTo = new SimpleDateFormat(patternTo);
            return formatTo.format(formatFrom.parse(date));
        } catch (Exception ex) {
            log.error("DateUtils.convertToString ERR" + ex.getMessage(), ex);
            return null;
        }
    }

    public Date setStartDateTimeInMonth(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        return setTimeStartDate(cal.getTime());
    }

    public Date setEndDateTimeInMonth(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        return setTimeEndDate(cal.getTime());
    }


    public Date setStartDateInMonth(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    public Date setEndDateInMonth(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        return cal.getTime();
    }

    public Date setTimeStartDate(Date startDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public Date setTimeEndDate(Date endDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public static Date setFirstDayInMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    public static Date setEndDayInMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

        return cal.getTime();
    }

    public static Date setHours(Date date, int hours) {
        return setMinute(date, hours, 0);
    }

    public static Date setMinute(Date date, int hours, int minutes) {
        return setSecond(date, hours, minutes, 0);
    }

    public static Date setSecond(Date date, int hours, int minutes, int seconds) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    public static Date setTimeToDate(Date date, LocalTime localTime) {
        return asDate(localTime, asLocalDate(date));
    }

    /**
     * Check if a date is between two dates
     *
     * @param localTime Date which needs to be checked
     * @param from      Lower bound of the range
     * @param to        Upper bound of the range
     * @return TRUE/FALSE
     */
    public boolean isBetween(final LocalTime localTime,
                             final LocalTime from,
                             final LocalTime to) {
        final LocalTime currentFrom = from == null ? LocalTime.MIN : from;
        final LocalTime currentTo = to == null ? LocalTime.MAX : to;
        return (localTime.isAfter(currentFrom) || localTime.equals(currentFrom))
               && (localTime.isBefore(currentTo) || localTime.equals(currentTo));
    }

    /**
     * Gets the maximum of the dates.
     *
     * @param value extends Comparable
     * @return Max of all Object. Will return {@link null } in case no value is passed
     */
    public <T extends Comparable<T>> T getMaxValue(final List<T> value) {
        return value
                .stream()
                .filter(Objects::nonNull)
                .max(T::compareTo)
                .orElse(null);
    }

    /**
     * Gets the min of the dates.
     *
     * @param value extends Comparable
     * @return Min of all Object. Will return {@link null } in case no value is passed
     */
    public <T extends Comparable<T>> T getMinValue(final List<T> value) {
        return value
                .stream()
                .filter(Objects::nonNull)
                .min(T::compareTo)
                .orElse(null);
    }

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        if (date == null) return null;
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalTime asLocalTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalTime();
    }

    public static Date asDate(LocalTime localTime, LocalDate localDate) {
        Instant instant = localTime.atDate(localDate).
                atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public static Integer getWorkingDayInMonth(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        Integer specifiedWorkingDay = 0;

        while (startCalendar.compareTo(endCalendar) < 1) {
            int dayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY) {
                specifiedWorkingDay++;
            }
            startCalendar.add(Calendar.DATE, 1);
        }

        return specifiedWorkingDay;
    }

    /*Function to check if given date is weekend or not*/
    public static boolean isWeekEnd(LocalDate localDate) {
        String dayOfWeek = localDate.getDayOfWeek().toString();
        return "SATURDAY".equalsIgnoreCase(dayOfWeek) ||
               "SUNDAY".equalsIgnoreCase(dayOfWeek);
    }

    public Date setDateInMonth(Integer date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, date);

        return cal.getTime();
    }

    public Date setDateInMonthPoint(Integer date, Date month) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        cal.set(Calendar.DAY_OF_MONTH, date);

        return cal.getTime();
    }

    public String getTimezoneOffset() {
        int millis = LocaleContextHolder.getTimeZone().getRawOffset();
        int minutes = (millis / (1000 * 60)) % 60;
        int hours = (millis / (1000 * 60 * 60)) % 24;
        return String.format("%s%02d:%02d", (millis >= 0 ? "+" : "-"), hours, minutes);
    }

    public Time setTime(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return new Time(calendar.getTimeInMillis());
    }
}
