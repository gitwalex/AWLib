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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.application.AWLIbApplication;

/**
 * Helper-Klasse fuer Aenderungen/Neuanlagen in der DB
 */
public class AWLibDBAlterHelper {
    private static int indexNumber;
    private static int uniqueIndexNumber;
    private final SQLiteDatabase database;
    private final String uniqueIndexNumberKey = "DBUniqueIndexNumber";
    private final String indexNumberKey = "DBIndexNumber";
    private final SharedPreferences prefs;
    private final Context context;

    /**
     * Initialisiert AWLibDBAlterHelper. Die letzte vergebene indexNummer/uniqueIndexNummer wird aus
     * Preferences gelesen.
     *
     * @param database
     *         database
     */
    public AWLibDBAlterHelper(Context context, SQLiteDatabase database) {
        this.database = database;
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        indexNumber = prefs.getInt(indexNumberKey, 0);
        uniqueIndexNumber = prefs.getInt(uniqueIndexNumberKey, 0);
    }

    /**
     * Aendert einen Index. Die Indices werden durchnummeriert.
     *
     * @param tbd
     *         DBDefinition
     */
    public void alterIndex(AWLibAbstractDBDefinition tbd) {
        alterIndex(tbd, "I" + indexNumber);
        indexNumber++;
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(indexNumberKey, indexNumber).apply();
    }

    /**
     * Aendert einen Index
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     * @param indexName
     *         Name des Index
     */
    public void alterIndex(AWLibAbstractDBDefinition tbd, String indexName) {
        int[] indexItems = tbd.getIndex();
        if (indexItems != null) {
            alterIndex(tbd, indexName, indexItems);
        }
    }

    /**
     * Aendert einen Index
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     * @param indexName
     *         Name des Index
     * @param indexItems
     *         Items des Index
     */
    public void alterIndex(AWLibAbstractDBDefinition tbd, String indexName, int[] indexItems) {
        if (!tbd.isView()) {
            dropIndex(tbd);
            String sql = getCreateIndexSQL(tbd, indexName, indexItems, false);
            database.execSQL(sql);
        }
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
    public void alterIndex(AWLibAbstractDBDefinition tbd, String indexName, String indexColumns) {
        dropIndex(tbd);
        String sql = "CREATE ";
        sql = sql + "INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" + indexColumns +
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
    public void alterIndex(AWLibAbstractDBDefinition tbd, String indexName, String indexColumns,
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
     *         AWLibAbstractDBDefinition
     * @param fromColumns
     *         Spalten, die kopiert werden sollen
     */
    public void alterTable(AWLibAbstractDBDefinition tbd, int[] fromColumns) {
        String colums = tbd.getCommaSeperatedList(fromColumns);
        String tempTableName = "temp" + tbd.name();
        String createTempTable = "CREATE TABLE " + tempTableName + getCreateTableSQL(tbd);
        String copyValuesSQL = "INSERT INTO temp" + tbd.name() + " (" + colums + ") SELECT " +
                colums + " FROM " + tbd.name();
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
        createIndex(tbd);
        tbd.createDatabase(this);
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an. Indices werden neu angelegt.
     * Nacharbeiten durch die Tabelle selbst werden ermoeglicht durch Aufruf {@link
     * AWLibAbstractDBDefinition#createDatabase(AWLibDBAlterHelper)}
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     * @param newColumn
     *         neue Column
     */
    public void alterTableAddColumn(AWLibAbstractDBDefinition tbd, int newColumn) {
        String colName = tbd.columnName(newColumn);
        String format = tbd.getSQLiteFormat(newColumn);
        String sql = "ALTER TABLE " + tbd.name() + " ADD " + colName + " " + format;
        database.execSQL(sql);
        createIndex(tbd);
        tbd.createDatabase(this);
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an. Indices werden neu angelegt.
     * Nacharbeiten durch die Tabelle selbst werden ermoeglicht durch Aufruf {@link
     * AWLibAbstractDBDefinition#createDatabase(AWLibDBAlterHelper)}. Der defualtWert wird in die
     * neue Column eingetragen
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     * @param newColumn
     *         neue Column
     */
    public void alterTableAddColumn(AWLibAbstractDBDefinition tbd, int newColumn,
                                    String defaultWert) {
        alterTableAddColumn(tbd, newColumn);
        String colName = tbd.columnName(newColumn);
        String sql = "UPDATE " + tbd.name() + " SET  " + colName + " = " + defaultWert;
        database.execSQL(sql);
    }

    /**
     * Aendert eine Tabelle und haengt eine neuen Column hinten an. Indices werden neu angelegt.
     * Nacharbeiten durch die Tabelle selbst werden ermoeglicht durch Aufruf {@link
     * AWLibAbstractDBDefinition#createDatabase(AWLibDBAlterHelper)}
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     * @param newColumn
     *         neue Column
     */
    public void alterTableAddColumn(AWLibAbstractDBDefinition tbd, String newColumn) {
        String sql = "ALTER TABLE " + tbd.name() + " ADD " + newColumn + " TEXT";
        database.execSQL(sql);
        createIndex(tbd);
        tbd.createDatabase(this);
    }

    public void alterTableDistinct(AWLibAbstractDBDefinition tbd, String distinctColumn) {
        String tempTableName = "temp" + tbd.name();
        String createTempTable = "CREATE TABLE " + tempTableName + getCreateTableSQL(tbd);
        String oldColumnNames = tbd.getCommaSeperatedList(tbd.getCreateTableResIDs());
        String copyValuesSQL =
                "INSERT INTO temp" + tbd.name() + " (" + oldColumnNames + ") SELECT " +
                        oldColumnNames +
                        " FROM " +
                        tbd.name() + " GROUP BY " + distinctColumn;
        database.execSQL(createTempTable);
        database.execSQL(copyValuesSQL);
        dropTable(tbd.name());
        renameTable(tempTableName, tbd.name());
        createIndex(tbd);
        tbd.createDatabase(this);
    }

    /**
     * Aendert eine Tabelle. Die neue Tabelle hat keine geaenderten Columns, nur die gleichen oder
     * weniger! Alle Indices werden wieder angelegt. Nacharbeiten durch die Tabelle selbst werden
     * ermoeglicht durch Aufruf {@link AWLibAbstractDBDefinition#createDatabase(AWLibDBAlterHelper)}
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     */
    public void alterTableOnlyDeletedColumns(AWLibAbstractDBDefinition tbd) {
        AWLIbApplication.Log("Alter Table: " + tbd.name());
        if (tbd.doCreate()) {
            String tempTableName = "temp" + tbd.name();
            String createTempTable = "CREATE TABLE " + tempTableName + getCreateTableSQL(tbd);
            String oldColumnNames = tbd.getCommaSeperatedList(tbd.getCreateTableResIDs());
            String copyValuesSQL =
                    "INSERT INTO temp" + tbd.name() + " (" + oldColumnNames + ") SELECT " +
                            oldColumnNames +
                            " FROM " +
                            tbd.name();
            database.execSQL(createTempTable);
            database.execSQL(copyValuesSQL);
            dropTable(tbd.name());
            renameTable(tempTableName, tbd.name());
            createIndex(tbd);
            tbd.createDatabase(this);
        }
    }

    /**
     * Aendert einen Index. Die Indices werden durchnummeriert.
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     */
    public void alterUniqueIndex(AWLibAbstractDBDefinition tbd) {
        alterUniqueIndex(tbd, "U" + uniqueIndexNumber);
        uniqueIndexNumber++;
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putInt(uniqueIndexNumberKey, uniqueIndexNumber).apply();
    }

    /**
     * Aendert einen UniqueIndex
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     */
    public void alterUniqueIndex(AWLibAbstractDBDefinition tbd, String indexName) {
        int[] uniqueIndexItems = tbd.getUniqueIndex();
        if (uniqueIndexItems != null) {
            alterUniqueIndex(tbd, indexName, uniqueIndexItems);
        }
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
    public void alterUniqueIndex(AWLibAbstractDBDefinition tbd, String indexName,
                                 int[] uniqueIndexItems) {
        dropIndex(indexName);
        String sql = getCreateIndexSQL(tbd, indexName, uniqueIndexItems, true);
        database.execSQL(sql);
    }

    /**
     * Aendert eine View. Create wird aus {@link AWLibAbstractDBDefinition#getCreateViewSQL()}
     * geholt
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     */
    public void alterView(AWLibAbstractDBDefinition tbd) {
        String viewSQL = tbd.getCreateViewSQL();
        if (viewSQL != null) {
            dropView(tbd);
            String sql = ("CREATE VIEW " + tbd.name() + " AS " + viewSQL);
            database.execSQL(sql);
        }
    }

    public void copyValues(AWLibAbstractDBDefinition from, int[] columns,
                           AWLibAbstractDBDefinition to) {
        String mColumns = from.getCommaSeperatedList(columns);
        String sql = "INSERT INTO " + to.name() + " (" + mColumns + ") SELECT " +
                mColumns +
                " FROM " +
                from.name();
        database.execSQL(sql);
    }

    /**
     * Legt alle Indices zu einer Tabelle wieder an.
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     */
    public void createIndex(AWLibAbstractDBDefinition tbd) {
        //        String orderIndex = getCreateIndexSQL(tbd, "O" + tbd.name(), tbd.getOrderByItems(), false);
        //        database.execSQL(orderIndex);
        alterIndex(tbd);
        alterUniqueIndex(tbd);
    }

    /**
     * Legt eine neue Tabelle an. Eine vorhandene Tabelle mit diesem Namen wird geloescht. ein
     * eventuell vorhandener Index bzw. UniqueIndex wird mit angelegt.
     *
     * @param tbd
     *         AWLibAbstractDBDefinition
     */
    public void createTable(AWLibAbstractDBDefinition tbd) {
        dropTable(tbd.name());
        String sql = "CREATE TABLE IF NOT EXISTS " + tbd.name() + " " + getCreateTableSQL(tbd);
        database.execSQL(sql);
        createIndex(tbd);
    }

    /**
     * Legt eine Tabelle mit Namen und Spalten an
     *
     * @param tablename
     *         Tabellenname
     * @param colums
     *         Spalten
     */
    public void createTable(String tablename, String[] colums) {
        dropTable(tablename);
        StringBuilder sql = new StringBuilder(" ( ");
        sql.append("tempID INTEGER PRIMARY KEY ");
        for (String colName : colums) {
            sql.append(", ").append(colName).append(" TEXT");
        }
        sql.append(")");
        String createSQL = "CREATE TABLE IF NOT EXISTS " + tablename + sql.toString();
        database.execSQL(createSQL);
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
     *         AWLibAbstractDBDefinition
     */
    public void dropIndex(AWLibAbstractDBDefinition tbd) {
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
     *         AWLibAbstractDBDefinition
     */
    public void dropView(AWLibAbstractDBDefinition tbd) {
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

    /**
     * Liefert die Tabellenspalten der Tabelle ohne _id zuruck.
     *
     * @param tbd
     *         tbd
     * @param tableindex
     *         Tableindex
     *
     * @return commaseperated List der Spalten
     */
    public String getCommaSeperatedListNoID(@NonNull AWLibAbstractDBDefinition tbd,
                                            @NonNull int[] tableindex) {
        List<String> columns = new ArrayList<>();
        for (int resID : tableindex) {
            columns.add(tbd.columnName(resID));
        }
        String idColumn = context.getString(R.string._id);
        columns.remove(idColumn);
        StringBuilder indexSQL = new StringBuilder(columns.get(0));
        for (int j = 1; j < columns.size(); j++) {
            String column = columns.get(j);
            indexSQL.append(", ").append(column);
        }
        return indexSQL.toString();
    }

    public String getCreateIndexSQL(AWLibAbstractDBDefinition tbd, String indexName, int[] columns,
                                    boolean uniqueIndex) {
        String sql = "CREATE ";
        if (uniqueIndex) {
            sql = sql + "UNIQUE ";
        }
        sql = sql + "INDEX IF NOT EXISTS " + indexName + " on " + tbd.name() + " (" +
                tbd.getCommaSeperatedList(columns) + ")";
        return sql;
    }

    /**
     * @return den String fuer den Aubau eine Tabelle (ohne CREATE TABLE AS name)
     */
    public String getCreateTableSQL(AWLibAbstractDBDefinition tbd) {
        StringBuilder sql = new StringBuilder(" ( ");
        boolean id = true;
        for (int resID : tbd.getCreateTableItems()) {
            String colName = tbd.columnName(resID);
            String format = tbd.getSQLiteFormat(resID);
            if (id) {
                sql.append(colName).append(" INTEGER PRIMARY KEY ");
                id = false;
            } else {
                sql.append(", ").append(colName).append(" ");
                sql.append(format);
            }
        }
        sql.append(")");
        return sql.toString();
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
