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
package de.aw.awlib.events;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.aw.awlib.AWLIbApplication;
import de.aw.awlib.AWLibNotification;
import de.aw.awlib.AWLibResultCodes;
import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.database.AbstractDBConvert;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * Klasse fuer Sicheren/Restoren DB
 */
public class EventDBRestore extends AsyncTask<String, Void, Integer>
        implements AWLibResultCodes, AWLibInterface {
    private static final String BACKUPPATH = AWLIbApplication.getApplicationBackupPath() + "/";
    private static final String DATABASEPATH = AWLIbApplication.getDatenbankFilename();
    private final Context mContext;
    private final int BUFFERSIZE = 8192;
    private final SharedPreferences prefs;
    private String backupFileName;
    private boolean fromServiceCalled = false;
    private boolean isRunning;

    public EventDBRestore(Service service) {
        this(service.getApplicationContext());
        fromServiceCalled = true;
    }

    public EventDBRestore(Context context) {
        mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Fuegt die Files dem Zip-Archiv hinzu.
     *
     * @param zos
     *         ZipOutputStraem
     * @param fileToZip
     *         File, welches gezipt werden soll
     * @param parrentDirectoryName
     *         Name des ParentDirectories. Wenn null, wird dies als Wurzel betrachtet
     *
     * @throws IOException
     *         Bei Fehlern
     */
    private void addDirToZipArchive(ZipOutputStream zos, File fileToZip,
                                    String parrentDirectoryName) throws IOException {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }
        String zipEntryName = fileToZip.getName();
        if (parrentDirectoryName != null && !parrentDirectoryName.isEmpty()) {
            zipEntryName = parrentDirectoryName + "/" + fileToZip.getName();
        }
        if (fileToZip.isDirectory()) {
            System.out.println("+" + zipEntryName);
            for (File file : fileToZip.listFiles()) {
                addDirToZipArchive(zos, file, zipEntryName);
            }
        } else {
            System.out.println("   " + zipEntryName);
            byte[] buffer = new byte[BUFFERSIZE];
            FileInputStream fis = new FileInputStream(fileToZip);
            zos.putNextEntry(new ZipEntry(zipEntryName));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    /**
     * Fuegt die Files dem Zip-Archiv hinzu.
     *
     * @param zos
     *         ZipOutputStraem
     * @param fileToZip
     *         File, welches gezipt werden soll
     *
     * @throws IOException
     *         Bei Fehlern
     */
    private void addFileToZipArchive(ZipOutputStream zos, File fileToZip) throws IOException {
        if (fileToZip == null || !fileToZip.exists()) {
            return;
        }
        String zipEntryName = fileToZip.getName();
        System.out.println("   " + zipEntryName);
        byte[] buffer = new byte[BUFFERSIZE];
        FileInputStream fis = new FileInputStream(fileToZip);
        zos.putNextEntry(new ZipEntry(zipEntryName));
        int length;
        while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

    @Override
    protected Integer doInBackground(String... params) {
        return null;
    }

    /**
     * Utility method to read data from InputStream
     */
    private void extractEntry(final ZipEntry zipEntry, InputStream is) throws IOException {
        String extractTo;
        if (AWLIbApplication.getDebug()) {
            extractTo = AWLIbApplication.getDatenbankFilename();
        } else {
            extractTo =
                    mContext.getDatabasePath(AWLIbApplication.getDatenbankname()).getAbsolutePath();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(extractTo);
            final byte[] buf = new byte[BUFFERSIZE];
            int length;
            while ((length = is.read(buf, 0, buf.length)) >= 0) {
                fos.write(buf, 0, length);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public String getFileName() {
        return backupFileName;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int restore(String inputFilename) {
        int result = RESULT_Divers;
        ZipInputStream is = null;
        BufferedInputStream bis;
        isRunning = true;
        AbstractDBHelper.getInstance().close();
        try {
            bis = new BufferedInputStream(new FileInputStream(inputFilename));
            is = new ZipInputStream(bis);
            ZipEntry entry;
            while ((entry = is.getNextEntry()) != null) {
                extractEntry(entry, is);
            }
            result = RESULT_OK;
        } catch (FileNotFoundException e) {
            result = RESULT_FILE_NOTFOUND;
        } catch (IOException e) {
            result = RESULT_FILE_ERROR;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
            }
        }
        AbstractDBHelper.getInstance();
        if (result == RESULT_OK) {
            AWLIbApplication.onRestoreDB();
        }
        isRunning = false;
        return result;
    }

    /**
     * Sichert die Datenbank.
     *
     * @return Darum der Sicherung
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Date save() {
        Date date = new Date(System.currentTimeMillis());
        Locale locale = Locale.getDefault();
        DateFormat formatter =
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        backupFileName =
                BACKUPPATH + (formatter.format(date)).replace(".", "_").replace(":", "_") + ".zip";
        String ticker = mContext.getString(R.string.tickerDBSicherung);
        String contentTitle = mContext.getString(R.string.contentTextDBSicherung);
        AWLibNotification mNotification = new AWLibNotification(mContext, contentTitle);
        mNotification.setTicker(ticker);
        mNotification.setHasProgressBar(true);
        mNotification.createNotification(contentTitle);
        int ergebnis = RESULT_OK;
        FileOutputStream fout;
        ZipOutputStream zout = null;
        try {
            File folder = new File(DATABASEPATH);
            File bakFile = new File(backupFileName);
            bakFile.delete();
            fout = new FileOutputStream(bakFile);
            zout = new ZipOutputStream(new BufferedOutputStream(fout));
            addFileToZipArchive(zout, folder);
        } catch (IOException e) {
            ergebnis = RESULT_FILE_ERROR;
        } catch (Exception e) {
            ergebnis = RESULT_Divers;
        } finally {
            try {
                if (zout != null) {
                    zout.close();
                }
            } catch (IOException e) {
                ergebnis = RESULT_FILE_ERROR;
            }
        }
        String result;
        switch (ergebnis) {
            case RESULT_OK:
                result = mContext.getString(R.string.dbSaved);
                SharedPreferences.Editor editor = prefs.edit();
                String mDate = AbstractDBConvert.convertDate(date);
                editor.putString(AWLibEvent.DoDatabaseSave.name(), mDate).apply();
                setDBSaveSummary(mDate);
                break;
            case RESULT_FILE_ERROR:
                result = mContext.getString(R.string.dbFileError);
                break;
            case RESULT_Divers:
            default:
                result = mContext.getString(R.string.dbSaveError);
                break;
        }
        mNotification.setHasProgressBar(false);
        mNotification.replaceNotification(result);
        return date;
    }

    /**
     * Liest aus den Preferences mit Key  {@link AWLibInterface.MainAction#doSave#name()} das letzte
     * Sicherungsdatum und stellt dieses in die Summary ein.
     */
    private void setDBSaveSummary(String saveDate) {
        int key = R.string.pkDBSave;
        final SharedPreferences.Editor doDBSave = prefs.edit();
        final StringBuilder sb =
                new StringBuilder(mContext.getString(R.string.lastSave)).append(": ");
        if (saveDate == null) {
            sb.append(mContext.getString(R.string.na));
        } else {
            sb.append(saveDate);
        }
        doDBSave.putString(mContext.getString(key), sb.toString()).apply();
    }
}
