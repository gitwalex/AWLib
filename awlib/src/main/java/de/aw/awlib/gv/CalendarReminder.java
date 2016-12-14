package de.aw.awlib.gv;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcel;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.content.ContextCompat;

import java.util.Date;
import java.util.Locale;

import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * Hilfsklasse fuer Erstellen, Aendern und loeschen von Calendar-Events
 */
@SuppressWarnings("MissingPermission")
public abstract class CalendarReminder extends AWApplicationGeschaeftsObjekt {
    public CalendarReminder(AWAbstractDBDefinition tbd, Long id) throws LineNotFoundException {
        super(AbstractDBHelper.getInstance().getContext(), tbd, id);
    }

    public CalendarReminder(AWAbstractDBDefinition tbd) {
        super(AbstractDBHelper.getInstance().getContext(), tbd);
    }

    protected CalendarReminder(Parcel in) {
        super(in);
    }

    /**
     * Erstellt einen Event im ausgewaehlten Caleendar
     *
     * @param calendarID
     *         ID des ausgewaehlten Kalenders
     * @param date
     *         Datum des Events
     * @param title
     *         Title des Events
     *
     * @return die ID des eingefuegten Events. -1, wenn ein Fehler aufgetreten ist.
     */
    public long createEvent(long calendarID, Date date, String title) {
        long id = -1;
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, date.getTime());
            values.put(CalendarContract.Events.DTEND, date.getTime());
            values.put(CalendarContract.Events.TITLE, title);
            values.put(CalendarContract.Events.CALENDAR_ID, calendarID);
            values.put(CalendarContract.Events.EVENT_TIMEZONE,
                    Locale.getDefault().getDisplayName());
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                id = Long.parseLong(uri.getLastPathSegment());
            }
        }
        return id;
    }

    /**
     * Loescht einen Eintrag aus dem Kalender
     *
     * @param calendarItemID
     *         ID des Eintrages, der geloescht werden soll
     *
     * @return true, wenn erfolgreich
     */
    public boolean deleteEvent(long calendarItemID) {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = getContext().getContentResolver();
            Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, calendarItemID);
            return (cr.delete(deleteUri, null, null) != -1);
        }
        return false;
    }

    /**
     * Setzt ein neues Datum fuer einen Kalendereintrag
     *
     * @param calendarItemID
     *         ID des items des Kalenders
     * @param date
     *         Neues Datum
     *
     * @return true, wenn erfolgreich
     */
    public boolean updateEventDate(long calendarItemID, Date date) {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = getContext().getContentResolver();
            ContentValues values = new ContentValues();
            Uri updateUri = null;
            // The new title for the event
            values.put(CalendarContract.Events.DTSTART, date.getTime());
            values.put(CalendarContract.Events.DTEND, date.getTime());
            updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, calendarItemID);
            return (cr.update(updateUri, values, null, null) != 0);
        }
        return false;
    }
}