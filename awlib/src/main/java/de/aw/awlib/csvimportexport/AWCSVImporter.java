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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.aw.awlib.AWResultCodes;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.application.ApplicationConfig;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AWDBAlterHelper;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * Converter von CSV-Files. Liest eine Datei und fuehrt folgende Pruefungen durch: 1. Zeile Erwartet
 * werden die Spaltennamen.
 * <p/>
 * 2. - n. Zeile Werte zu den Spalten
 * <p/>
 * letzte Zeile: Tabellenname, in die die Werte importiert werden sollen Prufungen: Spaltennamen
 * entsprechen grds. auch moeglichen Spalten in der Tabelle - Spaltenwerte etnsprechen vom Format
 * her dem entsprechenden Spaltennamen - Spalten sind in der Zieltabelle alle vorhanden
 * <p/>
 * Werte, die obigen Grundsaetzen entprechen, werden in myEntries gespeichert.
 */
public class AWCSVImporter implements AWResultCodes {
    private final NumberFormat nf = DecimalFormat.getInstance(Locale.getDefault());
    private final ApplicationConfig mAppconfig;
    /**
     * Spaltennamen des Importfiles
     */
    private String[] columns;
    private DateFormat df = DateFormat.getDateInstance();
    /**
     * Zeilen, die importiert werden koennen
     */
    private List<ContentValues> myEntries = new ArrayList<>();

    public AWCSVImporter(Context context) {
        mAppconfig = ((AWApplication) context.getApplicationContext()).getApplicationConfig();
    }

    private String convert(AWAbstractDBDefinition tbd, int resID, String value)
            throws ParseException {
        Long amount;
        if (value != null) {
            char format = mAppconfig.getDBHelper().getFormat(resID);
            switch (format) {
                case 'D':// Datum
                    Date date;
                    try {
                        date = df.parse(value);
                        value = AWDBConvert.convertDate2SQLiteDate(date);
                    } catch (ParseException e) {
                        throw new DateParseException("Datumsfehler", value);
                    }
                    break;
                case 'M':// Number
                case 'N':// Number
                    try {
                        amount = (long) (nf.parse(value).doubleValue() * AWDBConvert.mNumberDigits);
                        value = amount.toString();
                    } catch (ParseException e) {
                        throw new NumberParseException("Nummerfehler", value);
                    }
                    break;
                case 'K':// Number
                case 'C':// Currency
                    try {
                        amount = (long) (nf.parse(value)
                                .doubleValue() * AWDBConvert.mCurrencyDigits);
                        value = amount.toString();
                    } catch (ParseException e) {
                        throw new CurrencyParseException("Currencyfehler", value);
                    }
                    break;
                case 'B':// Boolean
                    int bool = Integer.parseInt(value);
                    if (bool != 0 && bool != 1) {
                        throw new BooleanParseException("Kein Boolean-Wert", value);
                    }
                    break;
                case 'P':// Percent
                    break;
                case 'I':// Text
                    // keine Aenderung
                    break;
                case 'T':// Text
                    // keine Aenderung
                    break;
                default:
                    throw new IllegalArgumentException("Format " + format + " nicht bekannt!");
            }
        }
        return value;
    }

    public int execute(String filename) {
        AbstractDBHelper db = null;
        int result = RESULT_OK;
        CSVReader reader = null;
        try {
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader ir = new InputStreamReader(fis, Charset.forName("ISO-8859-1"));
            reader = new CSVReader(ir, ';', CSVWriter.NO_QUOTE_CHARACTER);
            String[] nextLine;
            columns = reader.readNext();
            Integer[] columnsResIDs = new Integer[columns.length];
            for (int i = 0; i < columns.length; i++) {
                columnsResIDs[i] = db.getResID(columns[i]);
            }
            while ((nextLine = reader.readNext()) != null) {
                ContentValues cv = new ContentValues();
                for (int i = 0; i < nextLine.length; i++) {
                    String value = nextLine[i].trim();
                    cv.put(columns[i], value);
                }
                myEntries.add(cv);
            }
            if (myEntries.size() > 0) {
                ContentValues last = myEntries.get(myEntries.size() - 1);
                String tablename = last.getAsString(columns[0]);
                AWAbstractDBDefinition tbd = db.getDBDefinition(tablename);
                try {
                    for (int resID : columnsResIDs) {
                        AbstractDBHelper.getInstance().columnName(resID);
                    }
                    myEntries.remove(myEntries.size() - 1);
                } catch (IllegalArgumentException e1) {
                    result = RESULT_FEHLER_TBD;
                } catch (AWAbstractDBDefinition.ResIDNotFoundException e1) {
                    result = RESULT_SPALTE_IN_TBD_NICHT_VORHANDEN;
                }
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
        if (result == RESULT_OK) {
            SQLiteDatabase database = db.getWritableDatabase();
            AWDBAlterHelper helper = new AWDBAlterHelper(db, database);
            String tempTable = "ImportTable";
            helper.createTable(tempTable, columns);
            database.beginTransaction();
            try {
                for (ContentValues entries : myEntries) {
                    database.insert(tempTable, null, entries);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
        return result;
    }

    private class BooleanParseException extends ParseException {
        public BooleanParseException(String s, String value) {
            super(s + " bei " + value, 0);
        }
    }

    private class CurrencyParseException extends ParseException {
        public CurrencyParseException(String s, String value) {
            super(s + " bei " + value, 0);
        }
    }

    private class DateParseException extends ParseException {
        public DateParseException(String s, String value) {
            super(s + " bei " + value, 0);
        }
    }

    private class NumberParseException extends ParseException {
        public NumberParseException(String s, String value) {
            super(s + " bei " + value, 0);
        }
    }
}