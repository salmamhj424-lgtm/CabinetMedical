package com.example.cabinetmedical;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static final long NO_DATE = Long.MIN_VALUE;

    public static Date parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        f.setLenient(false);
        try {
            return f.parse(s.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String format(Calendar c) {
        return String.format(Locale.FRANCE, "%02d/%02d/%04d",
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    public static String addDays(int n) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, n);
        return format(c);
    }

    public static String today() {
        return addDays(0);
    }

    private static void zero(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    /** Nombre de jours entre aujourd'hui et la date (negatif = passe). NO_DATE si invalide. */
    public static long daysFromToday(String s) {
        Date d = parse(s);
        if (d == null) {
            return NO_DATE;
        }
        Calendar a = Calendar.getInstance();
        zero(a);
        Calendar b = Calendar.getInstance();
        b.setTime(d);
        zero(b);
        return Math.round((b.getTimeInMillis() - a.getTimeInMillis()) / 86400000.0);
    }

    public static String describe(long d) {
        if (d == NO_DATE) {
            return "";
        }
        if (d == 0) {
            return "Aujourd'hui";
        }
        if (d == 1) {
            return "Demain";
        }
        if (d == -1) {
            return "Hier";
        }
        if (d > 1) {
            return "Dans " + d + " jours";
        }
        return "En retard de " + (-d) + " jours";
    }

    public static int color(long d) {
        if (d == NO_DATE) {
            return 0xFF666666;
        }
        if (d < 0) {
            return 0xFFD32F2F;
        }
        if (d == 0) {
            return 0xFFEF6C00;
        }
        return 0xFF1976D2;
    }

    /** Cle de mois (annee * 12 + mois) d'une date jj/MM/aaaa, ou -1 si invalide. */
    public static int monthKey(String s) {
        java.util.Date d = parse(s);
        if (d == null) {
            return -1;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH);
    }

    public static int currentMonthKey() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH);
    }
}
