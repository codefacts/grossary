package com.imslbd.grossary.util;

import io.crm.promise.Decision;
import io.crm.promise.Promises;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by shahadat on 1/18/16.
 */
public class SqlUtils {
    private static final String ISO_DATE_FORMAT_STRING = "yyyy-MM-dd'T'00:00:00'Z'";
    private static final String ISO_DATE_FORMAT_STRING2 = "yyyy-MM-dd";
    private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return new SimpleDateFormat(ISO_DATE_FORMAT_STRING);
        }
    };

    private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL2 = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return new SimpleDateFormat(ISO_DATE_FORMAT_STRING2);
        }
    };

    public static String dateBetween(String field, Date from, Date to) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(to);
        instance.add(Calendar.DATE, 1);
        return "'" + toIsoDateStringWithTail(from) + "' <= " + field + " AND " + field + " < '" + toIsoDateStringWithTail(instance.getTime()) + "'";
    }

    public static String dateBetweenToday(String field) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 1);
        return "'" + toIsoDateStringWithTail(new Date()) + "' <= " + field + " AND " + field + " < '" + toIsoDateStringWithTail(instance.getTime()) + "'";
    }

    public static String toIsoDateStringWithTail(Date date) {
        return DATE_FORMAT_THREAD_LOCAL.get().format(date);
    }

    public static String toIsoDateString(Date date) {
        return DATE_FORMAT_THREAD_LOCAL2.get().format(date);
    }

    public static void main(String[] afg) {
        Promises.from()
            .decideAndMap(v -> Decision.of("ok", "go it"))
        ;
    }

    public static String escf(String field) {
        return "`" + field + "`";
    }
}
