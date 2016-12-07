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

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import de.aw.awlib.R;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AWDBAlterHelper;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * @author Alexander Winkler
 *         <p/>
 *         Aufzaehlung der Tabellen der Datenbank. 1. Parameter ist ein Integer-Array der resIds
 *         (R.string.xxx)der Tabellenspalten
 */
@SuppressWarnings("unused")
public enum AWDBDefinition implements Parcelable, AWAbstractDBDefinition {
    RemoteServer() {
        @Override
        public int[] getTableItems() {
            return new int[]{R.string._id//
                    , R.string.column_serverurl//
                    , R.string.column_userID//
                    , R.string.column_connectionType//
                    , R.string.column_maindirectory//
            };
        }
    };
    public static final Creator<AWDBDefinition> CREATOR = new Creator<AWDBDefinition>() {
        @Override
        public AWDBDefinition createFromParcel(Parcel in) {
            return AWDBDefinition.values()[in.readInt()];
        }

        @Override
        public AWDBDefinition[] newArray(int size) {
            return new AWDBDefinition[size];
        }
    };
    private static String mAuthority;
    private Uri mUri;

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
        String spalte = AbstractDBHelper.getInstance().columnName(resID);
        if (fullQualified) {
            return "max(" + name() + "." + spalte + ") AS " + spalte;
        }
        return "max(" + spalte + ") AS " + spalte;
    }

    /**
     * Erstellt SubSelect.
     *
     * @param tbd
     *         AWDBDefinition
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
    public String SQLSubSelect(AWDBDefinition tbd, int resID, String column, String selection,
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
        return AbstractDBHelper.getInstance().columnName(resID);
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
        return AbstractDBHelper.getInstance().columnNames(resIDs);
    }

    /**
     * Wird beim Erstellen der DB Nach Anlage aller Tabellen und Indices gerufen. Hier koennen noch
     * Nacharbeiten durchgefuehrt werden
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
     * Liefert zu einem int-Array die entsprechenden ColumnNamen getrennt durch Kommata zurueck
     *
     * @param tableindex
     *         Array, zu dem die Namen ermittelt werden sollen
     *
     * @return ColumnNamen, Komma getrennt
     */
    public String getCommaSeperatedList(@NonNull int[] tableindex) {
        return AbstractDBHelper.getInstance().getCommaSeperatedList(tableindex);
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
     */
    public char getFormat(int resID) {
        return AbstractDBHelper.getInstance().getFormat(resID);
    }

    /**
     * Liste der fuer eine sinnvolle Sortierung notwendigen Spalten.
     *
     * @return ResId der Spalten, die zu einer Sortierung herangezogen werden sollen.
     */
    public int[] getOrderByItems() {
        return new int[]{getTableItems()[0]};
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
        return AbstractDBHelper.getInstance().getCommaSeperatedList(orderColumns);
    }

    @Override
    public Uri getUri() {
        if (mUri == null) {
            mUri = Uri.parse("content://" + mAuthority + "/" + name());
        }
        return mUri;
    }

    /**
     * Indicator, ob AWDBDefinition eine View ist. Default false
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