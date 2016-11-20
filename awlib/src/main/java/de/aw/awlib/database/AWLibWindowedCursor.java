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

import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

/**
 * Ein AbstractWindowedCursor. Erlaubt die Erstellung eines Cursors, der ohne SQL-DB gefuellt werden
 * kann. In der ersten Ausbaustufe wird das Kopieren der Daten eines Cursors angeboten.
 */
public class AWLibWindowedCursor extends AbstractWindowedCursor {
    private final String[] mColums;
    private DataBinder dataBinder;

    public AWLibWindowedCursor(String[] colums) {
        mColums = colums;
    }

    /**
     * Kopiert einen Cursor in das CursorWindow dieses WindowedCursors.
     * <p>
     * Ist ein DataBinder gesetzt, wird dieser entsprechend gerufen. Ist kein DataBinder gesetzt,
     * wird der Cursor 1:1 kopiert.
     * <p>
     *
     * @param theCursor
     *         Cursor, der kopiert werden soll; das schliessen des Cursors muss die ruefende Klasse
     *         durchfuhren.
     */
    public final void copyValues(@NonNull Cursor theCursor) {
        CursorWindow data = new CursorWindow("AWLibWindowedCursor");
        data.setNumColumns(theCursor.getColumnCount());
        if (theCursor.moveToFirst()) {
            do {
                if (dataBinder != null) {
                    dataBinder.allocRow(data, theCursor);
                }
                data.allocRow();
                int dataRow = data.getNumRows() - 1;
                for (int position = 0; position < theCursor.getColumnCount(); position++) {
                    if (dataBinder != null && !dataBinder
                            .copyValue(mColums[position], position, theCursor, data, dataRow)) {
                        if (!theCursor.isNull(position)) {
                            data.putString(theCursor.getString(position), dataRow, position);
                        }
                    }
                }
            } while (theCursor.moveToNext());
        }
        setWindow(data);
    }

    @Override
    public String[] getColumnNames() {
        return mColums;
    }

    @Override
    public int getCount() {
        if (getWindow() == null) {
            return 0;
        }
        return getWindow().getNumRows();
    }

    /**
     * Setzt einen DataBinder
     *
     * @param binder
     *         DataBinder
     */
    public final void setDataBinder(DataBinder binder) {
        this.dataBinder = binder;
    }

    @CallSuper
    @Override
    public void setWindow(CursorWindow data) {
        if (dataBinder != null) {
            dataBinder.onSetWindow(data);
        }
        super.setWindow(data);
    }

    /**
     * Hook fuer kopieren eines Cursors fuer verschiedene Phasen des Kopiervorganges.
     */
    public interface DataBinder {
        /**
         * Wird vor jedem Hinzufuegen einer neuen Zeile im CursorWindow gerufen.
         *
         * @param data
         *         CursorWindow
         * @param theCursor
         *         zu kopierender Cursor. Cursor steht auf der Zeile, die als naechstes kopiert
         *         wird.
         */
        void allocRow(final CursorWindow data, Cursor theCursor);

        /**
         * Wird fuer jede zu kopierende Spalte gerufen.
         *
         * @param mColum
         *         Name der aktuellen Spalte
         * @param position
         *         Position der Spalte im Cursor
         * @param theCursor
         *         der Cursor.
         * @param data
         *         CursorWindow
         * @param dataRow
         *         aktuelle Zeile des CursorWindow
         *
         * @return true, wenn der Kopiervorgang aus Sicht des Hooks abgeschlossen ist. Bei false
         * wird der Inhalt der Spalte des Cursors 1:1 kopiert.
         */
        boolean copyValue(final String mColum, final int position, final Cursor theCursor,
                          final CursorWindow data, final int dataRow);

        /**
         * Wird unmittelbar vor dem setzen des CursorWindow gerufen. Letzte Moeglichkeit, Daten im
         * CursorWindow zu manipulieren.
         *
         * @param data
         *         CursorWindow
         */
        void onSetWindow(CursorWindow data);
    }
}
