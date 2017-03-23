package de.aw.awlib.views;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
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

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.database.AWDBConvert;

/**
 * TextView fuer Eingabe Datum. Bei Klick wird ein DatePickerDialog gezeigt und eine Aenderung durch
 * dem OnDateTextViewListener bekanntgegeben
 */
public class AWDateTextView extends android.support.v7.widget.AppCompatTextView
        implements AWInterface, OnDateSetListener, OnClickListener {
    private Calendar cal = Calendar.getInstance();
    private OnDateTextViewListener mOnDateSetListener;
    private int year, month, day;

    @BindingAdapter({"onDateChanged"})
    public static void onDateChanged(AWDateTextView view, OnDateTextViewListener listener) {
        view.setOnDateChangedListener(listener);
    }

    public AWDateTextView(Context context) {
        super(context);
    }

    public AWDateTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AWDateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @return Liefert das aktuell angezeigte Datum zurueck
     */
    public Date getDate() {
        return cal.getTime();
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
        super.setText(AWDBConvert.convertDate(date));
    }

    /**
     * Startet den DatumsDialog
     */
    @Override
    public void onClick(View v) {
        if (isFocusable()) {
            DatePickerDialog mDatePickerDialog =
                    new DatePickerDialog(getContext(), this, year, month, day);
            mDatePickerDialog.getDatePicker().setCalendarViewShown(false);
            mDatePickerDialog.show();
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
     * Setzt das uebergebene Datum.
     *
     * @param date
     *         Datum im SQLiteFormat
     * @throws ParseException,
     *         wenn das Datum nicht geparst werden kann
     */
    public void setDate(String date) throws ParseException {
        Date d = AWDBConvert.mSqliteDateFormat.parse(date);
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
     * OnHolderClickListener wird nicht beachtet. Stattdessen {@link OnDateTextViewListener}
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
     * Registriert einen {@link AWDateTextView.OnDateTextViewListener}
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
        void onDateChanged(AWDateTextView view, Calendar cal);
    }
}
