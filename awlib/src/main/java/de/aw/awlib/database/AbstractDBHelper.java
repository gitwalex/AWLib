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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.net.Uri;
import android.support.annotation.CallSuper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.application.AWLIbApplication;
import de.aw.awlib.application.ApplicationConfig;
import de.aw.awlib.gv.AWLibApplicationGeschaeftsObjekt;

/**
 * Helper fuer die SQLite-Database
 */
public abstract class AbstractDBHelper extends SQLiteOpenHelper implements AWLibInterface {
    /**
     * CursorFactory. Loggt die Query und die Dauer der Abfrage in nanosekunden, wenn im Debug-Modus
     * und nicht Import.
     */
    private static final SQLiteDatabase.CursorFactory mCursorFactory =
            new SQLiteDatabase.CursorFactory() {
                @Override
                public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
                                        String editTable, SQLiteQuery query) {
                    AWLibSQLiteCursor c = new AWLibSQLiteCursor(masterQuery, editTable, query);
                    long timer = System.nanoTime();
                    if (!AWLibApplicationGeschaeftsObjekt
                            .isImport() && AWLIbApplication.EnableCursorLogging) {
                        long elapsed = c.getFinishTime() - timer;
                        boolean longRunning = elapsed > 50000;
                        if (longRunning) {
                            // Langlaufende Query - als Error loggen
                            AWLIbApplication.LogError("Dauer der Query " + query.toString() + ": " +
                                    TimeUnit.NANOSECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
                        } else {
                            AWLIbApplication.Log("Dauer der Query" + query.toString() + ": " +
                                    TimeUnit.NANOSECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
                        }
                    }
                    return c;
                }
            };
    private static ApplicationConfig mApplicationConfig;
    /**
     * DBHelperTemplate ist ein Singleton.
     */
    protected final Context context;
    private final ContentResolver resolver;
    private SQLiteDatabase db;
    private Set<Uri> usedTables = new HashSet<>();

    protected AbstractDBHelper(ApplicationConfig config,
                               SQLiteDatabase.CursorFactory cursorFactory) {
        super(AWLIbApplication.getContext(), config.getApplicationDatabaseFilename(),
                (cursorFactory == null) ? mCursorFactory : cursorFactory,
                config.theDatenbankVersion());
        mApplicationConfig = config;
        this.context = AWLIbApplication.getContext();
        resolver = context.getContentResolver();
    }

    public static void doVacuum() {
        SQLiteDatabase db = getDatabase();
        db.execSQL("vacuum");
    }

    /**
     * Liefert die Liste der Spalten einer Tabelle zuruck.
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     *
     * @return Liste der Columns.
     */
    public static List<String> getColumnsForTable(AWLibAbstractDBDefinition tbd) {
        List<String> columns = new ArrayList<>();
        Cursor c = getDatabase().rawQuery("PRAGMA table_info (" + tbd.name() + ")", null);
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
     * Convinience-Methode fuer getWriteableDatabase()
     *
     * @return WriteableDatabase
     */
    public static SQLiteDatabase getDatabase() {
        return mApplicationConfig.getDBHelper().getWritableDatabase();
    }

    /**
     * Ermittelt die Anzahl der Zeilen, die durch die Selection potentiell zurueckgeliefert werden.
     *
     * @param tbd
     *         AWLibAbstractDBDefinition der Tabelle
     * @param selection
     *         Selection
     * @param selectionArgs
     *         SelectionArgs
     *
     * @return Anzahl der  Zeilen.
     */
    public static long getNumberOfRows(AWLibAbstractDBDefinition tbd, String selection,
                                       String[] selectionArgs) {
        String[] projection = new String[]{"COUNT(*)"};
        Cursor c = getDatabase()
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
     * Liefert eine Liste der Tabellennamen zurueck, in der die Spalte vorkommt
     *
     * @param columnName
     *         Spaltenname
     *
     * @return Liste der Tabellennamen. Kann leer sein.
     */
    public static List<String> getTableNamesForColumn(String columnName) {
        List<String> tables = new ArrayList<>();
        String[] projection = new String[]{"name"};
        String selection = " sql LIKE '%" + columnName + "%' AND " +
                " type = 'table'";
        Cursor c =
                getDatabase().query("sqlite_master", projection, selection, null, null, null, null);
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
     * siehe {@link SQLiteDatabase#beginTransaction()}
     */
    public void beginTransaction() {
        db = getWritableDatabase();
        db.beginTransaction();
        usedTables.clear();
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Set)} gerufen.
     */
    public int delete(AWLibAbstractDBDefinition tbd, String selection, String[] selectionArgs) {
        return delete(tbd.getUri(), selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Set)} gerufen.
     */
    public int delete(Uri uri, String selection, String[] selectionArgs) {
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
     * siehe {@link SQLiteDatabase#endTransaction()}
     * <p>
     * Transaktionen koennen geschachtelt werden. Erst wenn keine Transaktion mehr ansteht, wird
     * {@link AbstractDBHelper#notifyCursors(Set< AWLibAbstractDBDefinition >)} gerufen.
     */
    public void endTransaction() {
        db.endTransaction();
        if (!db.inTransaction()) {
            notifyCursors(usedTables);
            db = null;
        }
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
    public boolean inTransaction() {
        return db.inTransaction();
    }

    /**
     * siehe {@link SQLiteDatabase#insert(String, String, ContentValues)}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Set)} gerufen.
     */
    public long insert(AWLibAbstractDBDefinition tbd, String nullColumnHack,
                       ContentValues content) {
        return insert(tbd.getUri(), nullColumnHack, content);
    }

    /**
     * siehe {@link SQLiteDatabase#insert(String, String, ContentValues)}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Set)} gerufen.
     */
    public long insert(Uri uri, String nullColumnHack, ContentValues content) {
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
    public long insertWithOnConflict(Uri uri, String nullColumnHack, ContentValues values,
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
    public long insertWithOnConflict(AWLibAbstractDBDefinition tbd, String nullColumnHack,
                                     ContentValues values, int conflictAlgorithm) {
        return insertWithOnConflict(tbd.getUri(), nullColumnHack, values, conflictAlgorithm);
    }

    /**
     * Wird immer am Ende einer (kompletten) Transaktion gerufen, d.h, wenn eine Transaktion
     * geschachtelt ist, wird erst nach Ende der ersten begonnen Transaktion diese Methode gerufen.
     * <p>
     * Dies funktioniert z.B. mit folgendem Code:
     * <pre>
     * <code>
     *
     * super.notifyCursors(usedTables);
     * ContentResolver resolver = context.getContentResolver();
     * for (Uri uri : usedTables) {
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
     * @param tables
     *         Tabellen, die waehrend der gesamten Transaktion benutzt wurden. Alle Cursor zu diesen
     *         Tabellen werden ueber eine Aenderung informiert.
     */
    @CallSuper
    protected void notifyCursors(Set<Uri> tables) {
        for (Uri uri : usedTables) {
            notifyCursors(uri);
        }
    }

    /**
     * @param uri
     *         Tabelle, die benutzt wurde. Alle Cursor zu dieser Tabelle werden ueber eine Aenderung
     *         informiert.
     */
    protected void notifyCursors(Uri uri) {
        resolver.notifyChange(uri, null);
    }

    /**
     * siehe {@link SQLiteDatabase#setTransactionSuccessful()}
     */
    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    /**
     * siehe {@link SQLiteDatabase#update(String, ContentValues, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Set)} gerufen.
     */
    public int update(AWLibAbstractDBDefinition tbd, ContentValues content, String selection,
                      String[] selectionArgs) {
        return update(tbd.getUri(), content, selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#update(String, ContentValues, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBHelper#notifyCursors(Set)} gerufen.
     */
    public int update(Uri uri, ContentValues content, String selection, String[] selectionArgs) {
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

