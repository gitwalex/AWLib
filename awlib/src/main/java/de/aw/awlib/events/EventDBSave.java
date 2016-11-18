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

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import de.aw.awlib.AWLibNotification;
import de.aw.awlib.AWLibResultCodes;
import de.aw.awlib.AWLibUtils;
import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.application.AWLIbApplication;
import de.aw.awlib.database.AbstractDBConvert;

/**
 * Klasse fuer Sicheren/Restoren DB
 */
public class EventDBSave extends AsyncTask<Void, Void, Integer>
        implements AWLibResultCodes, AWLibInterface {
    private static final String BACKUPPATH = AWLIbApplication.getApplicationBackupPath() + "/";
    private static final String DATABASEPATH = AWLIbApplication.getDatenbankFilename();
    private final Context mContext;
    private final SharedPreferences prefs;
    private final Date date;
    private String backupFileName;
    private boolean fromServiceCalled = false;
    private AWLibNotification mNotification;

    public EventDBSave(Service service) {
        this(service.getApplicationContext());
        fromServiceCalled = true;
    }

    public EventDBSave(Context context) {
        mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        date = new Date(System.currentTimeMillis());
        Locale locale = Locale.getDefault();
        DateFormat formatter =
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        backupFileName =
                BACKUPPATH + (formatter.format(date)).replace(".", "_").replace(":", "_") + ".zip";
    }

    @Override
    protected Integer doInBackground(Void... params) {
        File bakFile = new File(backupFileName);
        return AWLibUtils.addToZipArchive(bakFile, DATABASEPATH);
    }

    public String getFileName() {
        return backupFileName;
    }

    @Override
    protected void onPostExecute(Integer ergebnis) {
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
    }

    @Override
    protected void onPreExecute() {
        String ticker = mContext.getString(R.string.tickerDBSicherung);
        String contentTitle = mContext.getString(R.string.contentTextDBSicherung);
        mNotification = new AWLibNotification(mContext, contentTitle);
        mNotification.setTicker(ticker);
        mNotification.setHasProgressBar(true);
        mNotification.createNotification(contentTitle);
    }

    public Date save() {
        try {
            execute().get();
        } catch (InterruptedException e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        } catch (ExecutionException e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Liest aus den Preferences mit Key  {@link MainAction#doSave#name()} das letzte
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
