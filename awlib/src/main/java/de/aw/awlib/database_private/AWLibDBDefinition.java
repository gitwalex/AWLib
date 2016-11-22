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
package de.aw.awlib.database_private;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.aw.awlib.R;
import de.aw.awlib.application.AWLIbApplication;
import de.aw.awlib.database.AWLibAbstractDBDefinition;
import de.aw.awlib.database.AWLibDBAlterHelper;

/**
 * @author Alexander Winkler
 *         <p/>
 *         Aufzaehlung der Tabellen der Datenbank. 1. Parameter ist ein Integer-Array der resIds
 *         (R.string.xxx)der Tabellenspalten
 */
public enum AWLibDBDefinition implements Parcelable, AWLibAbstractDBDefinition {
    RemoteServer() {
        @Override
        public int[] getTableItems() {
            return new int[]{R.string._id//
                    , R.string.column_serverurl//
                    , R.string.column_userID//
            };
        }
    };
    /**
     * Flag, ob initialize(context) aufgerufen wurde.
     */
    private static boolean isInitialized;
    private static AWLibDBFormate mDBFormat;

    /**
     * Initialisiert DBDefinition. Vor der ersten Nutzung aufzurufen
     *
     * @param con
     *         Context der App
     *
     * @throws IllegalArgumentException
     *         wenn 1. Festgestellt wird, dass unter einer resID zwei verschiedene Spaltennamen
     *         bestehen 2. Festgestellt wird, das unter einer resID zwei verschieden Formate
     *         bestehen 3. Festgestellt wird, dass unter einer resID zwei verschiedene QIFPraefixe
     *         bestehen
     */
    static {
        /*
         * Belegung der Maps fuer:
		 * 1. mapResID2columnNames
		 * 2. mapColumnName2ResID
		 */
        Context context = AWLIbApplication.getContext();
        for (AWLibDBDefinition tbd : AWLibDBDefinition.values()) {
            for (int map : tbd.tableitems) {
                String resIDString = context.getString(map);
                // Versorgen der Maps
                tbd.mapResID2columnNames.put(map, resIDString);
            }
        }
        mDBFormat = AWLibDBFormate.getInstance();
        isInitialized = true;
    }

    /**
     * Tableitems der Tabelle, die tatsaechlich angelegt werden
     */
    public final int[] createResIDs;
    /**
     * Tableitems der Tabelle
     */
    public final int[] tableitems;
    /**
     * Liste der Namen der Columns. Wird erstmalig belegt bei Aufruf von
     * mapResID2columnNames(context).
     */
    private final Map<Integer, String> mapResID2columnNames = new LinkedHashMap<>();
    /**
     * Alle resIDs der Tabelle/View
     */
    private final int[] resIDs;

    AWLibDBDefinition() {
        this.tableitems = getTableItems();
        resIDs = new int[tableitems.length];
        int[] createTableItems = getCreateTableItems();
        createResIDs = new int[createTableItems.length];
        int i = 0;
        for (int map : tableitems) {
            resIDs[i++] = map;
        }
        i = 0;
        for (int map : createTableItems) {
            createResIDs[i++] = map;
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
     * Liefert zu einem int-Array die entsprechenden ColumnNamen getrennt durch Kommata zurueck
     *
     * @param columns
     *         StringArray, Basis
     *
     * @return ColumnNamen, Komma getrennt
     */
    public static String getCommaSeperatedList(@NonNull AWLibDBDefinition tbd,
                                               @NonNull String[] columns) {
        StringBuilder indexSQL = new StringBuilder(columns[0]);
        for (int j = 1; j < columns.length; j++) {
            indexSQL.append(", ").append(columns[j]);
        }
        return indexSQL.toString();
    }

    /**
     * Erweiterung der value()-Methode der ENUM. Wirft {@link IllegalArgumentException}.
     *
     * @param ordinal
     *         Nummer der AWLibDBDefinition
     *
     * @return AWLibDBDefinition zu ordinal.
     *
     * @throws IllegalArgumentException
     *         wenn zu ordinal keine AWLibDBDefinition gefunden wurde
     */
    public static AWLibDBDefinition getMatch(int ordinal) {
        try {
            return values()[ordinal];
        } catch (Exception e) {
            throw new IllegalArgumentException("Keine entsprechende DataBaseDefinition gefunden");
        }
    }

    /**
     * Liefert zu einer resID ein MAX(resID) zurueck.
     *
     * @param resID
     *         resID des Items
     *
     * @return Select Max im Format MAX(itemname) AS itemname
     */
    public String SQLMaxItem(int resID) {
        return SQLMaxItem(resID, false);
    }

    /**
     * Liefert zu einer resID ein MAX(resID) zurueck.
     *
     * @param resID
     *         resID des Items
     * @param fullQualified
     *         ob der Name vollquelifiziert sein soll
     *
     * @return Select Max im Format MAX(Tablename.itemname) AS itemname
     */
    private String SQLMaxItem(int resID, boolean fullQualified) {
        String spalte = columnName(resID);
        if (fullQualified) {
            return "max(" + name() + "." + spalte + ") AS " + spalte;
        }
        return "max(" + spalte + ") AS " + spalte;
    }

    /**
     * Erstellt SubSelect.
     *
     * @param tbd
     *         AWLibDBDefinition
     * @param resID
     *         resID der Spalte
     * @param column
     *         Sapalte, die ermittelt wird.
     * @param selection
     *         Kann null sein
     * @param selectionArgs
     *         kann null sein. Es wird keinerlei Pruefung vorgenommen.
     *
     * @return SubSelect
     */
    public String SQLSubSelect(AWLibDBDefinition tbd, int resID, String column, String selection,
                               String[] selectionArgs) {
        String spalte = tbd.columnName(resID);
        String sql = " (SELECT " + column + " FROM " + tbd.name() + " b ? ) AS " + spalte;
        if (selectionArgs != null) {
            for (String args : selectionArgs) {
                selection = selection.replaceFirst("\\?", args);
            }
        }
        if (!TextUtils.isEmpty(selection)) {
            sql = sql.replace("?", " WHERE " + selection);
        } else {
            sql = sql.replace("?", "");
        }
        return sql;
    }

    /**
     * Liefert zu einer resID ein SUM(resID) zurueck.
     *
     * @param resID
     *         resID des Items
     *
     * @return Select Max im Format SUM(itemname) AS itemname
     */
    public String SQLSumItem(int resID) {
        return SQLSumItem(resID, false);
    }

    /**
     * Liefert zu einer resID ein SUM(resID) zurueck.
     *
     * @param resID
     *         resID des Items
     * @param fullQualified
     *         ob der Name vollquelifiziert sein soll
     *
     * @return Select Max im Format SUM(Tablename.itemname) AS itemname
     */
    public String SQLSumItem(int resID, boolean fullQualified) {
        String spalte = columnName(resID);
        if (fullQualified) {
            return "sum(" + name() + "." + spalte + ") AS " + spalte;
        }
        return "sum(" + spalte + ") AS " + spalte;
    }

    /**
     * @param fromResIDs
     *         Spalten
     * @param idColumn
     *         Name der Column, die als _id verwendet werden soll
     *
     * @return Liefert ein StringArray mit Spaltennamen. idColumn wird 'as _id' angehaengt
     */
    public String[] columNames(int[] fromResIDs, int idColumn) {
        String[] projection = new String[fromResIDs.length + 1];
        int i = 0;
        for (int resID : fromResIDs) {
            projection[i] = columnName(resID);
            i++;
        }
        projection[i] = columnName(idColumn) + " as _id";
        return projection;
    }

    /**
     * Liefert die Liste der uebergebene Colums als Map zuruckt (z.B. fuer SQLiteQueryBuilder
     *
     * @param columns
     *         Liste der Columns
     *
     * @return Map der Columns
     */
    public Map<String, String> columnMap(String[] columns) {
        Map<String, String> columMap = new HashMap<>();
        for (String s : columns) {
            columMap.put(s, s);
        }
        return columMap;
    }

    /**
     * Name einer Columns als String
     *
     * @param resID
     *         ResId, zu der der Columnname gewuenscht werden.
     *
     * @return Name der Columns
     *
     * @throws ResIDNotFoundException
     *         wenn ResId nicht in der Liste der Columns enthalten ist.
     */
    public String columnName(int resID) {
        return columnName(resID, true);
    }

    /**
     * Name einer Columns als String
     *
     * @param resID
     *         ResId, zu der der Columnname gewuenscht werden.
     * @param check
     *         Flag, ob bei einem Fehler eine ResIDNotFoundException geworfen werden soll.
     *
     * @return Name der Columns
     *
     * @throws ResIDNotFoundException
     *         wenn ResId nicht in der Liste der Columns enthalten ist.
     */
    public String columnName(int resID, boolean check) {
        if (!isInitialized) {
            throw new IllegalArgumentException(
                    "AWLibDBDefinition nicht initialisiert. Zuerst Aufruf " + "initalize(context)");
        }
        if (mapResID2columnNames.get(resID) == null & check) {
            throw new ResIDNotFoundException("ColumnName fuer " + resID + " " +
                    "(" + AWLIbApplication.getContext().getString(resID) +
                    ") in " + name() + " ist null!");
        }
        return mapResID2columnNames.get(resID);
    }

    /**
     * @return Liefert alle Spaltennamen zu den ResIDs zurueck. Es wird keine id angehaengt.
     */
    public String[] columnNames() {
        String[] columns = new String[resIDs.length];
        for (int i = 0; i < resIDs.length; i++) {
            columns[i] = columnName(resIDs[i]);
        }
        return columns;
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
    public String[] columnNames(int[] resIDs, String... args) {
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
     * Liste der Columns als StringArray
     *
     * @param resIDs
     *         Liste der ResId, zu denen die Columnnames gewuenscht werden.
     *
     * @return Liste der Columns. Anm Ende wird noch die Spalte '_id' hinzugefuegt.
     *
     * @throws ResIDNotFoundException
     *         wenn ResId nicht in der Liste der Columns enthalten ist.
     * @throws IllegalArgumentException
     *         wenn initialize(context) nicht gerufen wurde
     */
    public String[] columnNames(int... resIDs) {
        if (!isInitialized) {
            throw new IllegalArgumentException(
                    "AWLibDBDefinition nicht " + "initialisiert. Zuerst Aufruf initalize(context)");
        }
        if (resIDs != null) {
            boolean idPresent = false;
            List<String> columns = new ArrayList<>();
            for (int resID : resIDs) {
                String col = mapResID2columnNames.get(resID);
                if (resID == R.string._id) {
                    idPresent = true;
                }
                if (col == null) {
                    String value = AWLIbApplication.getContext().getResources().getString(resID);
                    throw new ResIDNotFoundException("ResID " + resID + " mit " +
                            "Namen " + value + " in " + name() + " nicht " +
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
     * @return Liefert alle Spaltennamen zu den ResIDs zurueck. Die _id Spalte wird nicht
     * mitgeliefert.
     */
    public String[] columnNamesWithoutID() {
        String[] columns = new String[resIDs.length - 1];
        int i = 0;
        for (int res : getResIDs()) {
            if (res != R.string._id) {
                columns[i] = columnName(res);
                i++;
            }
        }
        return columns;
    }

    /**
     * Wird beim Erstellen der DB Nach Anlage aller Tabellen und Indices gerufen. Hier koennen noch
     * Nacharbeiten durchgefuehrt werden
     *
     * @param helper
     *         AWLibDBAlterHelper database
     */
    public void createDatabase(AWLibDBAlterHelper helper) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Liefert zu einem int-Array die entsprechenden ColumnNamen getrennt durch Kommata zurueck
     *
     * @param tableindex
     *         Array, zu dem die Namen ermittelt werden sollen
     *
     * @return ColumnNamen, Komma getrennt
     */
    public String getCommaSeperatedList(@NonNull int[] tableindex) {
        StringBuilder indexSQL = new StringBuilder(columnName(tableindex[0]));
        for (int j = 1; j < tableindex.length; j++) {
            String column = columnName(tableindex[j]);
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    /**
     * @return Tableitems der Tabelle, Aufbau: [0]: resID [1]: Format in SQLite [2]: (otional)
     * Praefix fuer QIF-Import
     */
    public int[] getCreateTableItems() {
        return getTableItems();
    }

    public int[] getCreateTableResIDs() {
        return createResIDs;
    }

    /**
     * @return den String fuer den Aubau einer View (ohne CREATE View AS name). Muss bei Views
     * ueberscheiben werden. Standard: null
     */
    public String getCreateViewSQL() {
        return null;
    }

    /**
     * Format der Spalte anhand der ResID
     *
     * @param resID
     *         der Spalte
     *
     * @return Format
     *
     * @throws IllegalArgumentException
     *         wenn initialize(context) nicht gerufen wurde
     */
    public char getFormat(int resID) {
        if (!isInitialized) {
            throw new IllegalArgumentException(
                    "AWLibDBDefinition nicht initialisiert. Zuerst Aufruf initalize(context)");
        }
        return mDBFormat.getFormat(resID);
    }

    /**
     * @return Liefert die Spalten eines Index
     */
    public int[] getIndex() {
        return null;
    }

    /**
     * Liste der fuer eine sinnvolle Sortierung notwendigen Spalten.
     *
     * @return ResId der Spalten, die zu einer Sortierung herangezogen werden sollen.
     */
    public int[] getOrderByItems() {
        return new int[]{getCreateTableItems()[0]};
    }

    /**
     * Liefert ein Array der Columns zurueck, nach den sortiert werden sollte,
     *
     * @return Array der Columns, nach denen sortiert werden soll.
     */
    public String[] getOrderColumns() {
        int[] columItems = getOrderByItems();
        return columnNames(columItems);
    }

    /**
     * OrderBy-String - direkt fuer SQLITE verwendbar.
     *
     * @return OrderBy-String, wie in der Definition der ENUM vorgegeben
     */
    public String getOrderString() {
        String[] orderColumns = getOrderColumns();
        StringBuilder order = new StringBuilder(orderColumns[0]);
        for (int i = 1; i < orderColumns.length; i++) {
            order.append(", ").append(orderColumns[i]);
        }
        return order.toString();
    }

    /**
     * OrderBy-String - direkt fuer SQLITE verwendbar.
     *
     * @return OrderBy-String, wie in der Definition der ENUM vorgegeben
     */
    public String getOrderString(int... orderColumns) {
        return getCommaSeperatedList(orderColumns);
    }

    /**
     * @return Liefert alle resIDs zu einer Tabelle
     */
    public int[] getResIDs() {
        return resIDs;
    }

    /**
     * Liefert das Format der Column im Klartext fuer SQLite
     *
     * @param resId
     *         ResID der Colimn
     *
     * @return Format der Column fuer SQLite im Klartext
     */
    public String getSQLiteFormat(int resId) {
        if (!isInitialized) {
            throw new IllegalArgumentException(
                    "AWLibDBDefinition nicht initialisiert. Zuerst Aufruf " + "initalize(context)");
        }
        return mDBFormat.getSQLiteFormat(resId);
    }

    /**
     * @return TableItems der Tabelle. Koennen von den echten TableItems gemaess CreateTableItems ()
     * abweichen, z.B. wenn weitere Items dazugejoint werden.
     */
    public int[] getTableItems() {
        return getCreateTableItems();
    }

    /**
     * @return Items des UniqueIndex.  Default: null
     */
    public int[] getUniqueIndex() {
        return null;
    }

    @Override
    public Uri getUri() {
        return null;
    }

    /**
     * @return liefert den URI-Code zu der Tabelle zuruck. Dies ist der ordinal der Tabelle.
     */
    public int getUriCode() {
        return ordinal();
    }

    /**
     * Indicator, ob AWLibDBDefinition eine View ist. Default false
     *
     * @return false. Wenn DBDefintion eine View ist, muss dies zwingend ueberschreiben werden,
     * sonst wirds in DBHelper als Tabelle angelegt.
     */
    public boolean isView() {
        return false;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(ordinal());
    }

    /**
     * Wird geworfen, wenn eine ResID nicht gefunden wurde.
     */
    @SuppressWarnings("serial")
    public class ResIDNotFoundException extends RuntimeException {
        public ResIDNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }
}