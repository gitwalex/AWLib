/*
 * MonMa: Eine freie Android-Application fuer die Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */

package de.aw.awlib.database;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.aw.awlib.application.AWApplication;

/**
 * Klasse zum konvertieren von Daten in ein anderes Format
 */
public class AWDBConvert {
    protected static final Locale mLocale = Locale.getDefault();
    public static final DateFormat mSqliteDateFormat = new SimpleDateFormat("yyyy-MM-dd", mLocale);
    private static final DateFormat DATEFORMAT = DateFormat.getDateInstance();
    private static final DateFormat mDateFormat = new SimpleDateFormat("dd.MM.yyyy", mLocale);
    private static final DecimalFormat CURRENCYFORMAT =
            (DecimalFormat) NumberFormat.getCurrencyInstance(mLocale);
    private static final DecimalFormat DECIMALFORMAT =
            (DecimalFormat) NumberFormat.getNumberInstance(mLocale);
    private static final DecimalFormat PERCENTFORMAT =
            (DecimalFormat) NumberFormat.getPercentInstance(mLocale);
    public static double mCurrencyDigits;
    public static double mNumberDigits;

    static {
        mSqliteDateFormat.setTimeZone(TimeZone.getDefault());
        DECIMALFORMAT.setMaximumFractionDigits(3);
        int defaultFractionDigits =
                Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits();
        mCurrencyDigits = (long) Math.pow(10, defaultFractionDigits);
        mNumberDigits = (long) Math.pow(10, 6);
        PERCENTFORMAT.setMaximumFractionDigits(2);
        PERCENTFORMAT.setMinimumFractionDigits(2);
        // PERCENTFORMAT.applyPattern("#.##%");
    }

    protected static void Log(String message) {
        AWApplication.Log(message);
    }

    public static String convertCurrency(long amount) {
        return CURRENCYFORMAT.format(amount / mCurrencyDigits);
    }

    /**
     * Konvertiert Datum in Millis nach TT.MM.YYY
     *
     * @param millis
     *         Datum in Millis
     *
     * @return Datum im Format TT.MM.YYYY
     */
    public static String convertDate(long millis) {
        return DATEFORMAT.format(millis);
    }

    /**
     * @param date
     *         Datum
     *
     * @return Date als String im Format dd.MM.YYYY
     */
    public static String convertDate(Date date) {
        return mDateFormat.format(date);
    }

    /**
     * Konvertiert ein Datum aus der DB in lesbares Datum
     *
     * @param value
     *         Datum im SQLite-Format
     *
     * @return Datum als String
     */
    public static String convertDate(String value) {
        try {
            Date date = mSqliteDateFormat.parse(value);
            return mDateFormat.format(date);
        } catch (ParseException e) {
            Log("ParseException in convertDate: " + value);
            return value;
        }
    }

    public static String convertDate2SQLiteDate(Date date) {
        java.sql.Date d = new java.sql.Date(date.getTime());
        return d.toString();
    }

    /**
     * @param number
     *         number
     *
     * @return Liefert eine Zahl (mit entsprechenden Nachkommastellen) als String zurueck. Damit
     * kann z.B. die Menge WP angezeigt werden.
     */
    public static String convertNumber(long number) {
        return DECIMALFORMAT.format(number / mNumberDigits);
    }

    public static String convertNumber(String number) {
        try {
            Long value = Long.parseLong(number);
            return convertNumber(value);
        } catch (NumberFormatException e) {
            Log("NumberFormatException in convertNumber: " + number);
        }
        return number;
    }

    public static String convertPercent(long value) {
        double amount = (double) value / 10000;
        return PERCENTFORMAT.format(amount);
    }

    public static String convertPercent(String amount) {
        if (amount == null) {
            return convertPercent(0);
        }
        try {
            Long value = Long.parseLong(amount);
            return convertPercent(value);
        } catch (NumberFormatException e) {
            Log("NumberFormatException in convertPercent: " + amount);
        }
        return amount;
    }

    public static double getNumberDigits() {
        return mNumberDigits;
    }
}
