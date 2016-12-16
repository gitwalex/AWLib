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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import de.aw.awlib.R;

/**
 * EditTextPreferenceTime: Liest eine Uhrzeit und speichert diese als Long in Preferences. Gibt es
 * einen Default-Wert (Format: HH:mm), wird dieser uebernommen. Ist das Format falsch oder kein
 * DefaultWert vorgegeben, dann wird 00:00 angenommen
 */
public class TimePreference extends DialogPreference {
    private int mDialogLayoutResId = R.layout.pref_dialog_time;
    private int mTime;

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context context) {
        this(context, null);
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    public int getTime() {
        return mTime;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        setTime(restorePersistedValue ? getPersistedInt(mTime) : (int) defaultValue);
    }

    public void setTime(int time) {
        mTime = time;
        persistInt(time);
    }
}