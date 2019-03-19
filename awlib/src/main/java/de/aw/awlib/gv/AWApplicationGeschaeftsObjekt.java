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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.databinding.BaseObservable;
import android.os.Parcel;
import android.support.annotation.CallSuper;

import java.text.ParseException;
import java.util.Date;

import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.database.TableColumns;

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
public abstract class AWApplicationGeschaeftsObjekt extends BaseObservable
        implements AWInterface, TableColumns {
    private final String CLASSNAME = this.getClass().getSimpleName();
    /**
     * Tabellendefinition, fuer die dieser AWApplicationGeschaeftsObjekt gilt. Wird im Konstruktor
     * belegt.
     */
    private final AWAbstractDBDefinition tbd;
    protected String selection = _id + " = ?";
    protected String[] selectionArgs;
    /**
     * ID des AWApplicationGeschaeftsObjekt
     */
    protected Long id;
    /**
     * Abbild der jeweiligen Zeile der Datenbank. Werden nicht direkt geaendert.
     */
    private ContentValues currentContent = new ContentValues();
    private boolean isDirty;

    public AWApplicationGeschaeftsObjekt(AWAbstractDBDefinition tbd, Cursor c) {
        this(tbd);
        fillContent(c);
    }

    /**
     * Anlegen eines leeren Geschaeftsobjektes
     *
     * @param tbd
     *         AWAbstractDBDefinition
     */
    public AWApplicationGeschaeftsObjekt(AWAbstractDBDefinition tbd) {
        this.tbd = tbd;
    }

    public AWApplicationGeschaeftsObjekt(ContentResolver cr, AWAbstractDBDefinition tbd, long id) {
        this(tbd);
        selectionArgs = new String[]{String.valueOf(id)};
        String[] projection = tbd.getTableColumns();
        String orderby = tbd.getOrderString();
        Cursor c = getCursor(cr, tbd, projection, selection, selectionArgs, orderby);
        fillContent(c);
    }

    protected AWApplicationGeschaeftsObjekt(Parcel in) {
        this((AWAbstractDBDefinition) in
                .readParcelable(AWAbstractDBDefinition.class.getClassLoader()));
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.selectionArgs = in.createStringArray();
        this.currentContent = in.readParcelable(ContentValues.class.getClassLoader());
        this.isDirty = in.readByte() != 0;
    }

    public static Cursor getCursor(ContentResolver cr, AWAbstractDBDefinition tbd,
                                   String[] projection, String selection, String[] selectionArgs,
                                   String sortOrder) {
        return cr.query(tbd.getUri(), projection, selection, selectionArgs, sortOrder, null);
    }

    /**
     * Prueft, ob ein Wert zur ResID vorhanden ist.
     *
     * @param column
     *         Spaltenname
     *
     * @return true, wenn vorhanden. Sonst false
     */
    public boolean IsNull(String column) {
        return currentContent.containsKey(column);
    }

    /**
     * Prueft, ob ein Wert geaendert wurde oder bereits in der DB vorhanden ist.
     *
     * @param column
     *         Spaltenname
     *
     * @return true, wenn ein Wert ungleich null enthalten ist
     */
    public final boolean containsKey(String column) {
        return currentContent.get(column) != null;
    }

    protected void copy(AWApplicationGeschaeftsObjekt source) {
        currentContent = source.getContent();
        id = source.id;
    }

    protected int delete(AbstractDBHelper db) {
        if (id == null) {
            AWApplication.Log("AWApplicationGeschaeftsObjekt noch nicht angelegt! Delete nicht " +
                    "moeglich");
            return -1;
        }
        int result;
        result = db.delete(tbd, selection, selectionArgs);
        if (result != 0) {
            currentContent.putNull(_id);
            id = null;
            isDirty = false;
        }
        return result;
    }

    public int describeContents() {
        return 0;
    }

    /**
     * @param o
     *         zu vergleichendes Object.
     *
     * @return true,  wenn das vergleichende Object ein Geschaeftsobjekte ist, beide auf die gleiche
     * Tabelle zeigen (DBDefiniton)und die gleiche Anzahl Werte mit den gleichen Inhalten in den
     * ContentValues vorhanden sind.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AWApplicationGeschaeftsObjekt that = (AWApplicationGeschaeftsObjekt) o;
        return tbd == that.tbd && currentContent.equals(that.currentContent);
    }

    /**
     * Fuellt currentContent (aus einem Cursor. Cursor kann auch leer sein. Hat der Cursor mehrere
     * Zeilen, wird nur die erste uebernommen. Es werden nur Werte ungleich null uebernommen.
     * <p/>
     * Alle vorigen Werte werden verworfen.
     *
     * @param c
     *         Cursor
     */
    public final void fillContent(Cursor c) throws LineNotFoundException {
        if (c.isBeforeFirst()) {
            if (!c.moveToFirst()) {
                throw new LineNotFoundException("Cursor ist leer!");
            }
        }
        currentContent.clear();
        for (int i = 0; i < c.getColumnCount(); i++) {
            int type = c.getType(i);
            switch (type) {
                case Cursor.FIELD_TYPE_BLOB:
                    byte[] blob = c.getBlob(i);
                    currentContent.put(c.getColumnName(i), blob);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    currentContent.put(c.getColumnName(i), c.getFloat(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    currentContent.put(c.getColumnName(i), c.getLong(i));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    currentContent.put(c.getColumnName(i), c.getString(i));
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    putNull(c.getColumnName(i));
                    break;
                default:
                    String value = c.getString(i);
                    if (value != null) {
                        currentContent.put(c.getColumnName(i), c.getString(i));
                    }
            }
        }
        id = getAsLong(_id);
        selectionArgs = new String[]{id.toString()};
        currentContent.remove(_id);
        isDirty = false;
    }

    /**
     * Liefert den Wert aus dem Geschaeftsobjekt zu Spalte resID als Boolean.
     *
     * @param column
     *         Spalte
     *
     * @return Den aktuellen Wert der Spalte (true ooder false)
     */
    public final Boolean getAsBoolean(String column) {
        int value = getAsInt(column, 0);
        return value == 1;
    }

    public final byte[] getAsByteArray(String column) {
        return currentContent.getAsByteArray(column);
    }

    /**
     * Konvertiert ein Datum zurueck.
     *
     * @param datum
     *         Datum im SQLite-Format
     *
     * @return Date-Objekt oder null
     */
    public final Date getAsDate(String datum) {
        try {
            return new java.sql.Date(
                    AWDBConvert.mSqliteDateFormat.parse(getAsString(datum)).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Integer.
     *
     * @param column
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    public final Integer getAsInt(String column) {
        return currentContent.getAsInteger(column);
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Integer.
     *
     * @param column
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder default, wenn nicht vorhanden
     */
    public final int getAsInt(String column, int defaultValue) {
        Integer value = currentContent.getAsInteger(column);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als Long.
     *
     * @param column
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    public final Long getAsLong(String column) {
        return currentContent.getAsLong(column);
    }

    /**
     * Wie {@link AWApplicationGeschaeftsObjekt#getAsLong(String)}, liefert aber den Defaultwert
     * zuruck, wenn die Spalte nicht belegt iist.
     */
    public final long getAsLong(String column, long defaultWert) {
        Long value = getAsLong(column);
        if (value == null) {
            return defaultWert;
        }
        return value;
    }

    /**
     * Liefert den aktuellsten Wert aus dem Geschaeftsobjekt zu Spalte resID als String.
     *
     * @param column
     *         resID der Spalte
     *
     * @return Den aktuellen Wert der Spalte oder null, wenn nicht vorhanden
     */
    public final String getAsString(String column) {
        return currentContent.getAsString(column);
    }

    /**
     * @return Liefert eine Kopie der akteullen  Werte zurueck.
     */
    public final ContentValues getContent() {
        return new ContentValues(currentContent);
    }

    protected AWAbstractDBDefinition getDBDefinition() {
        return tbd;
    }

    /**
     * @return ID des Geschaeftsvorfalls
     */
    public long getID() {
        if (id == null) {
            throw new IllegalStateException("ID ist Null. Vorher insert() " + "ausfuehren");
        }
        return id;
    }

    /**
     * @return ID als Integer
     */
    public final Integer getIDAsInt() {
        if (id == null) {
            throw new IllegalStateException("ID ist Null. Vorher insert() " + "ausfuehren");
        }
        return id.intValue();
    }

    protected long insert(AbstractDBHelper db) {
        if (isInserted()) {
            throw new IllegalStateException(
                    "AWApplicationGeschaeftsObjekt bereits angelegt! Insert nicht moeglich");
        }
        id = db.insert(tbd, null, currentContent);
        if (id != -1) {
            currentContent.put(_id, id);
            selectionArgs = new String[]{id.toString()};
            isDirty = false;
        } else {
            AWApplication.Log("Insert in AWApplicationGeschaeftsObjekt " + CLASSNAME +
                    " fehlgeschlagen! Werte: " + currentContent.toString());
        }
        return id;
    }

    /**
     * @return true, wenn Aenderungen vorgenomen wurden.
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Check, ob das Geschaeftsobjekt schon in die DB eingefuegt wurde
     *
     * @return true, wenn bereits eingefuegt.
     */
    public final boolean isInserted() {
        return id != null;
    }

    public final boolean isNull(String column) {
        return currentContent.get(column) == null;
    }

    /**
     * Aendert oder Fuegt Daten ein. Werden mAccountID oder buchungsID belegt, werden die
     * entsprechenden Variaablen (neu) belegt. Wird catID belegt, wird aucch catname belegt. Wird
     * catname belegt, wird geprueft, ob catID bereits belegt ist. Wenn nicht, wird wird diese dort
     * auch belegt.
     *
     * @param column
     *         ResID der Spalte, die eingefuegt werden soll.
     * @param value
     *         Wert, der eingefuegt werden soll. Wert wird als String eingefuegt. Ist der Wert
     *         bereits identisch vorhanden (DB oder als geaenderter Wert), wird nichts eingefuegt.
     *         Ist value == null oder ein leerer String, wird ein ggfs. geaenderter Wert in der
     *         Spalte und nach Update dann auch aus der DB entfernt.
     */
    @CallSuper
    public void put(String column, boolean value) {
        if (value) {
            put(column, 1);
        } else {
            put(column, 0);
        }
        isDirty = true;
    }

    @CallSuper
    public void put(String column, int value) {
        currentContent.put(column, value);
        isDirty = true;
    }

    @CallSuper
    public void put(String column, long value) {
        currentContent.put(column, value);
        isDirty = true;
    }

    @CallSuper
    public void put(String column, float value) {
        currentContent.put(column, value);
        isDirty = true;
    }

    @CallSuper
    public void put(String column, double value) {
        currentContent.put(column, value);
        isDirty = true;
    }

    @CallSuper
    public void put(String column, Date date) {
        if (date != null) {
            currentContent.put(column, AWDBConvert.convertDate2SQLiteDate(date));
        } else {
            currentContent.putNull(column);
        }
        isDirty = true;
    }

    @CallSuper
    public void put(String column, String value) {
        if (value != null) {
            currentContent.put(column, value);
        } else {
            currentContent.putNull(column);
        }
        isDirty = true;
    }

    @CallSuper
    public void put(String column, CharSequence value) {
        if (value != null) {
            currentContent.put(column, value.toString());
        } else {
            currentContent.putNull(column);
        }
        isDirty = true;
    }

    /**
     * Aendert oder Fuegt Daten ein. Werden mAccountID oder buchungsID belegt, werden die
     * entsprechenden Variaablen (neu) belegt. Wird catID belegt, wird aucch catname belegt. Wird
     * catname belegt, wird geprueft, ob catID bereits belegt ist. Wenn nicht, wird wird diese dort
     * auch belegt.
     *
     * @param column
     *         ResID der Spalte, die eingefuegt werden soll.
     * @param value
     *         BlobWert, der eingefuegt werden soll.
     *
     * @throws IllegalArgumentException
     *         wenn ResID nicht in der Tabelle vorhanden ist
     */
    @CallSuper
    public void put(String column, byte[] value) {
        if (value == null) {
            currentContent.putNull(column);
        } else {
            currentContent.put(column, value);
        }
        isDirty = true;
    }

    @CallSuper
    public final void putNull(String column) {
        currentContent.putNull(column);
    }

    /**
     * @param column
     *         ResID der Spalte, die entfernt werden soll.
     *
     * @throws UnsupportedOperationException
     *         wennn _id entfernt werden soll.
     */
    @CallSuper
    public void remove(String column) {
        if (_id.equals(column)) {
            throw new UnsupportedOperationException("ID entfernen nur mit delete()!");
        }
        currentContent.remove(column);
        isDirty = true;
    }

    /**
     * Lesbare Informationen zum Geschaeftvorfall
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        if (id != null) {
            sb.append(", ID: ").append(getID());
        } else {
            sb.append(", Noch nicht eingefuegt");
        }
        sb.append(linefeed).append("Werte: ").append(currentContent.toString());
        return sb.toString();
    }

    protected int update(AbstractDBHelper db) {
        int result = 0;
        if (id == null) {
            throw new IllegalStateException(
                    "AWApplicationGeschaeftsObjekt noch nicht angelegt! Update nicht moeglich");
        }
        if (isDirty) {
            currentContent.put(_id, getID());
            selectionArgs = new String[]{id.toString()};
            result = db.update(tbd.getUri(), currentContent, selection, selectionArgs);
            if (result != 1) {
                throw new IllegalStateException(
                        "Fehler beim Update. Satz nicht gefunden mit RowID " + id);
            }
            isDirty = false;
        }
        return result;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.tbd, flags);
        dest.writeValue(this.id);
        dest.writeStringArray(this.selectionArgs);
        dest.writeParcelable(this.currentContent, flags);
        dest.writeByte(this.isDirty ? (byte) 1 : (byte) 0);
    }

    public static class LineNotFoundException extends RuntimeException {
        /**
         *
         */
        private static final long serialVersionUID = -3204185849776637352L;

        public LineNotFoundException(String detailMessage) {
            super(detailMessage);
        }
    }
}
