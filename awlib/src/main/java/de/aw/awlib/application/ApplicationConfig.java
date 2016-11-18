package de.aw.awlib.application;

/**
 * Konfigurationsklasse fuer AWLib-Applications.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ApplicationConfig {
    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'About'. Das file wird in
     * /assets/html erwartet.
     */
    public abstract String getAboutHTML();

    /**
     * @return Liefert ein HTML-File  fuer die Auswahl der Preferences 'Copyright'. Das file wird in
     * /assets/html erwartet.
     */
    public abstract String getCopyrightHTML();

    /**
     * @return Das DebugFlag der Application
     */
    public abstract boolean getDebugFlag();

    /**
     * Wird gerufen, wenn die Datenbank restored wurde
     */
    public abstract void onRestoreDatabase();

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
     */
    public abstract String theDatenbankname();
}
