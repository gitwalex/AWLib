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
package de.aw.awlib.preferences;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.widget.DatePicker;

import java.sql.Date;
import java.util.Calendar;

import de.aw.awlib.R;
import de.aw.awlib.fragments.AWFragment;

/**
 * EditTextPreferenceTime: Liest eine Uhrzeit und speichert diese als Long in Preferences. Gibt es
 * einen Default-Wert (Format: HH:mm), wird dieser uebernommen. Ist das Format falsch oder kein
 * DefaultWert vorgegeben, dann wird 00:00 angenommen
 */
public class PreferenceEditDateDialog extends AWFragment
        implements DatePickerDialog.OnDateSetListener {
    private Calendar calendar;
    private String mKey;

    public static PreferenceEditDateDialog newInstance(Preference preference) {
        PreferenceEditDateDialog fragment = new PreferenceEditDateDialog();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Dialog dlg = new DatePickerDialog(getActivity(), this, year, month, day);
        dlg.setTitle(R.string.dlgTitleDate);
        return dlg;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!isCanceled) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(mKey, calendar.getTimeInMillis());
        }
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        mKey = args.getString("key");
        long value = prefs.getLong(mKey, System.currentTimeMillis());
        calendar = Calendar.getInstance();
        calendar.setTime(new Date(value));
    }
}