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
package de.aw.awlib.csvimportexport;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AWDBConvert;

/**
 * Erstellt einen Export der Tabellendaten. Aufbau wie folgt:
 * <p/>
 * 1. Zeile Spaltennamen 2.- (n-1) Zeile: Inhalte der Spalten n. Tabellenname
 */
public class AWCSVExporter {
    private final String filename;
    private final NumberFormat nf;
    //private final Context context;

    /**
     * Konstruktor. Erwartet Activity und filename. Export wird angestossen durch {@link
     * AWCSVExporter#doExport(AWAbstractDBDefinition, Cursor, int[])}
     *
     * @param filename
     *         filename des Exportfiles.
     */
    public AWCSVExporter(@NonNull String filename) {
        nf = NumberFormat.getInstance(Locale.getDefault());
        nf.setGroupingUsed(false);
        Date date = new Date(System.currentTimeMillis());
        Locale locale = Locale.getDefault();
        DateFormat formatter =
                (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale));
        String dateString = formatter.format(date).replace(".", "_");
        this.filename = ((filename + "-" + dateString) + ".csv").replace(":", "");
    }

    /**
     * Exportiert Inhalte eines Cursors.
     *
     * @param tbd
     *         Tabelle, die gelesen wird. Name wird ans Ende des Reports angehaengt
     * @param c
     *         Cursor, dessen Inhalte exportiert werden sollen
     * @param fromResIDs
     *         Tabellenspalten, die exportiert werden. Werden benoetigt, um das Format korrekt
     *         dazustellen. Reihenfolge muss mit den Spalten des Cursors uebereinstimmen
     *
     * @throws IOException
     *         Wenn was schiefgegangen ist
     */
    public void doExport(AWAbstractDBDefinition tbd, Cursor c, int[] fromResIDs)
            throws IOException {
        List<String[]> list = new ArrayList<>();
        String filename = AWApplication.getApplicationExportPath() + "/" + this.filename;
        File file = new File(filename);
        FileOutputStream fos;
        CSVWriter bw = null;
        try {
            if (c != null) {
                if (c.moveToFirst()) {
                    fos = new FileOutputStream(file);
                    bw = new CSVWriter(new OutputStreamWriter(fos, Charset.forName("ISO-8859-1")),
                            ';', CSVWriter.NO_QUOTE_CHARACTER);
                    // Ueberachriften: Spaltennamen
                    String[] columns = new String[c.getColumnCount()];
                    for (int j = 0; j < c.getColumnCount(); j++) {
                        columns[j] = c.getColumnName(j);
                    }
                    list.add(columns);
                    do {
                        // Cursor auslesen
                        columns = new String[c.getColumnCount()];
                        for (int j = 0; j < c.getColumnCount(); j++) {
                            int resID = fromResIDs[j];
                            char format = tbd.getFormat(resID);
                            switch (format) {
                                case 'C':
                                    Long amount = c.getLong(j);
                                    columns[j] = nf.format(amount / AWDBConvert.mCurrencyDigits);
                                    break;
                                case 'D':
                                    String btag = c.getString(j);
                                    columns[j] = AWDBConvert.convertDate(btag);
                                    break;
                                case 'M':
                                    amount = c.getLong(j);
                                    columns[j] = nf.format(amount / AWDBConvert.mNumberDigits);
                                    break;
                                default:
                                    columns[j] = c.getString(j);
                            }
                        }
                        list.add(columns);
                    } while (c.moveToNext());
                    columns = new String[1];
                    columns[0] = tbd.name();
                    list.add(columns);
                    bw.writeAll(list);
                }
            }
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
            if (bw != null) {
                bw.close();
            }
        }
    }

    /**
     * Exportiert eine Tabelle in ein exportfile. Das Exportfile ist unter {@link
     * AWApplication#getApplicationExportPath()} } zu finden
     *
     * @param tbd
     *         Tabelle zum export
     * @param fromResIDs
     *         Liste der resIDS, die exportiert werden sollen. Wenn null, werden alle Spalten der
     *         Tabelle exportiert
     * @param selection
     *         selection.
     * @param selectionArgs
     *         selectionArgs
     * @param groupBy
     *         GroupBy-Clause
     *
     * @throws IOException
     *         Wenn ein Fehler auftritt
     */
    public void doExport(@NonNull AWAbstractDBDefinition tbd, int[] fromResIDs, String selection,
                         String[] selectionArgs, String groupBy) throws IOException {
        if (fromResIDs == null) {
            fromResIDs = tbd.getResIDs();
        }
        if (groupBy != null) {
            if (selection == null) {
                selection = " 1 = 1 ";
            }
            selection += " GROUP BY " + groupBy;
        }
        String[] projection = tbd.columnNames(fromResIDs);
        Cursor c = AWApplication.getContext().getContentResolver()
                .query(tbd.getUri(), projection, selection, selectionArgs, null);
        doExport(tbd, c, fromResIDs);
    }
}
