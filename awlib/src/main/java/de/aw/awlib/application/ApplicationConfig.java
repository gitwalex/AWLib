package de.aw.awlib.application;

import android.content.Context;
import android.support.annotation.CallSuper;

import java.io.File;

import de.aw.awlib.database.AbstractDBHelper;

import static de.aw.awlib.application.AWApplication.getApplicationBackupPath;
import static de.aw.awlib.application.AWApplication.getApplicationExportPath;
import static de.aw.awlib.application.AWApplication.getApplicationImportPath;

/**
 * Konfigurationsklasse fuer AWLib-Applications.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ApplicationConfig {
    private final String theMainpath;

    public ApplicationConfig(String AWLibDatapath) {
        theMainpath = AWLibDatapath;
    }

    @CallSuper
    protected void createFiles() {
        File folder = new File(getApplicationDataPath());
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
     * /assets/html erwartet.
     * <p>
     * Default: Anzeige kein About
     */
    public String getAboutHTML() {
        return "no_about.html";
    }

    public final String getApplicationDataPath() {
        return getApplicationPath() + "/database/";
    }

    public String getApplicationDatabaseFilename() {
        return getApplicationDataPath() + "/" + theDatenbankname();
    }

    public final String getApplicationPath() {
        return theMainpath + theApplicationDirectory();
    }

    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'Copyright'. Das file wird in
     * /assets/html erwartet.
     * <p>
     * Default: Anzeige kein Copyright
     */
    public String getCopyrightHTML() {
        return "no_copyright.html";
    }

    public abstract AbstractDBHelper getDBHelper();

    /**
     * @return Das DebugFlag der Application
     * <p>
     * Default: True, immer im Debugmodus
     */
    public boolean getDebugFlag() {
        return true;
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
     * @return Datenbankname
     * <p>
     * Default: "database.db"
     */
    public String theDatenbankname() {
        return "database.db";
    }
}
