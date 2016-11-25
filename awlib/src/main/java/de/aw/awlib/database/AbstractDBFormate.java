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
package de.aw.awlib.database;

import android.content.Context;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

import de.aw.awlib.R;
import de.aw.awlib.application.AWLIbApplication;

/**
 * Formate fuer die einzelnen Columns der DB
 */
public class AbstractDBFormate {
    /**
     * Map der ResIDs auf das Format der Spalte
     */
    private static final SparseArray<Character> mapResID2Formate = new SparseArray<>();
    private static AbstractDBFormate mInstance;
    /**
     * "TTEXT", "DDate", "NNUMERIC", "BBoolean", "CNUMERIC", "PNUMERIC", "KNUMERIC"};
     * <p>
     * Liste der moeglichen Formate.
     * <p>
     * T = normaler Text
     * <p>
     * N = Numerisch
     * <p>
     * C = Numerisch als Currency, Long, anzahl Stellen wie Nachkommastellen Locale.getCurrency
     * <p>
     * K = Numerisch als Currency, Long, aktuell Anzahl Stellen wie Nachkommastellen
     * Locale.getCurrency
     * <p>
     * D = Datum
     * <p>
     * B = Boolean
     * <p>
     * P = Numerisch als Prozent
     * <p>
     * K = Numerisch mit 5 NAchkommastellen (Kurs)
     */
    private final Map<Character, String> formate = new HashMap<>();
    private final Map<String, Integer> mapColumnNae2ResID = new HashMap<>();

    protected AbstractDBFormate() {
        Context context = AWLIbApplication.getContext();
        int resID = R.string._id;
        mapResID2Formate.put(resID, 'I');
        mapColumnNae2ResID.put(context.getString(resID), resID);
        String[] s = {"TTEXT", "DDate", "NNUMERIC", "MNUMERIC", "BBoolean", "CNUMERIC", "PNUMERIC",
                "KNUMERIC", "IINTEGER", "OBLOB"};
        for (String f : s) {
            formate.put(f.charAt(0), f.substring(1));
        }
        /*
         * Belegung der Maps fuer:
		 * 1. mapResID2columnNames
		 * 2. mapColumnName2ResID
		 */
        for (int[] map : getItems()) {
            resID = map[0];
            mapResID2Formate.put(resID, (char) map[1]);
            mapColumnNae2ResID.put(context.getString(resID), resID);
        }
    }

    public static AbstractDBFormate getInstance() {
        if (mInstance == null) {
            mInstance = new AbstractDBFormate();
        }
        return mInstance;
    }

    /**
     * @param resID
     *         resID
     *
     * @return Liefert das Format der column zurueck
     */
    public Character getFormat(Integer resID) {
        Character format = mapResID2Formate.get(resID);
        if (format == null) {
            format = 'T';
            AWLIbApplication.Log("Kein Format fuer " + resID + ". Default 'T'");
        }
        return format;
    }

    /**
     * Hier sollten alle comumnItems aufgefuerht werden, deren Format nicht Text ist. Dann wird im
     * Geschaeftobject der Wert mit dem entsprechenden in die Tabellenspalte geschrieben.
     *
     * @return Liste der columns. [0] = resID, [1] = format
     */
    public int[][] getItems() {
        return new int[][]{};
    }

    public Integer getResID(String resName) {
        return mapColumnNae2ResID.get(resName.trim());
    }

    public String getSQLiteFormat(Integer resId) {
        Character c = mapResID2Formate.get(resId);
        return formate.get(c);
    }
}
