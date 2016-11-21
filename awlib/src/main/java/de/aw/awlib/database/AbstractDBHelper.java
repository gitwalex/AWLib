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

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.application.AWLIbApplication;
import de.aw.awlib.application.ApplicationConfig;
import de.aw.awlib.gv.AWLibGeschaeftsObjekt;

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
                    if (!AWLibGeschaeftsObjekt.isImport() && AWLIbApplication.EnableCursorLogging) {
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
    /**
     * DBHelperTemplate ist ein Singleton.
     */
    private static AbstractDBHelper ch;

    protected AbstractDBHelper(ApplicationConfig config,
                               SQLiteDatabase.CursorFactory cursorFactory) {
        super(AWLIbApplication.getContext(), config.getApplicationDatabaseFilename(),
                (cursorFactory == null) ? mCursorFactory : cursorFactory,
                config.theDatenbankVersion());
        ch = this;
    }

    public static void doVacuum() {
        SQLiteDatabase db = getInstance().getWritableDatabase();
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
        return AbstractDBHelper.getInstance().getWritableDatabase();
    }

    public static AbstractDBHelper getInstance() {
        if (ch == null) {
            throw new IllegalStateException("DBHelperTemplate noch nicht initialisiert.");
        }
        return ch;
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
}

