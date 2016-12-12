package de.aw.awlib.application;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import java.io.File;

import de.aw.awlib.activities.AWMainActivity;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.database_private.AWDBDefinition;

/**
 * Konfigurationsklasse fuer AWLib-Applications.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ApplicationConfig {
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
    private final String APPLICATIONPATH;
    private final Context mContext;
    private AbstractDBHelper mDBHelper;
    private AWMainActivity startActivityClass;

    public ApplicationConfig(Context context) {
        mContext = context.getApplicationContext();
        APPLICATIONPATH = AWApplication.DE_AW_APPLICATIONPATH + "/" + theApplicationDirectory();
        AWAbstractDBDefinition[] tbds = getDBDefinitionValues();
        if (tbds.length > 0) {
            tbds[0].setAuthority(getAuthority());
        }
        AWDBDefinition.values()[0].setAuthority(getAuthority());
    }

    @NonNull
    protected abstract AbstractDBHelper createDBHelper(Context context);

    @CallSuper
    public void createFiles() {
        File folder = new File(getApplicationPath());
        if (!folder.exists()) {
            folder.mkdir();
        }
        folder = new File(getApplicationDataPath());
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

    public final String getApplicationBackupPath() {
        return APPLICATIONPATH + BACKUPPATH;
    }

    public final String getApplicationDataPath() {
        return APPLICATIONPATH + "/database";
    }

    public final String getApplicationDatabaseAbsoluteFilename() {
        return getApplicationDataPath() + "/" + theDatenbankname();
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

    public abstract String getAuthority();

    public Context getContext() {
        return mContext;
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

    protected abstract AWAbstractDBDefinition[] getDBDefinitionValues();

    public AbstractDBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = createDBHelper(mContext);
        }
        return mDBHelper;
    }

    /**
     * @return Das DebugFlag der Application
     * <p>
     * Default: True, immer im Debugmodus
     */
    public boolean getDebugFlag() {
        return true;
    }

    public abstract Class<? extends AWMainActivity> getStartActivityClass();

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
