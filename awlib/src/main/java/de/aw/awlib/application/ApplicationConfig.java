package de.aw.awlib.application;

/**
 * Konfigurationsklasse fuer AWLib-Applications.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ApplicationConfig {
    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'About'. Das file wird in
     * /assets/html erwartet.
     * <p>
     * Default: Anzeige kein About
     */
    public String getAboutHTML() {
        return "no_about.html";
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
    public void onRestoreDatabase() {
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
     * Default: ApplicationDirectory + '.db'
     */
    public String theDatenbankname() {
        return theApplicationDirectory().replaceAll("/", "") + ".db";
    }
}
