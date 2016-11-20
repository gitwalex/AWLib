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

/**
 * EditTextPreferenceTime: Liest eine Uhrzeit und speichert diese als Long in Preferences. Gibt es
 * einen Default-Wert (Format: HH:mm), wird dieser uebernommen. Ist kein DefaultWert vorgegeben,
 * dann wird 00:00 angenommen
 */
public class EditTextPreferenceTime extends DialogPreference {
    public EditTextPreferenceTime(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public EditTextPreferenceTime(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
    }

    @Override
    public CharSequence getSummary() {
        return getPersistedString("00:00");
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /**
     * Belegt den Initialwert fuer Time. Gibt es einen Default-Wert (Format: HH:mm), wird dieser
     * uebernommen. Ist kein DefaultWert vorgegeben, dann wird 00:00 angenommen
     */
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String value;
        if (restoreValue) {
            if (defaultValue == null) {
                value = getPersistedString("00:00");
            } else {
                value = defaultValue.toString();
            }
        } else {
            if (defaultValue == null) {
                value = "00:00";
            } else {
                value = defaultValue.toString();
            }
        }
        persistString(value);
        setSummary(getSummary());
    }
}