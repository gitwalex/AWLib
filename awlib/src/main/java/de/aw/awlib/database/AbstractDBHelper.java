/*
 * MonMa: Eine freie Android-Application fuer die Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
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
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.application.AWApplication;

import static de.aw.awlib.application.AWApplication.Log;
import static de.aw.awlib.application.AWApplication.LogError;

/**
 * Helper fuer die SQLite-Database
 */
@SuppressWarnings({"WeakerAccess", "TryFinallyCanBeTryWithResources", "unused"})
public abstract class AbstractDBHelper extends SQLiteOpenHelper
        implements AWInterface, TableColumns {
    private final ContentResolver mContentresolver;
    private final Resources mResources;
    /**
     * DBHelperTemplate ist ein Singleton.
     */
    private SQLiteDatabase db;
    private Set<Uri> usedTables = new HashSet<>();

    protected AbstractDBHelper(Context context, SQLiteDatabase.CursorFactory cursorFactory) {
        super(context, ((AWApplication) context.getApplicationContext()).theDatenbankname(),
                cursorFactory,
                ((AWApplication) context.getApplicationContext()).theDatenbankVersion());
        mContentresolver = context.getContentResolver();
        mResources = context.getResources();
    }

    public static String SQLMaxItem(String column) {
        return "max(" + column + ") AS " + column;
    }

    /**
     * Liefert zu einem Spaltennamen ein SUM(resID) zurueck.
     *
     * @param column
     *         Name der Spalte
     *
     * @return Select SUM im Format SUM(itemname) AS itemname
     */
    public static String SQLSumItem(String column) {
        return "sum(" + column + ") AS " + column;
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
    public static String getCommaSeperatedList(@NonNull String... columns) {
        StringBuilder indexSQL = new StringBuilder(columns[0]);
        for (int j = 1; j < columns.length; j++) {
            String column = columns[j];
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    /**
     * siehe {@link SQLiteDatabase#beginTransaction()}
     */
    public final void beginTransaction() {
        db = getWritableDatabase();
        if (!db.inTransaction()) {
            usedTables.clear();
        }
        db.beginTransaction();
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])} Befindet sich die Datenbank
     * nicht innerhalb einer Transaktion wird {@link AbstractDBHelper#notifyCursors(Uri)} gerufen.
     */
    public final int delete(AWAbstractDBDefinition tbd, String selection, String[] selectionArgs) {
        return delete(tbd.getUri(), selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#delete(String, String, String[])} Befindet sich die Datenbank
     * nicht innerhalb einer Transaktion wird {@link AbstractDBHelper#notifyCursors(Uri)} gerufen.
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
     * siehe {@link SQLiteDatabase#endTransaction()} Transaktionen koennen geschachtelt werden. Erst
     * wenn keine Transaktion mehr ansteht, wird mit jeder in der gesamten Transaction genutzen Uri
     * {@link AbstractDBHelper#notifyCursors(Uri)} gerufen.
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

    public final Resources getApplicationResources() {
        return mResources;
    }

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

    public ContentResolver getContentResolver() {
        return mContentresolver;
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
    public String[] getOrderByItems(AWAbstractDBDefinition tbd) {
        return new String[]{tbd.getTableColumns()[0]};
    }

    /**
     * OrderBy-String - direkt fuer SQLITE verwendbar.
     *
     * @return OrderBy-String, wie in der Definition der ENUM vorgegeben
     */
    public final String getOrderString(AWAbstractDBDefinition tbd) {
        String[] orderColumns = getOrderByItems(tbd);
        return orderColumns == null ? null : getCommaSeperatedList(orderColumns);
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
     * Liefert eine Liste der Tabellennamen zurueck, in der der Name der Spalte vorkommt
     *
     * @param columnName
     *         Spaltenname
     *
     * @return Liste der Tabellennamen. Kann leer sein.
     */
    public final List<AWAbstractDBDefinition> getTableNamesForColumn(String columnName) {
        List<String> tables = new ArrayList<>();
        String[] projection = new String[]{"name"};
        String selection = " sql LIKE '%" + columnName + "%' AND " + " type = 'table'";
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
        List<AWAbstractDBDefinition> tbdList = new ArrayList<>();
        for (String table : tables) {
            tbdList.add(getDBDefinition(table));
        }
        return tbdList;
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
     * siehe {@link SQLiteDatabase#insert(String, String, ContentValues)} Befindet sich die
     * Datenbank nicht innerhalb einer Transaktion wird {@link AbstractDBHelper#notifyCursors(Uri)}
     * gerufen.
     */
    public final long insert(AWAbstractDBDefinition tbd, String nullColumnHack,
                             ContentValues content) {
        return insert(tbd.getUri(), nullColumnHack, content);
    }

    /**
     * siehe {@link SQLiteDatabase#insert(String, String, ContentValues)} Befindet sich die
     * Datenbank nicht innerhalb einer Transaktion wird {@link AbstractDBHelper#notifyCursors(Uri)}
     * gerufen.
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
     * Dies funktioniert z.B. mit folgendem Code:
     * <pre>
     * <code>
     * super.notifyCursors(usedTables);
     * ContentResolver resolver = context.getContentResolver();
     * DBDefinition tbd = DBDefinition.valueOf(uri.getLastPathSegment());
     * switch (tbd) {
     * case BankRegelm:
     *      resolver.notifyChange(tbd.getUri(), null);
     *      break;
     * ...
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
        mContentresolver.notifyChange(uri, null);
        return uri.getLastPathSegment().equals(AWDBDefinition.RemoteServer.name());
    }

    @Override
    public final void onCreate(SQLiteDatabase database) {
        AWDBAlterHelper dbhelper = new AWDBAlterHelper(database);
        database.beginTransaction();
        try {
            for (AWAbstractDBDefinition tbd : getAllDBDefinition()) {
                if (tbd.doCreate() && !tbd.isView()) {
                    dbhelper.createTable(tbd);
                }
            }
            for (AWAbstractDBDefinition tbd : getAllDBDefinition()) {
                if (tbd.doCreate() && tbd.isView()) {
                    dbhelper.alterView(tbd);
                }
            }
            for (AWDBDefinition tbd : AWDBDefinition.values()) {
                if (tbd.doCreate() && !tbd.isView()) {
                    dbhelper.createTable(tbd);
                }
            }
            for (AWDBDefinition tbd : AWDBDefinition.values()) {
                if (tbd.doCreate() && tbd.isView()) {
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
        AWDBAlterHelper dbhelper = new AWDBAlterHelper(database);
        database.beginTransaction();
        try {
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
            Log("DatenbankUpgrade von Version " + oldVersion + " nach " + newVersion +
                    " erfolgreich!");
        } catch (Exception e) {
            LogError("DatenbankUpgrade von Version " + oldVersion + " nach " + newVersion +
                    " fehlgeschlagen!");
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
     * siehe {@link SQLiteDatabase#update(String, ContentValues, String, String[])} Befindet sich
     * die Datenbank nicht innerhalb einer Transaktion wird
     * {@link AbstractDBHelper#notifyCursors(Uri)}
     * gerufen.
     */
    public final int update(AWAbstractDBDefinition tbd, ContentValues content, String selection,
                            String[] selectionArgs) {
        return update(tbd.getUri(), content, selection, selectionArgs);
    }

    /**
     * siehe {@link SQLiteDatabase#update(String, ContentValues, String, String[])} Befindet sich
     * die Datenbank nicht innerhalb einer Transaktion wird
     * {@link AbstractDBHelper#notifyCursors(Uri)}
     * gerufen.
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

    /**
     * @author Alexander Winkler
     * <p/>
     * Aufzaehlung der Tabellen der Datenbank. 1. Parameter ist ein Integer-Array der resIds
     * (R.string.xxx)der Tabellenspalten
     */
    @SuppressWarnings("unused") public enum AWDBDefinition
            implements Parcelable, AWAbstractDBDefinition, TableColumns {
        /**
         * Definition fuer Calendar
         */
        AndroidCalendar() {
            public Uri mUri;

            @Override
            public String[] getTableColumns() {
                return new String[]{_id};
            }

            @Override
            public boolean doCreate() {
                return false;
            }

            @Override
            public String getCreateViewSQL() {
                return null;
            }

            @Override
            public Uri getUri() {
                if (mUri == null) {
                    mUri = Uri.parse("content://com.android.calendar/calendars");
                }
                return mUri;
            }
        }, RemoteServer() {
            @Override
            public String getCreateViewSQL() {
                return _id + " INTEGER PRIMARY KEY, " //
                        + column_serverurl + "TEXT, " //
                        + column_userID + " TEXT, " //
                        + column_connectionType + " + TEXT, "//
                        + column_maindirectory + " +  TEXT" //
                        + ")";
            }

            @Override
            public String[] getTableColumns() {
                return new String[]{_id, column_serverurl, column_userID, column_connectionType,
                        column_maindirectory};
            }
        };
        public static final Parcelable.Creator<AWDBDefinition> CREATOR =
                new Parcelable.Creator<AWDBDefinition>() {
                    @Override
                    public AWDBDefinition createFromParcel(Parcel in) {
                        return AWDBDefinition.values()[in.readInt()];
                    }

                    @Override
                    public AWDBDefinition[] newArray(int size) {
                        return new AWDBDefinition[size];
                    }
                };
        private String mAuthority;
        private Uri mUri;
        //private AbstractDBHelper dbHelper;

        /**
         * Wird beim Erstellen der DB Nach Anlage aller Tabellen und Indices gerufen. Hier koennen
         * noch Nacharbeiten durchgefuehrt werden
         *
         * @param helper
         *         AWDBAlterHelper database
         */
        public void createDatabase(AWDBAlterHelper helper) {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Indicator, ob AbstractDBHelper.AWDBDefinition angelegt werden soll. Default true
         *
         * @return true
         */
        @Override
        public boolean doCreate() {
            return true;
        }

        /**
         * @return den String fuer den Aubau einer View (ohne CREATE View AS name). Muss bei Views
         * ueberscheiben werden. Standard: null
         */
        public abstract String getCreateViewSQL();

        /**
         * Liefert ein Array der Columns zurueck, nach den sortiert werden sollte,
         *
         * @return Array der Columns, nach denen sortiert werden soll.
         */
        public String[] getOrderByColumns() {
            return new String[]{getTableColumns()[0]};
        }

        /**
         * OrderBy-String - direkt fuer SQLITE verwendbar.
         *
         * @return OrderBy-String, wie in der Definition der ENUM vorgegeben
         */
        public String getOrderString() {
            String[] orderColumns = getOrderByColumns();
            StringBuilder order = new StringBuilder(orderColumns[0]);
            for (int i = 1; i < orderColumns.length; i++) {
                order.append(", ").append(orderColumns[i]);
            }
            return order.toString();
        }

        @Override
        public Uri getUri() {
            if (mUri == null) {
                mUri = Uri.parse("content://" + mAuthority + "/" + name());
            }
            return mUri;
        }

        /**
         * Indicator, ob AbstractDBHelper.AWDBDefinition eine View ist. Default false
         *
         * @return false. Wenn DBDefintion eine View ist, muss dies zwingend ueberschreiben werden,
         * sonst wirds in DBHelper als Tabelle angelegt.
         */
        public boolean isView() {
            return false;
        }

        @Override
        public void setAuthority(String authority) {
            mAuthority = authority;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(ordinal());
        }
    }
}
