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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.CallSuper;

import java.util.HashSet;
import java.util.Set;

import de.aw.awlib.application.AWLIbApplication;

/**
 * Helper fuer Database-CUD. Fuer Read ist der ContentResolver zu nutzen
 */
public class AbstractDBChangeHelper {
    protected final Context context;
    private final SQLiteDatabase db;
    private final ContentResolver resolver;
    private Set<Uri> usedTables = new HashSet<>();

    public AbstractDBChangeHelper() {
        this.context = AWLIbApplication.getContext();
        resolver = context.getContentResolver();
        db = AbstractDBHelper.getDatabase();
    }

    /**
     * siehe {@link SQLiteDatabase#beginTransaction()}
     */
    public void beginTransaction() {
        db.beginTransaction();
        usedTables.clear();
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public int delete(AWLibAbstractDBDefinition tbd, String selection, String[] selectionArgs) {
        return delete(tbd.getUri(), selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        usedTables.add(uri);
        int rows = db.delete(uri.getLastPathSegment(), selection, selectionArgs);
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
        return insert(tbd.getUri(), nullColumnHack, content);
    }

    /**
     * siehe {@link SQLiteDatabase#insert(String, String, ContentValues)}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public long insert(Uri uri, String nullColumnHack, ContentValues content) {
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
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public int update(AWLibAbstractDBDefinition tbd, ContentValues content, String selection,
                      String[] selectionArgs) {
        return update(tbd.getUri(), content, selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#update(String, ContentValues, String, String[])}
     * <p>
     * Befindet sich die Datenbank nicht innerhalb einer Transaktion wird {@link
     * AbstractDBChangeHelper#notifyCursors(Set)} gerufen.
     */
    public int update(Uri uri, ContentValues content, String selection, String[] selectionArgs) {
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

