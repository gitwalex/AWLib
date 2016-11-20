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
/**
 *
 */
package de.aw.awlib.fragments;

import android.content.SharedPreferences;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;

import de.aw.awlib.preferences.EditTextPreferenceTime;
import de.aw.awlib.preferences.MainPreferenceInterface;

/**
 * Erstellt und bearbeitet die allgemeinen Preferences. Die einzelnen Preferences werden als Wert in
 * die Summary eingestellt.
 *
 * @author alex
 */
public abstract class AWLibPreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * Initialisiert die Summaries, wenn das Fragment gestartet wird.
     *
     * @param p
     *         Preference.
     */
    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    /**
     * De-Registriert sich als {@link SharedPreferences.OnSharedPreferenceChangeListener}
     */
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Registriert sich als {@link SharedPreferences.OnSharedPreferenceChangeListener}
     */
    @Override
    public void onResume() {
        super.onResume();
        initSummary(getPreferenceScreen());
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Beim Aufruf wird die Summary-Zeile der Preference aktualisiert. Siehe {@link
     * AWLibPreferenceFragment#updatePrefSummary(Preference)}
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
    }

    /**
     * Stellt den Wert der Preference in die Summary-Zeile ein. Die gilt fuer Preferences, die das
     * Interface {@link MainPreferenceInterface} implementieren, ListPreferences und
     * EditTextPreferences. Alle anderen Preferences werden nicht barbeitet. Enthaelt der Titel der
     * EditTextPreference als Teilstring 'asswor'  werden statt des Textes Sterne in die
     * Summaryzeile gesetzt. Liefert MainPreferenceInterface 'null' oder einen leeren String zuruck,
     * wird nichts in die Summary eingestelle.
     *
     * @param p
     *         Preference
     */
    protected void updatePrefSummary(Preference p) {
        if (p instanceof MainPreferenceInterface) {
            String summary = ((MainPreferenceInterface) p).getSummaryText();
            if (!TextUtils.isEmpty(summary)) {
                p.setSummary(summary);
            }
            return;
        }
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
            return;
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (p.getTitle().toString().contains("asswor")) {
                p.setSummary("******");
            } else {
                p.setSummary(editTextPref.getText());
            }
            return;
        }
        if (p instanceof EditTextPreferenceTime) {
            EditTextPreferenceTime editTextPref = (EditTextPreferenceTime) p;
            p.setSummary(editTextPref.getSummary());
            return;
        }
    }
}
