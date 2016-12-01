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
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileNotFoundException;
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

import de.aw.awlib.AWResultCodes;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.database.AWDBFormate;

import static de.aw.awlib.AWResultCodes.RESULT_OK;

/**
 * Erstellt einen Export der Daten eines Cursors. Aufbau wie folgt:
 * <p/>
 * 1. Zeile Spaltennamen
 * <p>
 * 2.- (n-1) Zeile: Inhalte der Spalten
 * <p>
 * n. Tabellenname
 */
public class AWCSVExporter {
    private static final NumberFormat nf;

    static {
        nf = NumberFormat.getInstance(Locale.getDefault());
        nf.setGroupingUsed(false);
    }

    private final ExportCursorListener mExportCursorListener;
    private String fullFilename;

    public AWCSVExporter(@NonNull ExportCursorListener listener) {
        mExportCursorListener = listener;
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
     */
    public void doExport(@NonNull AWAbstractDBDefinition tbd, int[] fromResIDs, String selection,
                         String[] selectionArgs, String groupBy) {
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
        doExport(tbd, c);
    }

    /**
     * Exportiert Inhalte eines Cursors.
     *
     * @param tbd
     *         Tabelle, die gelesen wird. Name wird ans Ende des Exports angehaengt
     * @param c
     *         Cursor, dessen Inhalte exportiert werden sollen
     */
    public void doExport(AWAbstractDBDefinition tbd, Cursor c) {
        new ExportCursor(tbd, c);
    }

    public String getFilename() {
        return fullFilename;
    }

    /**
     * Interface fuer Listener auf Ergebnisse.
     */
    public interface ExportCursorListener {
        /**
         * Wird nach Ende des Exports gerufen
         *
         * @param result
         *         ReultCode gemaess {@link AWResultCodes}
         */
        void onFinishExport(int result);

        /**
         * Wird vor dem Start des Exports gerufen
         */
        void onStartExport();
    }

    private class ExportCursor extends AsyncTask<Void, Void, Integer> {
        private final AWAbstractDBDefinition tbd;
        private final Cursor c;
        private final String filename;

        /**
         * Exportiert Inhalte eines Cursors.
         *
         * @param tbd
         *         Tabelle, die gelesen wird. Name wird ans Ende des Exports angehaengt
         * @param c
         *         Cursor, dessen Inhalte exportiert werden sollen
         */
        public ExportCursor(AWAbstractDBDefinition tbd, Cursor c) {
            this.tbd = tbd;
            this.c = c;
            Date date = new Date(System.currentTimeMillis());
            Locale locale = Locale.getDefault();
            DateFormat formatter =
                    (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale));
            String dateString = formatter.format(date).replace(".", "_");
            this.filename = ((dateString) + ".csv").replace(":", "");
            execute();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            AWDBFormate mDBFormat = AWDBFormate.getInstance();
            int[] fromResIDs = new int[c.getColumnCount()];
            for (int i = 0; i < c.getColumnCount(); i++) {
                String columnname = c.getColumnName(i);
                fromResIDs[i] = mDBFormat.getResID(columnname);
            }
            List<String[]> list = new ArrayList<>();
            String filename = AWApplication.getApplicationExportPath() + "/" + this.filename;
            File file = new File(filename);
            fullFilename = file.getAbsolutePath();
            FileOutputStream fos;
            CSVWriter bw = null;
            int result = RESULT_OK;
            try {
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
            } catch (FileNotFoundException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
                result = AWResultCodes.RESULT_FILE_ERROR;
            } finally {
                if (!c.isClosed()) {
                    c.close();
                }
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        //TODO Execption bearbeiten
                        e.printStackTrace();
                        result = AWResultCodes.RESULT_FILE_ERROR;
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mExportCursorListener.onFinishExport(result);
        }

        @Override
        protected void onPreExecute() {
            mExportCursorListener.onStartExport();
        }
    }
}
