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
import android.support.v7.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * EditTextPreference, welches die eingegebenen Daten in der Summary anzeigt.
 */
public class EditTextPreferenceMain extends EditTextPreference implements MainPreferenceInterface {
    public EditTextPreferenceMain(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditTextPreferenceMain(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreferenceMain(Context context) {
        super(context);
    }

    /**
     * Set die Summary der Preference.
     */
    @Override
    public String getSummaryText() {
        return getPersistedString(null);
    }
}

