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
package de.aw.awlib.gv;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;

import java.util.Date;

import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * MonMa AWApplicationGeschaeftsObjekt Vorlage fuer die Geschaeftsvorfaelle, z.B. Bankkonto-Buchung,
 * Neues Account etc. Bietet Import-Funktion, die Tabellen werden direkt aus dem Import-File
 * gefuellt.
 * <p/>
 * Fuer Sonderheiten kann die Methode doImport(int resID, String Key, String value) ueberschieben
 * werden. Diese Funktion wird immer aufgerufen, wenn ein neues Praefix beim Import gefunden wurde.
 * <p/>
 * Die erbende Klasse muss durch Aufrufe von update, delete, cancel fuer die Pflege der Datenbank
 * sorgen.
 *
 * @author alex
 */
public abstract class AWApplicationGeschaeftsObjekt extends AWApplicationGeschaeftsObjektNew {
    /**
     * Tabellendefinition, fuer die dieser AWApplicationGeschaeftsObjekt gilt. Wird im Konstruktor
     * belegt.
     */
    /**
     * Laden eines Geschaeftsvorfalls mit der id aus der Datenbank.
     *
     * @param tbd
     *         Tabelle, der der GV zugeordnet ist.
     * @param id
     *         ID des Geschaeftsvorfalls in der entsprechenden Tabelle.
     *
     * @throws LineNotFoundException
     *         Wenn keine Zeile mit der id gefunden wurde.
     * @throws android.content.res.Resources.NotFoundException
     *         Wenn kein Datensatz mit dieser ID gefunden wurde.
     */
    public AWApplicationGeschaeftsObjekt(Context context, AWAbstractDBDefinition tbd, Long id)
            throws LineNotFoundException {
        super(context.getContentResolver(), tbd, id);
    }

    public AWApplicationGeschaeftsObjekt(AWAbstractDBDefinition tbd, Cursor c) {
        super(tbd, c);
    }

    /**
     * Anlegen eines leeren Geschaeftsobjektes
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public AWApplicationGeschaeftsObjekt(AWAbstractDBDefinition tbd) {
        super(tbd);
    }

    protected AWApplicationGeschaeftsObjekt(Parcel in) {
        super(in);
    }

    /**
     * Prueft, ob ein Wert geaendert wurde oder bereits in der DB vorhanden ist.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return true, wenn ein Wert ungleich null enthalten ist
     */
    @Deprecated
    public final boolean containsKey(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.containsKey(key);
    }

    /**
     * Liefert den Wert aus dem Geschaeftsobjekt zu Spalte resID als Boolean.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte (true ooder false)
     */
    @Deprecated
    public final Boolean getAsBoolean(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsBoolean(key);
    }

    @Deprecated
    public final byte[] getAsByteArray(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsByteArray(key);
    }

    /**
     * Liest das Datum aus dem Geschaeftsobjekt und liefert es als Date zurueck. Siehe {@link
     * AWApplicationGeschaeftsObjekt#getAsDate(String)}
     */
    @Deprecated
    public Date getAsDate(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsDate(key);
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Integer.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    @Deprecated
    public final Integer getAsInt(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsInt(key);
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Integer.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    @Deprecated
    public final int getAsInt(int resID, int defaultValue) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsInt(key, defaultValue);
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Long.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    @Deprecated
    public final Long getAsLong(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsLong(key);
    }

    /**
     * Wie {@link AWApplicationGeschaeftsObjekt#getAsLong(int)}, liefert aber den Defaultwert
     * zuruck, wenn die Spalte nicht belegt iist.
     */
    @Deprecated
    public final long getAsLong(int resID, long defaultWert) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsLong(key, defaultWert);
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als String.
     *
     * @param resID
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    @Deprecated
    public final String getAsString(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        return super.getAsString(key);
    }

    /**
     * Aendert oder Fuegt Daten ein. Werden mAccountID oder buchungsID belegt, werden die
     * entsprechenden Variaablen (neu) belegt. Wird catID belegt, wird aucch catname belegt. Wird
     * catname belegt, wird geprueft, ob catID bereits belegt ist. Wenn nicht, wird wird diese dort
     * auch belegt.
     *
     * @param resID
     *         ResID der Spalte, die eingefuegt werden soll.
     * @param value
     *         Wert, der eingefuegt werden soll. Wert wird als String eingefuegt. Ist der Wert
     *         bereits identisch vorhanden (DB oder als geaenderter Wert), wird nichts eingefuegt.
     *         Ist value == null oder ein leerer String, wird ein ggfs. geaenderter Wert in der
     *         Spalte und nach Update dann auch aus der DB entfernt.
     *
     * @throws IllegalArgumentException
     *         wenn ResID nicht in der Tabelle vorhanden ist
     */
    @Deprecated
    public void put(int resID, String value) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.put(key, value);
    }

    /**
     * Aendert oder Fuegt Daten ein. Werden mAccountID oder buchungsID belegt, werden die
     * entsprechenden Variaablen (neu) belegt. Wird catID belegt, wird aucch catname belegt. Wird
     * catname belegt, wird geprueft, ob catID bereits belegt ist. Wenn nicht, wird wird diese dort
     * auch belegt.
     *
     * @param resID
     *         ResID der Spalte, die eingefuegt werden soll.
     * @param value
     *         BlobWert, der eingefuegt werden soll.
     *
     * @throws IllegalArgumentException
     *         wenn ResID nicht in der Tabelle vorhanden ist
     */
    @Deprecated
    public void put(int resID, byte[] value) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.put(key, value);
    }

    @Deprecated
    public void put(int resID, int value) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.put(key, value);
    }

    @Deprecated
    public void put(int resID, CharSequence value) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.put(key, value);
    }

    @Deprecated
    public void put(int resID, Date value) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.put(key, value);
    }

    @Deprecated
    public void put(int resID, long value) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.put(key, value);
    }

    @Deprecated
    public void put(int resID, boolean value) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.put(key, value);
    }

    @Deprecated
    public void putNull(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.putNull(key);
    }

    @Deprecated
    public void remove(int resID) {
        String key = AbstractDBHelper.mapResID2ColumnName.get(resID);
        super.remove(key);
    }
}
