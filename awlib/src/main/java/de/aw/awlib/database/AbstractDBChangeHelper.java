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
import android.database.sqlite.SQLiteDatabase;

import java.util.HashSet;
import java.util.Set;

import de.aw.awlib.application.AWLIbApplication;

/**
 * Helper fuer Database-CUD. Fuer Read ist der ContentResolver zu nutzen
 */
public abstract class AbstractDBChangeHelper {
    protected final Context context;
    private final SQLiteDatabase db;
    private Set<AWLibAbstractDBDefinition> usedTables = new HashSet<>();

    public AbstractDBChangeHelper() {
        this.context = AWLIbApplication.getContext();
        db = AbstractDBHelper.getDatabase();
    }

    /**
     * siehe {@link SQLiteDatabase#beginTransaction()}
     */
    public void beginTransaction() {
        db.beginTransaction();
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public int delete(AWLibAbstractDBDefinition tbd, String selection, String[] selectionArgs) {
        usedTables.add(tbd);
        int rows = db.delete(tbd.name(), selection, selectionArgs);
        if (!db.inTransaction()) {
            notifyCursors(usedTables);
        }
        return rows;
    }

    /**
     * siehe {@link SQLiteDatabase#endTransaction()}
     * <p>
     * Transaktionen koennen geschachtelt werden. Erst wenn keine Transaktion mehr ansteht, wird
     * {@link AbstractDBChangeHelper#notifyCursors(Set< AWLibAbstractDBDefinition >)} gerufen.
     */
    public void endTransaction() {
        db.endTransaction();
        if (!db.inTransaction()) {
            notifyCursors(usedTables);
        }
    }

    public SQLiteDatabase getDatabase() {
        return db;
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
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public long insert(AWLibAbstractDBDefinition tbd, String nullColumnHack,
                       ContentValues content) {
        usedTables.add(tbd);
        long id = db.insert(tbd.name(), nullColumnHack, content);
        if (!db.inTransaction()) {
            notifyCursors(usedTables);
        }
        return id;
    }

    /**
     * siehe {@link SQLiteDatabase#insertWithOnConflict(String, String, ContentValues, int)}
     */
    public long insertWithOnConflict(AWLibAbstractDBDefinition tbd, String nullColumnHack,
                                     ContentValues values, int conflictAlgorithm) {
        return db.insertWithOnConflict(tbd.name(), nullColumnHack, values, conflictAlgorithm);
    }

    /**
     * Wird immer am Ende einer (kompletten) Transaktion gerufen, d.h, wenn eine Transaktion
     * geschachtelt ist, wird erst nach Ende der ersten begonnen Transaktion diese Methode gerufen.
     * <p>
     * Dies funktioniert z.B. mit folgendem Code:
     * <pre>
     * <code>
     *
     *     ContentResolver resolver = context.getContentResolver();
     *     for (AWLibAbstractDBDefinition tbd usedTables) {
     *     resolver.notifyChange(tbd.getUri(), null);
     *     switch (tbd.name()) {}
     *     case
     *     "Tabellenname":
     *      resolver.notifyChange(tabellenname.getUri(), null);
     *
     *
     * </code>
     * </pre>
     *
     * @param tables
     *         Tabellen, die waehrend der gesamten Transaktion benutzt wurden
     */
    protected abstract void notifyCursors(Set<AWLibAbstractDBDefinition> tables);

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
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public int update(AWLibAbstractDBDefinition tbd, ContentValues content, String selection,
                      String[] selectionArgs) {
        usedTables.add(tbd);
        int rows = db.update(tbd.name(), content, selection, selectionArgs);
        if (!db.inTransaction()) {
            notifyCursors(usedTables);
        }
        return rows;
    }
}

