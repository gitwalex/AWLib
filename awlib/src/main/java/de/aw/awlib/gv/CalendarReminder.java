package de.aw.awlib.gv;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.aw.awlib.application.AWApplication;

/**
 * Hilfsklasse fuer Erstellen, Aendern und loeschen von Calendar-Events
 */
@SuppressWarnings("MissingPermission")
public class CalendarReminder {
    private final Context mContext;

    public CalendarReminder(Context context) {
        mContext = context;
    }

    /**
     * Erstellt einen Taeglichen Termin
     *
     * @param calendarID
     *         calendarID
     * @param title
     *         Titel
     * @param body
     *         Body
     *
     * @return ID des Event, -1 bei Fehler
     */
    public long createDailyEvent(long calendarID, String title, String body) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        return createEvent(calendarID, cal.getTime(), cal.getTime(), title, body);
    }

    /**
     * Erstellt einen Event im ausgewaehlten Caleendar
     *
     * @param calendarID
     *         ID des ausgewaehlten Kalenders
     * @param start
     *         Startdatum des Events
     * @param end
     *         Endedatum des Events (optional)
     * @param title
     *         Title des Events
     * @param body
     *         Body des Events (optional)
     *
     * @return die ID des eingefuegten Events. -1, wenn ein Fehler aufgetreten ist.
     */
    public long createEvent(long calendarID, @NonNull Date start, @Nullable Date end,
                            @NonNull String title, @Nullable String body) {
        long id = -1;
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = createEventValues(calendarID, start, end, title, body);
            Uri uri = cr.insert(Events.CONTENT_URI, values);
            if (uri != null) {
                id = Long.parseLong(uri.getLastPathSegment());
            }
        }
        return id;
    }

    /**
     * Erstellt ContentValues fuer einen Event im ausgewaehlten Calendar
     *
     * @param calendarID
     *         ID des ausgewaehlten Kalenders
     * @param start
     *         Startdatum des Events
     * @param end
     *         Endedatum des Events (optional)
     * @param title
     *         Title des Events
     * @param body
     *         Body des Events (optional)
     *
     * @return die ID des eingefuegten Events. -1, wenn ein Fehler aufgetreten ist.
     */
    private ContentValues createEventValues(long calendarID, @NonNull Date start,
                                            @Nullable Date end, @NonNull String title,
                                            @Nullable String body) {
        ContentValues values = new ContentValues();
        values.put(Events.DTSTART, start.getTime());
        if (end != null) {
            values.put(Events.DTEND, end.getTime());
        }
        values.put(Events.TITLE, title);
        if (body != null) {
            values.put(Events.DESCRIPTION, body);
        }
        values.put(Events.CALENDAR_ID, calendarID);
        values.put(Events.EVENT_TIMEZONE, Locale.getDefault().getDisplayName());
        return values;
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
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = mContext.getContentResolver();
            Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, calendarItemID);
            return (cr.delete(deleteUri, null, null) != -1);
        }
        return false;
    }

    public void dumpEvents() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] projection =
                            new String[]{Events.CALENDAR_ID, Events.ORGANIZER, Events.TITLE,
                                    Events.EVENT_LOCATION, Events.DESCRIPTION, Events.EVENT_COLOR,
                                    Events.DTSTART, Events.DTEND, Events.EVENT_TIMEZONE,
                                    Events.EVENT_END_TIMEZONE, Events.DURATION, Events.ALL_DAY,
                                    Events.RRULE, Events.RDATE, Events.EXRULE, Events.EXDATE,
                                    Events.ORIGINAL_ID, Events.ORIGINAL_SYNC_ID,
                                    Events.ORIGINAL_INSTANCE_TIME, Events.ORIGINAL_ALL_DAY,
                                    Events.ACCESS_LEVEL, Events.AVAILABILITY,
                                    Events.GUESTS_CAN_MODIFY, Events.GUESTS_CAN_INVITE_OTHERS,
                                    Events.GUESTS_CAN_SEE_GUESTS, Events.CUSTOM_APP_PACKAGE,
                                    Events.CUSTOM_APP_URI, Events.DIRTY, Events._SYNC_ID,
                                    Events.SYNC_DATA1, Events.SYNC_DATA2, Events.SYNC_DATA3,
                                    Events.SYNC_DATA4, Events.SYNC_DATA5, Events.SYNC_DATA6,
                                    Events.SYNC_DATA7, Events.SYNC_DATA8, Events.SYNC_DATA9,
                                    Events.SYNC_DATA10};
                    Cursor c = mContext.getContentResolver()
                            .query(Events.CONTENT_URI, projection, null, null, null);
                    try {
                        if (c.moveToFirst()) {
                            do {
                                StringBuilder sb = new StringBuilder("Event: ");
                                for (int i = 0; i < projection.length; i++) {
                                    String value = c.getString(i);
                                    if (value != null && !value.equals("0")) {
                                        sb.append(", " + projection[i] + ":" + c.getString(i));
                                    }
                                }
                                AWApplication.Log(sb.toString());
                            } while (c.moveToNext());
                        }
                    } finally {
                        c.close();
                    }
                }
            }).start();
        }
    }

    /**
     * Setzt ein neues Datum fuer einen Kalendereintrag
     *
     * @param eventID
     *         ID des items des Kalenders
     * @param date
     *         Neues Datum
     *
     * @return true, wenn erfolgreich
     */
    public boolean updateEventDate(long eventID, Date date) {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            // The new title for the event
            values.put(CalendarContract.Events.DTSTART, date.getTime());
            values.put(CalendarContract.Events.DTEND, date.getTime());
            Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, eventID);
            return (cr.update(updateUri, values, null, null) != 0);
        }
        return false;
    }
}