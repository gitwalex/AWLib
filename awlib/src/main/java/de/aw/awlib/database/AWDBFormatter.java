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
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.aw.awlib.R;
import de.aw.awlib.database_private.AWDBDefinition;

/**
 * Formate fuer die einzelnen Columns der DB
 */
public class AWDBFormatter {
    /**
     * Map der ResIDs auf das Format der Spalte
     */
    private static final SparseArray<Character> mapResID2Formate = new SparseArray<>();
    private final Map<Character, String> formate = new HashMap<>();
    private final Map<String, Integer> mapColumnName2ResID = new HashMap<>();
    private final Map<Integer, String> mapResID2ColumnName = new HashMap<>();

    public AWDBFormatter(Context context, AWAbstractDBDefinition[] tbds) {
        int resID = R.string._id;
        mapResID2Formate.put(resID, 'I');
        mapColumnName2ResID.put(context.getString(resID), resID);
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
        }
        for (AWAbstractDBDefinition tbd : AWDBDefinition.values()) {
            int[] columns = tbd.getTableItems();
            for (int mResID : columns) {
                String value = context.getString(mResID);
                mapResID2ColumnName.put(mResID, value);
                mapColumnName2ResID.put(value, mResID);
            }
        }
        for (AWAbstractDBDefinition tbd : tbds) {
            int[] columns = tbd.getTableItems();
            for (int mResID : columns) {
                String value = context.getString(mResID);
                mapResID2ColumnName.put(mResID, value);
                mapColumnName2ResID.put(value, mResID);
            }
        }
    }

    /**
     * Liefert zu einem int-Array die entsprechenden ColumnNamen getrennt durch Kommata zurueck
     *
     * @param columnResIds
     *         Array, zu dem die Namen ermittelt werden sollen
     *
     * @return ColumnNamen, Komma getrennt
     */
    public static String getCommaSeperatedList(@NonNull Context context,
                                               @NonNull int[] columnResIds) {
        StringBuilder indexSQL = new StringBuilder(context.getString(columnResIds[0]));
        for (int j = 1; j < columnResIds.length; j++) {
            String column = context.getString(columnResIds[j]);
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    /**
     * Liefert zu einer Liste die entsprechenden ColumnNamen getrennt durch Kommata zurueck
     *
     * @param columns
     *         Liste der Columns
     *
     * @return ColumnNamen, Komma getrennt
     */
    public static String getCommaSeperatedList(@NonNull List<String> columns) {
        StringBuilder indexSQL = new StringBuilder(columns.get(0));
        for (int j = 1; j < columns.size(); j++) {
            String column = columns.get(j);
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    /**
     * Liefert zu einer Liste die entsprechenden ColumnNamen getrennt durch Kommata zurueck
     *
     * @param columns
     *         Liste der Columns
     *
     * @return ColumnNamen, Komma getrennt
     */
    public static String getCommaSeperatedList(@NonNull String[] columns) {
        StringBuilder indexSQL = new StringBuilder(columns[0]);
        for (int j = 1; j < columns.length; j++) {
            String column = columns[j];
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    /**
     * @param resID
     *         resID
     *
     * @return Liefert den Spaltennamen zu einer resID zurueck
     */
    public final String columnName(int resID) {
        return mapResID2ColumnName.get(resID);
    }

    /**
     * Erstellt eine projection ahnhand von ResIDs und weiteren Spaltennamen
     *
     * @param resIDs
     *         ResIDs, die in der prjection gewuenscht sind
     * @param args
     *         Spaltenbezeichungen als String[]
     *
     * @return projection
     */
    public final String[] columnNames(int[] resIDs, String... args) {
        // Estmal alle columns der resIDs uebernehmen
        ArrayList<String> names = new ArrayList<>(Arrays.asList(columnNames(resIDs)));
        // Am ende steht jetzt schon "_id" - entfernen
        names.remove(names.size() - 1);
        // Jetzt alle String uebernehmen
        names.addAll(Arrays.asList(args));
        // Und anschliessend "_id" hinten anhaengen
        names.add(columnName(R.string._id));
        return names.toArray(new String[names.size()]);
    }

    /**
     * @return Liefert alle Spaltennamen zu den ResIDs zurueck. Es wird keine id angehaengt.
     */
    public final String[] columnNames(AWAbstractDBDefinition tbd) {
        int[] resIDs = tbd.getTableItems();
        String[] columns = new String[resIDs.length];
        for (int i = 0; i < resIDs.length; i++) {
            columns[i] = columnName(resIDs[i]);
        }
        return columns;
    }

    /**
     * Liste der Columns als StringArray
     *
     * @param resIDs
     *         Liste der ResId, zu denen die Columnnames gewuenscht werden.
     *
     * @return Liste der Columns. Anm Ende wird noch die Spalte '_id' hinzugefuegt.
     *
     * @throws AWAbstractDBDefinition.ResIDNotFoundException
     *         wenn ResId nicht in der Liste der Columns enthalten ist.
     * @throws IllegalArgumentException
     *         wenn initialize(context) nicht gerufen wurde
     */
    public final String[] columnNames(int... resIDs) {
        if (resIDs != null) {
            boolean idPresent = false;
            List<String> columns = new ArrayList<>();
            for (int resID : resIDs) {
                String col = columnName(resID);
                if (resID == R.string._id) {
                    idPresent = true;
                }
                if (col == null) {
                    throw new AWAbstractDBDefinition.ResIDNotFoundException(
                            "ResID " + resID + " nicht " +
                                    "vorhanden!.");
                }
                columns.add(col);
            }
            if (!idPresent) {
                columns.add(columnName(R.string._id));
            }
            return columns.toArray(new String[columns.size()]);
        }
        return null;
    }

    /**
     * Liefert zu einem int-Array die entsprechenden ColumnNamen getrennt durch Kommata zurueck
     *
     * @param tableindex
     *         Array, zu dem die Namen ermittelt werden sollen
     *
     * @return ColumnNamen, Komma getrennt
     */
    public final String getCommaSeperatedList(@NonNull int[] tableindex) {
        StringBuilder indexSQL = new StringBuilder(columnName(tableindex[0]));
        for (int j = 1; j < tableindex.length; j++) {
            String column = columnName(tableindex[j]);
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    /**
     * @param resID
     *         resID
     *
     * @return Liefert das Format der column zurueck
     */
    public final Character getFormat(Integer resID) {
        Character format = mapResID2Formate.get(resID);
        if (format == null) {
            format = 'T';
        }
        return format;
    }

    /**
     * Hier sollten alle comumnItems aufgefuerht werden, deren Format nicht Text ist. Dann wird im
     * Geschaeftobject der Wert mit dem entsprechenden in die Tabellenspalte geschrieben.
     *
     * @return Liste der columns. [0] = resID, [1] = format
     * <p>
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
     * K = Numerisch mit 5 Nachkommastellen (Kurs)
     **/
    public int[][] getItems() {
        return new int[][]{};
    }

    public final Integer getResID(String resName) {
        return mapColumnName2ResID.get(resName.trim());
    }

    /**
     * Liefert das Format der Column im Klartext fuer SQLite
     *
     * @param resId
     *         ResID der Colimn
     *
     * @return Format der Column fuer SQLite im Klartext
     */
    public final String getSQLiteFormat(Integer resId) {
        Character c = mapResID2Formate.get(resId);
        return formate.get(c);
    }
}
