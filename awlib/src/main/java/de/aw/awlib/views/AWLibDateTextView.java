/*
 * MonMa: Eine freie Android-App fuer Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 23730 Neustadt/Germany]
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
package de.aw.awlib.views;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.database.AbstractDBConvert;

/**
 * TextView fuer Eingabe Datum. Bei Klick wird ein DatePickerDialog gezeigt und eine Aenderung durch
 * dem OnDateTextViewListener bekanntgegeben
 */
public class AWLibDateTextView extends TextView
        implements AWLibInterface, OnDateSetListener, OnClickListener {
    private Calendar cal = Calendar.getInstance();
    private OnDateTextViewListener mOnDateSetListener;
    private int year, month, day;

    public AWLibDateTextView(Context context) {
        super(context);
    }

    public AWLibDateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWLibDateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @return Liefert das aktuell angezeigte Datum zurueck
     */
    public Date getDate() {
        return cal.getTime();
    }

    /**
     * Startet den DatumsDialog
     */
    @Override
    public void onClick(View v) {
        if (isFocusable()) {
            DatePickerDialog dialog = new DatePickerDialog(getContext(), this, year, month, day);
            dialog.getDatePicker().setCalendarViewShown(false);
            dialog.show();
        }
    }

    /**
     * Wird gerufen, wenn das Datum im Dialog gesetzt wurde. In diesem Fall wird der Listener
     * gerufen, dass sich das Datum geaendert hat.
     */
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        setDate(year, monthOfYear, dayOfMonth);
        mOnDateSetListener.onDateChanged(this, cal);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        super.setOnClickListener(this);
        setFocusable(true);
        setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
    }

    /**
     * Setzt das uebergebene Datum. Die Zeit wird auf 00:00 gesetzt
     *
     * @param date
     *         Datum
     */
    public void setDate(Date date) {
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        super.setText(AbstractDBConvert.convertDate(date));
    }

    /**
     * Setzt das uebergebene Datum.
     *
     * @param date
     *         Datum im SQLiteFormat
     *
     * @throws ParseException,
     *         wenn das Datum nicht geparst werden kann
     */
    public void setDate(String date) throws ParseException {
        Date d = AbstractDBConvert.mSqliteDateFormat.parse(date);
        setDate(d);
    }

    /**
     * Setzt die uebergebenen Werte als Datum
     *
     * @param year
     *         Jahr
     * @param month
     *         Monat
     * @param day
     *         Tag des Monats
     */
    public void setDate(int year, int month, int day) {
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        setDate(cal.getTime());
    }

    /**
     * OnClickListener wird nicht beachtet. Stattdessen {@link OnDateTextViewListener}
     * implementieren
     *
     * @throws IllegalArgumentException
     *         bei jedem Aufruf.
     */
    @Override
    public void setOnClickListener(OnClickListener l) {
        throw new IllegalArgumentException("Nicht moeglich");
    }

    /**
     * Registriert einen {@link AWLibDateTextView.OnDateTextViewListener}
     *
     * @param l
     *         OnDateTextViewListener
     */
    public void setOnDateChangedListener(OnDateTextViewListener l) {
        mOnDateSetListener = l;
    }

    /**
     * Wird gerufen, wenn sich das Datum geaendert hat.
     */
    public interface OnDateTextViewListener {
        /**
         * Wird gerufen, wenn das Datum eingegeben wurde.
         *
         * @param view
         *         View
         * @param cal
         *         Calendar mit neuem Datum
         */
        void onDateChanged(AWLibDateTextView view, Calendar cal);
    }
}
