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

package de.aw.awlib.preferences;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import de.aw.awlib.R;
import de.aw.awlib.fragments.AWLibFragment;

/**
 * EditTextPreferenceTime: Liest eine Uhrzeit und speichert diese als Long in Preferences. Gibt es
 * einen Default-Wert (Format: HH:mm), wird dieser uebernommen. Ist das Format falsch oder kein
 * DefaultWert vorgegeben, dann wird 00:00 angenommen
 */
public class PreferenceEditTimeDialog extends AWLibFragment
        implements TimePickerDialog.OnTimeSetListener {
    private int mHour;
    private String mKey;
    private int mMinute;

    public static PreferenceEditTimeDialog newInstance(Preference preference) {
        PreferenceEditTimeDialog fragment = new PreferenceEditTimeDialog();
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mKey = args.getString("key");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String[] split = prefs.getString(mKey, "00:00").split(":");
        mHour = Integer.parseInt(split[0]);
        mMinute = Integer.parseInt(split[1]);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dlg = new TimePickerDialog(getActivity(), this, mHour, mMinute,
                DateFormat.is24HourFormat(getActivity()));
        dlg.setTitle(R.string.dlgTitleTime);
        return dlg;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (!isCanceled) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(mKey,
                    String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)).apply();
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHour = hourOfDay;
        mMinute = minute;
    }
}