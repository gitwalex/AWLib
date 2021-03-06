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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.aw.awlib.application.AWApplication;

/**
 * Helper-Klasse fuer Aenderungen/Neuanlagen in der DB
 */
public final class AWDBAlterHelper implements TableColumns {
    private final SQLiteDatabase database;

    /**
     * Initialisiert AWDBAlterHelper. Die letzte vergebene indexNummer/uniqueIndexNummer wird aus
     * Preferences gelesen.
     */
    public AWDBAlterHelper(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * Legt einen Index an.
     *
     * @param tbd
     *         Tabelle
     * @param indexName
     *         Name des Index
     * @param indexColumns
     *         Spalten des Index, Komme getrennt
     */
    public void alterIndex(AWAbstractDBDefinition tbd, String indexName, String[] indexColumns) {
        String columns = AbstractDBHelper.getCommaSeperatedList(indexColumns);
        dropIndex(tbd);
        String sql =
                "CREATE INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" + columns +
                        ")";
        database.execSQL(sql);
    }

    /**
     * Legt einen Index an.
     *
     * @param tbd
     *         Tabelle
     * @param indexName
     *         Name des Index
     * @param indexColumns
     *         Spalten des Index, Komme getrennt
     * @param selection
     *         Selection fuer den Index
     */
    public void alterIndex(AWAbstractDBDefinition tbd, String indexName, String indexColumns,
                           String selection) {
        dropIndex(tbd);
        String sql = "CREATE ";
        sql = sql + "INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" + indexColumns +
                ")" + selection;
        database.execSQL(sql);
    }

    /**
     * Legt eine Tabelle mit neuen Spalten an. Inhalte werden kopiert, die zu kopierenden Felder
     * werden ueber fromColumn vorgegeben
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param fromColumns
     *         Spalten, die kopiert werden sollen
     */
    public void alterTable(AWAbstractDBDefinition tbd, String[] fromColumns) {
        String colums = AbstractDBHelper.getCommaSeperatedList(fromColumns);
        String tempTableName = "temp" + tbd.name();
        String createTempTable =
                "CREATE TABLE " + tempTableName + "(" + tbd.getCreateViewSQL() + ")";
        String copyValuesSQL =
                "INSERT INTO temp" + tbd.name() + " (" + colums + ") SELECT " + colums + " FROM " +
                        tbd.name();
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an. Indices werden neu angelegt. Der
     * defaultWert wird in die neue Column eingetragen
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param columnname
     *         Name neue Column
     * @param columnformat
     *         Formaat der Column (TEXT, INTEGER...)
     * @param defaultWert
     *         defaultwert der column nach einfuegen
     */
    public void alterTableAddColumn(AWAbstractDBDefinition tbd, String columnname,
                                    String columnformat, String defaultWert) {
        String sql = "UPDATE " + tbd.name() + " SET  " + columnname + " = " + defaultWert;
        database.execSQL(sql);
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an.
     *
     * @param tbd
     *         AWAbstractDBDefinition
     * @param columnname
     *         Name neue Column
     * @param columnformat
     *         Formaat der Column (TEXT, INTEGER...)
     */
    public void alterTableAddColumn(AWAbstractDBDefinition tbd, String columnname,
                                    String columnformat) {
        String sql = "ALTER TABLE " + tbd.name() + " ADD " + columnname + " " + columnformat;
        database.execSQL(sql);
    }

    public void alterTableDistinct(AWAbstractDBDefinition tbd, String distinctColumn) {
        String tempTableName = "temp" + tbd.name();
        String createTempTable =
                "CREATE TABLE " + tempTableName + "(" + tbd.getCreateViewSQL() + ")";
        String oldColumnNames = AbstractDBHelper.getCommaSeperatedList(tbd.getTableColumns());
        String copyValuesSQL =
                "INSERT INTO temp" + tbd.name() + " (" + oldColumnNames + ") SELECT " +
                        oldColumnNames + " FROM " + tbd.name() + " GROUP BY " + distinctColumn;
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
    }

    /**
     * Aendert eine Tabelle. Die neue Tabelle hat keine geaenderten Columns, nur die gleichen oder
     * weniger!
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void alterTableOnlyDeletedColumns(AWAbstractDBDefinition tbd) {
        AWApplication.Log("Alter Table: " + tbd.name());
        String tempTableName = "temp" + tbd.name();
        String createTempTable =
                "CREATE TABLE " + tempTableName + "( " + tbd.getCreateViewSQL() + ")";
        String oldColumnNames = AbstractDBHelper.getCommaSeperatedList(tbd.getTableColumns());
        String copyValuesSQL =
                "INSERT INTO temp" + tbd.name() + " (" + oldColumnNames + ") SELECT " +
                        oldColumnNames + " FROM " + tbd.name();
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
    }

    /**
     * Aendert eine UniqueIndex
     *
     * @param tbd
     *         tbd
     * @param indexName
     *         IndexName
     * @param uniqueIndexItems
     *         IndexItems
     */
    public void alterUniqueIndex(AWAbstractDBDefinition tbd, String indexName,
                                 String[] uniqueIndexItems) {
        String columns = AbstractDBHelper.getCommaSeperatedList(uniqueIndexItems);
        dropIndex(indexName);
        String sql = "CREATE UNIQUE INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" +
                columns + ")";
        database.execSQL(sql);
    }

    /**
     * Aendert eine View. Create wird aus {@link AWAbstractDBDefinition#getCreateViewSQL()} geholt
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void alterView(AWAbstractDBDefinition tbd) {
        String viewSQL = tbd.getCreateViewSQL();
        if (viewSQL != null) {
            dropView(tbd);
            String sql = ("CREATE VIEW " + tbd.name() + " AS " + viewSQL);
            database.execSQL(sql);
            checkView(tbd);
        }
    }

    public void alterWPUMsatz(AWAbstractDBDefinition tbd) {
        String tempTableName = "temp" + tbd.name();
        String createTempTable =
                "CREATE TABLE " + tempTableName + "( " + tbd.getCreateViewSQL() + ")";
        String oldColumnNames = AbstractDBHelper.getCommaSeperatedList(tbd.getTableColumns());
        String copyValuesSQL =
                "INSERT INTO temp" + tbd.name() + " (" + oldColumnNames + ") SELECT " +
                        oldColumnNames + " FROM " + tbd.name();
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
    }

    /**
     * Versorgt die Statistiktabellen
     */
    public void analyze() {
        database.execSQL("analyze");
    }

    private boolean checkView(AWAbstractDBDefinition tbd) {
        ContentValues cv = new ContentValues();
        AWApplication.Log("View wird ueberprueft: " + tbd.name());
        List<String> fehler = new ArrayList<>();
        String[] projection = tbd.getTableColumns();
        Cursor c = database.query(tbd.name(), projection, null, null, null, null, null);
        try {
            for (String column : c.getColumnNames()) {
                cv.put(column, "test");
            }
        } finally {
            c.close();
        }
        for (String column : projection) {
            if (cv.get(column) == null) {
                fehler.add("In " + tbd.name() + " column nicht gefunden: " + column);
            }
        }
        for (String value : fehler) {
            AWApplication.Log(value);
        }
        return fehler.size() == 0;
    }

    public void copyValues(AWAbstractDBDefinition from, String[] columns,
                           AWAbstractDBDefinition to) {
        String mColumns = AbstractDBHelper.getCommaSeperatedList(columns);
        String sql =
                "INSERT INTO " + to.name() + " (" + mColumns + ") SELECT " + mColumns + " FROM " +
                        from.name();
        database.execSQL(sql);
    }

    /**
     * Legt eine neue Tabelle an. Eine vorhandene Tabelle mit diesem Namen wird geloescht. ein
     * eventuell vorhandener Index bzw. UniqueIndex wird mit angelegt.
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void createTable(AWAbstractDBDefinition tbd) {
        dropTable(tbd.name());
        String sql =
                "CREATE TABLE IF NOT EXISTS " + tbd.name() + " ( " + tbd.getCreateViewSQL() + ")";
        database.execSQL(sql);
    }

    /**
     * Dropt alle Indices der Datenbank.
     */
    public void dropAllIndices() {
        String selection = "SELECT name FROM sqlite_master WHERE type == 'index'";
        Cursor c = database.rawQuery(selection, null);
        if (c.moveToFirst()) {
            String name = c.getString(0);
            dropIndex(name);
        }
        c.close();
    }

    /**
     * Dropt einen Index
     *
     * @param name
     *         Name
     */
    public void dropIndex(String name) {
        String sql = "DROP INDEX IF EXISTS " + name;
        database.execSQL(sql);
    }

    /**
     * Loescht einen (Unique-) Index ersatzlos
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void dropIndex(AWAbstractDBDefinition tbd) {
        String sql = "DROP INDEX IF EXISTS " + tbd.name();
        database.execSQL(sql);
    }

    /**
     * Loescht eine Tabelle ersatzlos
     *
     * @param tablename
     *         Name
     */
    public void dropTable(String tablename) {
        String sql = "DROP TABLE IF EXISTS " + tablename;
        database.execSQL(sql);
    }

    /**
     * Loescht eine View ersatzlos
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public void dropView(AWAbstractDBDefinition tbd) {
        dropView(tbd.name());
    }

    /**
     * Dropt eine View mit Namen
     *
     * @param s
     *         Name der View
     */
    public void dropView(String s) {
        String sql = "DROP VIEW IF EXISTS " + s;
        database.execSQL(sql);
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    /**
     * Umbenennung einer Tabelle
     *
     * @param oldName
     *         alter Name der Tabelle
     * @param newName
     *         neuer Name der Tabelle
     */
    public void renameTable(String oldName, String newName) {
        String renameTableSQL = "ALTER TABLE " + oldName + " RENAME TO " + newName;
        database.execSQL(renameTableSQL);
    }

    /**
     * Komprimiert die Datenbank
     */
    public void vacuum() {
        database.execSQL("vacuum");
    }
}
