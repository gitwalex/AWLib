package de.aw.awlib.csvimportexport;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
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

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static de.aw.awlib.AWResultCodes.RESULT_Divers;
import static de.aw.awlib.AWResultCodes.RESULT_FILE_ERROR;
import static de.aw.awlib.AWResultCodes.RESULT_FILE_NOTFOUND;
import static de.aw.awlib.AWResultCodes.RESULT_OK;

/**
 * Liest ein File als CSV-File. .
 */
public class CSVFileReaderHelper {
    private final List<String[]> myEntries = new ArrayList<>();
    private int result;

    /**
     * Liest ein CSV-File. Die erste Zeile wird ueberlesen. Das Ergebnis kann durch {@link
     * CSVFileReaderHelper#getResult()} ermittelt werden.
     * <p>
     * Die Ergebnisliste erhaelt man durch {@link CSVFileReaderHelper#getResultList()}
     *
     * @param file
     *         Importfile
     */
    public CSVFileReaderHelper(File file) {
        result = RESULT_OK;
        CSVReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader ir = new InputStreamReader(fis, Charset.forName("ISO-8859-1"));
            reader = new CSVReader(ir, ';', CSVWriter.NO_QUOTE_CHARACTER);
            // Erste zeile ueberlesen
            reader.readNext();
            //
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String[] cv = new String[nextLine.length];
                for (int i = 0; i < nextLine.length; i++) {
                    String value = nextLine[i].trim();
                    cv[i] = value;
                }
                myEntries.add(cv);
            }
        } catch (FileNotFoundException e) {
            result = RESULT_FILE_NOTFOUND;
        } catch (IOException e1) {
            result = RESULT_FILE_ERROR;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                result = RESULT_Divers;
            }
        }
    }

    /**
     * @return ResultCode des Imports:
     * <p>
     * RESULT_OK Alles OK
     * <p>
     * RESULT_FILE_ERROR Fele beim Lesen
     * <p>
     * RESULT_FILE_NOTFOUND File not found
     * <p>
     * RESULT_Divers sonstiger Fehler
     */
    public int getResult() {
        return result;
    }

    /**
     * @return Liste der Importierten Werte.
     */
    public List<String[]> getResultList() {
        return myEntries;
    }
}
