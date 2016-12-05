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
package de.aw.awlib.application;

import android.app.Application;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Debug;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ViewConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.Date;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWActivityDebug;

import static de.aw.awlib.activities.AWInterface.linefeed;
import static de.aw.awlib.events.EventDBSave.checkDBSaveAlarm;

/**
 * AWApplication: Einschalten von StrictModus, wenn im debug-mode. Erstellt eine HProf-Log bei
 * penaltyDeath().
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class AWApplication extends Application {
    /**
     * Debugging Cursor einschalten
     */
    public static final boolean EnableCursorLogging = true;
    /**
     * true: Debugging FragmentManager einschalten
     */
    public static final boolean EnableFragmentManagerLogging = true;
    /**
     * true: Debugging LoaderManager einschalten
     */
    public static final boolean EnableLoaderManagerLogging = false;
    public static final String STACKTRACE = "STACKTRACE", TAG = "de.aw";
    /**
     * Pfad, indem alle de.aw.-Applications abgelegt werden
     */
    public static final String DE_AW_APPLICATIONPATH =
            Environment.getExternalStorageDirectory() + "/de.aw";
    private static final String STACKTRACEPATH = "/stackTrace.txt";
    private String APPLICATIONPATH;
    private ApplicationConfig mApplicationConfig;

    /**
     * Loggt Warnungen
     *
     * @param message
     *         message
     */
    public static void Log(String message) {
        Log.d(AWApplication.TAG, message);
    }

    /**
     * Loggt Fehler. Die Meldung wird auch in das File Applicationpath/LOG.txt geschrieben
     *
     * @param message
     *         Fehlermeldung
     */
    public static void LogError(Context context, String message) {
        File logFile = new File(
                ((AWApplication) context.getApplicationContext()).APPLICATIONPATH + "/LOG.txt");
        try {
            FileOutputStream fileout = new FileOutputStream(logFile, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            CharSequence date = DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date());
            outputWriter.write(date + ": " + message);
            outputWriter.write(linefeed);
            outputWriter.close();
        } catch (IOException e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        }
        Log.e(AWApplication.TAG, message);
    }

    public ApplicationConfig getApplicationConfig() {
        return mApplicationConfig;
    }

    protected abstract ApplicationConfig getApplicationConfig(String theMainPath);

    private void handleUncaughtException(Throwable e) throws IOException {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        String stackTrace = Log.getStackTraceString(e);
        File stackTraceFile = new File(APPLICATIONPATH + STACKTRACEPATH);
        FileOutputStream fileout = new FileOutputStream(stackTraceFile);
        OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
        CharSequence date = DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date());
        outputWriter.write(date + ": " + stackTrace);
        outputWriter.close();
        String exceptionAsString = stackTrace + e.getMessage();
        Intent intent = new Intent(this, AWActivityDebug.class);
        intent.putExtra(STACKTRACE, exceptionAsString);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity(intent);
    }

    @Override
    public void onCreate() {
        mApplicationConfig = getApplicationConfig(DE_AW_APPLICATIONPATH);
        APPLICATIONPATH = mApplicationConfig.getApplicationPath();
        File folder = new File(DE_AW_APPLICATIONPATH);
        if (!folder.exists()) {
            folder.mkdir();
        }
        boolean mDebugFlag = mApplicationConfig.getDebugFlag();
        folder = new File(APPLICATIONPATH);
        if (!folder.exists()) {
            folder.mkdir();
        }
        mApplicationConfig.createFiles();
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.awlib_preferences_allgemein, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkDBSaveAlarm(this, prefs);
        if (mDebugFlag) {
            try {
                // Im Debug-Mode Pruefen lassen, welche Constraints verletzt werden.
                // Hier nur logging
                StrictMode.setThreadPolicy(
                        new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
                //
                // Achtung: .detectActivityLeaks() wurde bewusst abgeschaltet
                // und muss zumdeindest zeitweise mal gestestet werden.
                // Hintergund:
                //http://stackoverflow.com/questions/21145261/strictmode-activity-instance-count
                // -violation-2-instances-1-expected-on-rotati/25252425#25252425
                StrictMode.setVmPolicy(
                        new StrictMode.VmPolicy.Builder().detectLeakedClosableObjects()
                                .detectLeakedRegistrationObjects().detectLeakedSqlLiteObjects()
                                .penaltyLog().build());
                // Schreiben/Lesen auf Disk erlauben
                StrictMode.allowThreadDiskReads();
                StrictMode.allowThreadDiskWrites();
                // Replace System.err with one that'll monitor for StrictMode
                // killing us and perform a hprof heap dump just before it does.
                System.setErr(new PrintStreamThatDumpsHprofWhenStrictModeKillsUs(System.err));
                FragmentManager.enableDebugLogging(EnableFragmentManagerLogging);
                LoaderManager.enableDebugLogging(EnableLoaderManagerLogging);
            } catch (Exception e) {
                // ignore errors
            }
        }
        // Bei Abbruch ein Fenster mit der Fehlermeldung
        // zeigen
        final UncaughtExceptionHandler oldExceptionHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                e.printStackTrace();
                try {
                    handleUncaughtException(e);
                } catch (IOException e1) {
                    //TODO Execption bearbeiten
                    e1.printStackTrace();
                }
                oldExceptionHandler.uncaughtException(thread, e);
            }
        });
        //-Ausschalten Device - Option - Button, wenn vorhanden.
        // If an Android device has an option button, the overflow menu is not
        // shown.
        // While it it not recommended as the user except a certain behavior
        // from his device, you can trick you device in thinking it has no
        // option button with the following code.
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Im Debug-Modus wird Strict eingeschaltet. Sollte es zu einem Fehler kommen, wird der Heap in
     * ein File gedumpt.
     */
    private class PrintStreamThatDumpsHprofWhenStrictModeKillsUs extends PrintStream {
        PrintStreamThatDumpsHprofWhenStrictModeKillsUs(OutputStream outs) {
            super(outs);
        }

        @Override
        public synchronized void println(String str) {
            super.println(str);
            if (str.startsWith("StrictMode VmPolicy violation with POLICY_DEATH;")) {
                // StrictMode is about to terminate us... do a heap dump!
                try {
                    AWApplication.Log("Logge HPROF...");
                    File file = new File(APPLICATIONPATH, "strictmode-violation.hprof");
                    super.println("Dumping HPROF to: " + file.getName());
                    Debug.dumpHprofData(file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
