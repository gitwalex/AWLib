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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database_private.AWDBDefinition;

import static de.aw.awlib.application.AWApplication.Log;
import static de.aw.awlib.application.AWApplication.LogError;

/**
 * Helper fuer die SQLite-Database
 */
@SuppressWarnings({"WeakerAccess", "TryFinallyCanBeTryWithResources", "unused"})
public abstract class AbstractDBHelper extends SQLiteOpenHelper implements AWInterface {
    protected static AbstractDBHelper dbHelper;
    /**
     * Map der ResIDs auf das Format der Spalte
     */
    private final SparseArray<Character> mapResID2Formate = new SparseArray<>();
    private final Map<Character, String> formate = new HashMap<>();
    private final Map<String, Integer> mapColumnName2ResID = new HashMap<>();
    private final Map<Integer, String> mapResID2ColumnName = new HashMap<>();
    private final WeakReference<Context> mContext;
    /**
     * DBHelperTemplate ist ein Singleton.
     */
    private SQLiteDatabase db;
    private Set<Uri> usedTables = new HashSet<>();

    protected AbstractDBHelper(Context context, SQLiteDatabase.CursorFactory cursorFactory) {
        super(context, ((AWApplication) context.getApplicationContext()).getApplicationConfig()
                        .getApplicationDatabaseAbsoluteFilename(), cursorFactory,
                ((AWApplication) context.getApplicationContext()).getApplicationConfig()
                        .theDatenbankVersion());
        dbHelper = this;
        mContext = new WeakReference<>(context.getApplicationContext());
        int resID = R.string._id;
        AWAbstractDBDefinition[] tbds = getAllDBDefinition();
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
        for (int[] map : getNonTextColumnItems()) {
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
    public final static String getCommaSeperatedList(@NonNull Context context,
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
    public final static String getCommaSeperatedList(@NonNull List<String> columns) {
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
    public final static String getCommaSeperatedList(@NonNull String[] columns) {
        StringBuilder indexSQL = new StringBuilder(columns[0]);
        for (int j = 1; j < columns.length; j++) {
            String column = columns[j];
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    public static AbstractDBHelper getInstance() {
        return dbHelper;
    }

    /**
     * Liefert zu einer resID ein MAX(resID) zurueck.
     *
     * @param resID
     *         resID des Items
     *
     * @return Select Max im Format MAX(itemname) AS itemname
     */
    public final String SQLMaxItem(int resID) {
        String spalte = columnName(resID);
        return "max(" + spalte + ") AS " + spalte;
    }

    /**
     * Erstellt SubSelect.
     *
     * @param tbd
     *         DBDefinition
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
    public final String SQLSubSelect(AWAbstractDBDefinition tbd, int resID, String column,
                                     String selection, String[] selectionArgs) {
        String spalte = columnName(resID);
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
    public final String SQLSumItem(int resID) {
        String spalte = columnName(resID);
        return "sum(" + spalte + ") AS " + spalte;
    }

    /**
     * siehe {@link SQLiteDatabase#beginTransaction()}
     */
    public final void beginTransaction() {
        db = getWritableDatabase();
        usedTables.clear();
        db.beginTransaction();
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
     * siehe {@link SQLiteDatabase#delete(String, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Uri)} gerufen.
     */
    public final int delete(AWAbstractDBDefinition tbd, String selection, String[] selectionArgs) {
        return delete(tbd.getUri(), selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Uri)} gerufen.
     */
    public final int delete(Uri uri, String selection, String[] selectionArgs) {
        if (db == null) {
            db = getWritableDatabase();
        }
        int rows = db.delete(uri.getLastPathSegment(), selection, selectionArgs);
        if (!db.inTransaction()) {
            notifyCursors(uri);
        } else {
            usedTables.add(uri);
        }
        return rows;
    }

    /**
     * Wird bei erstellen der Datanabank innerhalb einer Transaktion gerufen
     *
     * @param database
     *         SQLiteDatabase
     * @param dbhelper
     *         DBHelper
     */
    protected abstract void doCreate(SQLiteDatabase database, AWDBAlterHelper dbhelper);

    /**
     * Wird innerhalb einer Transaktion aus onUpgrade gerufen
     *
     * @param database
     *         SQLiteDatabase
     * @param dbhelper
     *         AWAlterDBHelper
     * @param oldVersion
     *         Version vor upgrade
     * @param newVersion
     *         neue Version
     */
    protected abstract void doUpgrade(SQLiteDatabase database, AWDBAlterHelper dbhelper,
                                      int oldVersion, int newVersion);

    /**
     * siehe {@link SQLiteDatabase#endTransaction()}
     * <p>
     * Transaktionen koennen geschachtelt werden. Erst wenn keine Transaktion mehr ansteht, wird mit
     * jeder in der gesamten Transaction genutzen Uri {@link AbstractDBHelper#notifyCursors(Uri)}
     * gerufen.
     */
    public final void endTransaction() {
        db.endTransaction();
        if (!db.inTransaction()) {
            for (Uri uri : usedTables) {
                notifyCursors(uri);
            }
            db = null;
        }
    }

    /**
     * Liefert alle AWAbstractDBDefinition  zurusck
     *
     * @return AWAbstractDBDefinition als Array
     */
    public abstract AWAbstractDBDefinition[] getAllDBDefinition();

    /**
     * Liefert die Liste der Spalten einer Tabelle zuruck.
     *
     * @param tbd
     *         AWAbstractDBDefinition
     *
     * @return Liste der Columns.
     */
    public final List<String> getColumnsForTable(AWAbstractDBDefinition tbd) {
        List<String> columns = new ArrayList<>();
        Cursor c = getWritableDatabase().rawQuery("PRAGMA table_info (" + tbd.name() + ")", null);
        try {
            if (c.moveToFirst()) {
                int indexName = c.getColumnIndexOrThrow("name");
                do {
                    columns.add(c.getString(indexName));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }
        return columns;
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

    public final Context getContext() {
        return mContext.get();
    }

    /**
     * Liefert eine AWAbstractDBDefinition zu einem Tablename zurusck
     *
     * @param tablename
     *         Name der Tabelle als String
     *
     * @return AWAbstractDBDefinition
     */
    public abstract AWAbstractDBDefinition getDBDefinition(String tablename);

    /**
     * Liefert Informationen zu den Columns einer Tabelle zurueck
     *
     * @param database
     *         database
     * @param tableName
     *         Name der Tabelle
     *
     * @return Curur ueber die Daten(columnName, Typ, boolean NotNull, Defaultwert, ist Primarykey)
     */
    public final Cursor getDatabaseTableInfo(SQLiteDatabase database, String tableName) {
        String sql = "PRAGMA table_info (" + tableName + ")";
        return database.rawQuery(sql, null, null);
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
     * Hier sollten alle columnItems aufgefuerht werden, deren Format nicht Text ist. Dann wird im
     * Geschaeftobject der Wert mit dem entsprechenden in die Tabellenspalte geschrieben.
     *
     * @return Liste der columns. [0] = resID, [1] = format
     * <p>
     * <p>
     * Liste der moeglichen Formate.
     * <p>
     * T = normaler Text (optional)
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
    @NonNull
    public abstract int[][] getNonTextColumnItems();

    /**
     * Ermittelt die Anzahl der Zeilen, die durch die Selection potentiell zurueckgeliefert werden.
     *
     * @param tbd
     *         AWAbstractDBDefinition der Tabelle
     * @param selection
     *         Selection
     * @param selectionArgs
     *         SelectionArgs
     *
     * @return Anzahl der  Zeilen.
     */
    public final long getNumberOfRows(AWAbstractDBDefinition tbd, String selection,
                                      String[] selectionArgs) {
        String[] projection = new String[]{"COUNT(*)"};
        Cursor c = getWritableDatabase()
                .query(tbd.name(), projection, selection, selectionArgs, null, null, null);
        long numberOfRows = NOROWS;
        try {
            if (c.moveToFirst()) {
                numberOfRows = c.getLong(0);
            }
        } finally {
            c.close();
        }
        return numberOfRows;
    }

    /**
     * Liste der fuer eine sinnvolle Sortierung notwendigen Spalten.
     *
     * @return ResId der Spalten, die zu einer Sortierung herangezogen werden sollen.
     */
    public final int[] getOrderByItems(AWAbstractDBDefinition tbd) {
        return new int[]{tbd.getTableItems()[0]};
    }

    /**
     * Liefert ein Array der Columns zurueck, nach den sortiert werden sollte,
     *
     * @return Array der Columns, nach denen sortiert werden soll.
     */
    public final String[] getOrderColumns(AWAbstractDBDefinition tbd) {
        int[] columItems = getOrderByItems(tbd);
        return columnNames(columItems);
    }

    /**
     * OrderBy-String - direkt fuer SQLITE verwendbar.
     *
     * @return OrderBy-String, wie in der Definition der ENUM vorgegeben
     */
    public final String getOrderString(@NonNull int... resIDs) {
        String[] orderColumns = columnNames(resIDs);
        return getOrderString(orderColumns);
    }

    public final String getOrderString(String[] orderColumns) {
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
    public final String getOrderString(AWAbstractDBDefinition tbd) {
        String[] orderColumns = getOrderColumns(tbd);
        return getOrderString(orderColumns);
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
        if (c == null) {
            c = 'T';
        }
        return formate.get(c);
    }

    /**
     * @param database
     *         Database
     *
     * @return Liefert eine Liste der Tabellennamen zurueck
     */
    protected List<String> getTableNames(SQLiteDatabase database) {
        List<String> tableNames = new ArrayList<>();
        String selection = "type = 'table'";
        String[] projection = new String[]{"name"};
        Cursor c = database.query("sqlite_master", projection, selection, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                do {
                    tableNames.add(c.getString(0));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }
        return tableNames;
    }

    /**
     * Liefert eine Liste der Tabellennamen zurueck, in der die Spalte vorkommt
     *
     * @param columnresID
     *         resID des Spaltennamens
     *
     * @return DBDefinition der Tabellennamen. Kann leer sein.
     */
    public final List<AWAbstractDBDefinition> getTableNamesForColumn(int columnresID) {
        String column = getContext().getString(columnresID);
        List<String> tables = getTableNamesForColumn(column);
        List<AWAbstractDBDefinition> tbdList = new ArrayList<>();
        for (String table : tables) {
            tbdList.add(getDBDefinition(table));
        }
        return tbdList;
    }

    /**
     * Liefert eine Liste der Tabellennamen zurueck, in der der Name der Spalte vorkommt
     *
     * @param columnName
     *         Spaltenname
     *
     * @return Liste der Tabellennamen. Kann leer sein.
     */
    public final List<String> getTableNamesForColumn(String columnName) {
        List<String> tables = new ArrayList<>();
        String[] projection = new String[]{"name"};
        String selection = " sql LIKE '%" + columnName + "%' AND " +
                " type = 'table'";
        Cursor c = getWritableDatabase()
                .query("sqlite_master", projection, selection, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                do {
                    tables.add(c.getString(0));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }
        return tables;
    }

    /**
     * @param database
     *         Database
     *
     * @return Liefert eine Liste der Viewnamen zurueck
     */
    protected List<String> getViewNames(SQLiteDatabase database) {
        List<String> viewNames = new ArrayList<>();
        String selection = "type = 'view'";
        String[] projection = new String[]{"name"};
        Cursor c = database.query("sqlite_master", projection, selection, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                do {
                    viewNames.add(c.getString(0));
                } while (c.moveToNext());
            }
        } finally {
            c.close();
        }
        return viewNames;
    }

    /**
     * siehe {@link SQLiteDatabase#inTransaction()}
     */
    public final boolean inTransaction() {
        return db != null && db.inTransaction();
    }

    /**
     * siehe {@link SQLiteDatabase#insert(String, String, ContentValues)}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Uri)} gerufen.
     */
    public final long insert(AWAbstractDBDefinition tbd, String nullColumnHack,
                             ContentValues content) {
        return insert(tbd.getUri(), nullColumnHack, content);
    }

    /**
     * siehe {@link SQLiteDatabase#insert(String, String, ContentValues)}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Uri)} gerufen.
     */
    public final long insert(Uri uri, String nullColumnHack, ContentValues content) {
        if (db == null) {
            db = getWritableDatabase();
        }
        long id = db.insert(uri.getLastPathSegment(), nullColumnHack, content);
        if (!db.inTransaction()) {
            notifyCursors(uri);
        } else {
            usedTables.add(uri);
        }
        return id;
    }

    /**
     * siehe {@link SQLiteDatabase#insertWithOnConflict(String, String, ContentValues, int)}
     */
    public final long insertWithOnConflict(Uri uri, String nullColumnHack, ContentValues values,
                                           int conflictAlgorithm) {
        if (db == null) {
            db = getWritableDatabase();
        }
        long id = db.insertWithOnConflict(uri.getLastPathSegment(), nullColumnHack, values,
                conflictAlgorithm);
        if (!db.inTransaction()) {
            notifyCursors(uri);
        } else {
            usedTables.add(uri);
        }
        return id;
    }

    /**
     * siehe {@link SQLiteDatabase#insertWithOnConflict(String, String, ContentValues, int)}
     */
    public final long insertWithOnConflict(AWAbstractDBDefinition tbd, String nullColumnHack,
                                           ContentValues values, int conflictAlgorithm) {
        return insertWithOnConflict(tbd.getUri(), nullColumnHack, values, conflictAlgorithm);
    }

    /**
     * Wird immer am Ende einer (kompletten) Transaktion gerufen, d.h, wenn eine Transaktion
     * geschachtelt ist, wird erst nach Ende der zuerst begonnen Transaktion diese Methode gerufen.
     * <p>
     * Dies funktioniert z.B. mit folgendem Code:
     * <pre>
     * <code>
     *
     * super.notifyCursors(usedTables);
     * ContentResolver resolver = context.getContentResolver();
     * DBDefinition tbd = DBDefinition.valueOf(uri.getLastPathSegment());
     * switch (tbd) {
     * case BankRegelm:
     *      resolver.notifyChange(tbd.getUri(), null);
     *      break;
     * ...
     *
     * </code>
     * </pre>
     *
     * @param uri
     *         uri der Tabelle, die waehrend der gesamten Transaktion benutzt wurde. Alle Cursor zu
     *         diesen Tabellen werden ueber eine Aenderung informiert. Wenn keine weiteren von
     *         dieser Tabelle abhaengigen Uris informiert werden solle, wars das dann.
     *
     * @return true, wenn es sich bei der betroffenen Tabelle um eine zentrale tabelle handelt. Dann
     * kann der erbende DBHelper nichts von dieser Tabelle wissen. Sonst false.
     */
    @CallSuper
    protected boolean notifyCursors(Uri uri) {
        mContext.get().getContentResolver().notifyChange(uri, null);
        return uri.getLastPathSegment().equals(AWDBDefinition.RemoteServer.name());
    }

    @Override
    public final void onCreate(SQLiteDatabase database) {
        AWDBAlterHelper dbhelper = new AWDBAlterHelper(this, database);
        database.beginTransaction();
        try {
            for (AWAbstractDBDefinition tbd : getAllDBDefinition()) {
                if (!tbd.isView()) {
                    dbhelper.createTable(tbd);
                }
            }
            for (AWAbstractDBDefinition tbd : getAllDBDefinition()) {
                if (tbd.isView()) {
                    dbhelper.alterView(tbd);
                }
            }
            for (AWDBDefinition tbd : AWDBDefinition.values()) {
                if (!tbd.isView()) {
                    dbhelper.createTable(tbd);
                }
            }
            for (AWDBDefinition tbd : AWDBDefinition.values()) {
                if (tbd.isView()) {
                    dbhelper.alterView(tbd);
                }
            }
            doCreate(database, dbhelper);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Wenn sich die Tabelleninformationen geaendert habe, wird hier ein Upgrade ausgefuehrt.
     * Steuerung ueber DataBase-Versionsnummer. Es werden bei jedem Upgrade alle Views geloescht und
     * neu angelegt
     */
    @Override
    public final void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        AWDBAlterHelper dbhelper = new AWDBAlterHelper(this, database);
        database.beginTransaction();
        try {
            AWDBDefinition tbd = AWDBDefinition.RemoteServer;
            dbhelper.alterTableOnlyDeletedColumns(tbd);
            doUpgrade(database, dbhelper, oldVersion, newVersion);
            // Bei jeder Aenderung der DB werden alle Views geloescht und neu angelegt.
            List<String> views = getViewNames(database);
            for (String view : views) {
                database.execSQL("DROP VIEW " + view);
            }
            for (AWDBDefinition definition : AWDBDefinition.values()) {
                if (definition.isView()) {
                    dbhelper.alterView(definition);
                }
            }
            for (AWAbstractDBDefinition definition : getAllDBDefinition()) {
                if (definition.isView()) {
                    dbhelper.alterView(definition);
                }
            }
            database.setTransactionSuccessful();
            Log("DatenbankUpgrade von Version " + oldVersion + " nach " + newVersion + " erfolgreich!");
        } catch (Exception e) {
            LogError(getContext(),
                    "DatenbankUpgrade von Version " + oldVersion + " nach " + newVersion + " fehlgeschlagen!");
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Komprimiert die Datenbank und fuehrt 'runstats' aus.
     */
    public final void optimize() {
        db = getWritableDatabase();
        db.execSQL("Analyze");
        db.execSQL("vacuum");
    }

    /**
     * siehe {@link SQLiteDatabase#setTransactionSuccessful()}
     */
    public final void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    /**
     * siehe {@link SQLiteDatabase#update(String, ContentValues, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Uri)} gerufen.
     */
    public final int update(AWAbstractDBDefinition tbd, ContentValues content, String selection,
                            String[] selectionArgs) {
        return update(tbd.getUri(), content, selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#update(String, ContentValues, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Uri)} gerufen.
     */
    public final int update(Uri uri, ContentValues content, String selection,
                            String[] selectionArgs) {
        if (db == null) {
            db = getWritableDatabase();
        }
        String table = uri.getLastPathSegment();
        int rows = db.update(table, content, selection, selectionArgs);
        if (!db.inTransaction()) {
            notifyCursors(uri);
        } else {
            usedTables.add(uri);
        }
        return rows;
    }
}

