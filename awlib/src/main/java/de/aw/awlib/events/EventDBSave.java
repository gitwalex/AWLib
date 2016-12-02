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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import de.aw.awlib.AWNotification;
import de.aw.awlib.AWResultCodes;
import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.utils.AWUtils;

/**
 * Klasse fuer Sicheren/Restoren DB
 */
public class EventDBSave extends BroadcastReceiver implements AWResultCodes, AWInterface {
    private static final String ALARMTIME = "ALARMTIME";
    private static final int ALARM_TYPE = AlarmManager.RTC_WAKEUP;
    private static final String BACKUPPATH =
            AWApplication.getApplicationConfig().getApplicationBackupPath() + "/";
    private static final String DATABASEFILENAME =
            AWApplication.getApplicationConfig().getApplicationDatabaseFilename();
    private final Context mContext;
    private final SharedPreferences prefs;
    private final Date date;
    private File backupFile;
    private AWNotification mNotification;

    public EventDBSave() {
        mContext = AWApplication.getContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        date = new Date(System.currentTimeMillis());
        Locale locale = Locale.getDefault();
        DateFormat formatter =
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        backupFile = new File(
                BACKUPPATH + (formatter.format(date)).replace(".", "_").replace(":", "_") + ".zip");
    }

    private static void cancelDBSaveAlarm(Context context, SharedPreferences prefs) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AWEventService.class);
        intent.setAction(AWLIBACTION);
        PendingIntent alarmIntent =
                PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.cancel(alarmIntent);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(context.getString(R.string.nextDoDBSave)).apply();
    }

    /**
     * Prueft, ob die automatische Sicherung der DB in den Preferences gewuenscht ist, dann ggfs.
     * Alarm setzen. Ansonsten einen ggfs. vorhandenen Alarm cancel.
     *
     * @param context
     *         Context
     * @param prefs
     *         SharedPreferences
     */
    public static void checkDBSaveAlarm(Context context, SharedPreferences prefs) {
        if (prefs.getBoolean(context.getString(R.string.pkSavePeriodic), false)) {
            setDBSaveAlarm(context, prefs);
        } else {
            cancelDBSaveAlarm(context, prefs);
        }
    }

    /**
     * Setzt den Termin fuer die naechste Automatische Datenbanksicherung,
     *
     * @param context
     *         Context
     * @param prefs
     *         SharedPreferences
     */
    private static void setDBSaveAlarm(Context context, SharedPreferences prefs) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.DAY_OF_MONTH, 5);
        SharedPreferences.Editor editor = prefs.edit();
        long nextDBSave = cal.getTimeInMillis();
        editor.putLong(context.getString(R.string.nextDoDBSave), nextDBSave).apply();
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, AWEventService.class);
        newIntent.setAction(AWLIBEVENT);
        newIntent.putExtra(AWLIBEVENT, (Parcelable) AWEvent.DoDatabaseSave);
        newIntent.putExtra(ALARMTIME, nextDBSave);
        PendingIntent newAlarmIntent =
                PendingIntent.getService(context, 0, newIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mAlarmManager.set(ALARM_TYPE, nextDBSave, newAlarmIntent);
    }

    public File execute() throws ExecutionException, InterruptedException {
        new EventDBSaveExecute().execute(backupFile).get();
        return getFileName();
    }

    public File getFileName() {
        return backupFile;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        checkDBSaveAlarm(context, prefs);
    }

    public void save() {
        new EventDBSaveExecute().execute(backupFile);
    }

    private class EventDBSaveExecute extends AsyncTask<File, Void, Integer> {
        @Override
        protected Integer doInBackground(File... params) {
            return AWUtils.addToZipArchive(params[0], DATABASEFILENAME);
        }

        @Override
        protected void onPostExecute(Integer ergebnis) {
            String result;
            switch (ergebnis) {
                case RESULT_OK:
                    result = mContext.getString(R.string.dbSaved);
                    SharedPreferences.Editor editor = prefs.edit();
                    String mDate = AWDBConvert.convertDate(date);
                    editor.putString(AWEvent.DoDatabaseSave.name(), mDate).apply();
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
            mNotification = new AWNotification(mContext, contentTitle);
            mNotification.setTicker(ticker);
            mNotification.setHasProgressBar(true);
            mNotification.createNotification(contentTitle);
        }
    }
}
