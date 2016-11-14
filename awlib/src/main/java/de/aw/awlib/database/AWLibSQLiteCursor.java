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

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteQuery;

/**
 * Cursor, der die Zeit zum Ende der Abfrage festhaelt. Diese Zeit kann dann mittels {@link
 * AWLibSQLiteCursor#getFinishTime()} abgefragt werden.
 */
public class AWLibSQLiteCursor extends SQLiteCursor {
    private final long finishTime;

    public AWLibSQLiteCursor(SQLiteCursorDriver driver, String editTable, SQLiteQuery query) {
        super(driver, editTable, query);
        finishTime = System.nanoTime();
    }

    @Override
    protected void finalize() {
        if (!isClosed()) {
            close();
        }
        super.finalize();
    }

    public long getFinishTime() {
        return finishTime;
    }
}
