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

import android.net.Uri;
import android.os.Parcelable;

/**
 */
public interface AWLibAbstractDBDefinition extends Parcelable {
    String AUTHORITY = "de.aw.appcontentprovider";

    String columnName(int newColumn);

    String[] columnNames();

    String[] columnNames(int... intArray);

    void createDatabase(AWLibDBAlterHelper dbAlterHelper);

    boolean doCreate();

    String getCommaSeperatedList(int[] columns);

    int[] getCreateTableItems();

    int[] getCreateTableResIDs();

    String getCreateViewSQL();

    char getFormat(int resID);

    int[] getIndex();

    int[] getOrderByItems();

    String getOrderString();

    int[] getResIDs();

    String getSQLiteFormat(int newColumn);

    int[] getUniqueIndex();

    Uri getUri();

    boolean isView();

    String name();

    int ordinal();

    /**
     * Wird geworfen, wenn eine ResID nicht gefunden wurde.
     */
    @SuppressWarnings("serial")
    class ResIDNotFoundException extends RuntimeException {
        public ResIDNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }
}