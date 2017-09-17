package de.aw.awlib.application;

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

import android.app.Application;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
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

import de.aw.awlib.activities.AWActivityDebug;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.events.AWEventService;

import static de.aw.awlib.activities.AWInterface.linefeed;

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
    /**
     * Pfad, indem alle Backups zu de.aw.-Applications abgelegt werden
     */
    private static final String PICTUREPATH = "/pictures";
    /**
     * Pfad, indem alle Backups zu de.aw.-Applications abgelegt werden
     */
    private static final String BACKUPPATH = "/backup";
    /**
     * Pfad, indem alle Exports zu de.aw.-Applications abgelegt werden
     */
    private static final String EXPORTPATH = "/export";
    /**
     * Pfad, indem alle Imports zu de.aw.-Applications abgelegt werden
     */
    private static final String IMPORTPATH = "/import";
    private static final String DATABASEPATH = "/database";
    private String APPLICATIONPATH;
    private AbstractDBHelper mDBHelper;

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
    public static void LogError(String message) {
        File logFile = new File(DE_AW_APPLICATIONPATH + "/LOG.txt");
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

    @NonNull
    protected abstract AbstractDBHelper createDBHelper(Context context);

    @CallSuper
    public void createFiles() {
        File folder = new File(getApplicationPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationDatabasePath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationBackupPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationExportPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationImportPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'About'. Das file wird in
     * /assets/html erwartet. Default: Anzeige kein About
     */
    public String getAboutHTML() {
        return "no_about.html";
    }

    public final String getApplicationBackupPath() {
        return APPLICATIONPATH + BACKUPPATH;
    }

    public final String getApplicationDatabaseAbsoluteFilename() {
        return getDatabasePath(theDatenbankname()).getAbsolutePath();
    }

    public String getApplicationDatabasePath() {
        return APPLICATIONPATH + DATABASEPATH;
    }

    public final String getApplicationExportPath() {
        return APPLICATIONPATH + EXPORTPATH;
    }

    public final String getApplicationImportPath() {
        return APPLICATIONPATH + IMPORTPATH;
    }

    public final String getApplicationPath() {
        return APPLICATIONPATH;
    }

    public final String getApplicationPicturePath() {
        return APPLICATIONPATH + PICTUREPATH;
    }

    public abstract String getAuthority();

    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'Copyright'. Das file wird in
     * /assets/html erwartet. Default: Anzeige kein Copyright
     */
    public String getCopyrightHTML() {
        return "no_copyright.html";
    }

    protected abstract AWAbstractDBDefinition[] getDBDefinitionValues();

    public AbstractDBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = createDBHelper(this);
        }
        return mDBHelper;
    }

    /**
     * @return Das DebugFlag der Application Default: True, immer im Debugmodus
     */
    public boolean getDebugFlag() {
        return true;
    }

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
        APPLICATIONPATH = AWApplication.DE_AW_APPLICATIONPATH + "/" +
                theApplicationDirectory().replace("/", "");
        AWAbstractDBDefinition[] tbds = getDBDefinitionValues();
        if (tbds.length > 0) {
            tbds[0].setAuthority(getAuthority());
        }
        AbstractDBHelper.AWDBDefinition.values()[0].setAuthority(getAuthority());
        createDBHelper(this);
        boolean mDebugFlag = getDebugFlag();
        super.onCreate();
        AWEventService.setDailyAlarm(this);
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
                                .detectLeakedRegistrationObjects()
                                .detectLeakedSqlLiteObjects().penaltyLog()
                                .build());
                // Schreiben/Lesen auf Disk erlauben
                StrictMode.allowThreadDiskReads();
                StrictMode.allowThreadDiskWrites();
                // Replace System.err with one that'll monitor for StrictMode
                // killing us and perform a hprof heap dump just before it does.
                System.setErr(new PrintStreamThatDumpsHprofWhenStrictModeKillsUs(System.err));
            } catch (Exception e) {
                // ignore errors
            }
        }
        FragmentManager.enableDebugLogging(EnableFragmentManagerLogging);
        LoaderManager.enableDebugLogging(EnableLoaderManagerLogging);
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
     * Wird gerufen, wenn die Datenbank restored wurde
     */
    public void onRestoreDatabase(Context context) {
    }

    /**
     * @return Verzeichnis, in dem die Appplicationsdaten abgelegt werden sollen
     */
    public abstract String theApplicationDirectory();

    /**
     * @return Datenbankversion
     */
    public abstract int theDatenbankVersion();

    /**
     * @return Datenbankname Default: "database.db"
     */
    public String theDatenbankname() {
        return "database.db";
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
