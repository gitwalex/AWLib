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
package de.aw.awlib;

/**
 * Verschiedene Codes fuer Ergebnisse von Aktivitaeten
 */
public interface Codes {
    /**
     * Code fuer Request des Namens eines Partners/Wertpaiers/Categorie o.ae.
     */
    int REQUEST_AUSWAHLNAME = 1;//
    /**
     * Request_Codes fuer Fragments und Activities
     */
    /**
     * Alles OK
     */
    int RESULT_OK = -1,//
    /**
     * Keine Bestaende mit YahooKuerzel
     */
    RESULT_NOYahooKuerzel = -2,//
    /**
     * TimeOut
     */
    RESULT_TimeOut = -3,//
    /**
     * Fehler bei Dateibearbeitung
     */
    RESULT_FILE_ERROR = -4,//
    /**
     * Keine Buchungen beim Abgelich gefunden
     */
    RESULT_KEINE_BUCHUNGEN = 0,//
    /**
     * Wenn keine automatischer Abgleich OnlineBuchungen gewuenscht ist.
     */
    RESULT_KEIN_ABGLEICHGEWUENSCHT = -5,//
    /**
     * File not found
     */
    RESULT_FILE_NOTFOUND = -6,//
    /**
     * Sonstiger Fehler
     */
    RESULT_Divers = -99,//
    /**
     * Import: Zieltabelle nicht gefunden
     */
    RESULT_FEHLER_TBD = -7,//
    /**
     * Import: Spaltenname in tbd nicht vorhanden
     */
    RESULT_SPALTE_IN_TBD_NICHT_VORHANDEN = -8//
            ;
}
