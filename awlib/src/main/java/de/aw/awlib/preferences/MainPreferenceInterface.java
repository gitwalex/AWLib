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

import de.aw.awlib.fragments.AWLibPreferenceFragment;

/**
 * Interface fuer Preferences, wenn die Summary-Zeile ueberschrieben werden soll. Wird in {@link
 * AWLibPreferenceFragment#updatePrefSummary(android.support.v7.preference.Preference)} gerufen.
 * Created by alex on 12.09.2015.
 */
public interface MainPreferenceInterface {
    /**
     * Liefert den Text zuruckk, der in die Summary-Zeile der Preference geschrieben werden soll.
     */
    String getSummaryText();
}
