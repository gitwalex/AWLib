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
import android.text.TextUtils;
import android.util.AttributeSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * EditTextPreference, welches die eingegebenen Daten als Prozentwert anzeigt. Created by alex on
 * 12.09.2015.
 */
public class EditTextPreferencePercent extends EditTextPreference
        implements MainPreferenceInterface {
    private static final Locale mLocale = Locale.getDefault();
    public static final DecimalFormat PERCENTFORMAT =
            (DecimalFormat) NumberFormat.getPercentInstance(mLocale);

    public EditTextPreferencePercent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public EditTextPreferencePercent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextPreferencePercent(Context context) {
        super(context);
        init();
    }

    /**
     * Liefert die maximal Anzahl der Stellen nach dem Komma. Siehe {@link
     * NumberFormat#setMaximumIntegerDigits(int)}
     *
     * @return Die maximale Anzahl der Stellen nach dem Komma. Default ist 1.
     */
    public int getMaximumFractionDigits() {
        return 1;
    }

    /**
     * Liefert die minimale Anzahl der Stellen nach dem Komma. Siehe {@link
     * NumberFormat#setMinimumFractionDigits(int)}
     *
     * @return Die minimale Anzahl der Stellen nach dem Komma. Default ist 0.
     */
    public int getMinimumFractionDigits() {
        return 0;
    }

    /**
     * Set die Summary der Preference als Prozentwert.
     */
    @Override
    public String getSummaryText() {
        String value = getPersistedString(null);
        if (!TextUtils.isEmpty(value)) {
            double amount = Double.parseDouble(value) / 100;
            return PERCENTFORMAT.format(amount);
        }
        return null;
    }

    /**
     * Initialisiert die EditTextPreferencePercent. Wenn die Methode ueberschrieben wird, ist
     * super.init() aufzurufen.
     */
    protected void init() {
        PERCENTFORMAT.setMaximumFractionDigits(getMaximumFractionDigits());
        PERCENTFORMAT.setMinimumFractionDigits(getMinimumFractionDigits());
    }
}

