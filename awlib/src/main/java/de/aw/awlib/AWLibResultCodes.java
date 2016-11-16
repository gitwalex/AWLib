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
 * Verschiedene ResultCodes fuer Ergebnisse von Aktivitaeten
 */
public interface AWLibResultCodes {
    /**
     * Alles OK
     */
    int RESULT_OK = -1,//
    /**
     * TimeOut
     */
    RESULT_TimeOut = -3,//
    /**
     * Fehler bei Dateibearbeitung
     */
    RESULT_FILE_ERROR = -4,//
    /**
     * File not found
     */
    RESULT_FILE_NOTFOUND = -6,//
    /**
     * Sonstiger Fehler
     */
    RESULT_Divers = -99//
            ;
}
