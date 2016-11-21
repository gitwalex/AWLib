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
package de.aw.awlib.database_private;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.application.AWLIbApplication;

/**
 * Erweiterung des MainContentProviders. Hier koennen MonMa-spazifische Funtionen festgelegt
 * werden.
 *
 * @author alex
 */
public class AWLibContentProvider extends ContentProvider implements AWLibInterface {
    public static final String AUTHORITY = "de.aw.awlibcontentprovider";
    private static final SparseArray<Uri> CONTENT_URI = new SparseArray<>();
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        String BASE_PATH;
        int code;
        // URIs festlegen fuer die einzelnen Tabellen und Views
        for (AWLibDBDefinition tbd : AWLibDBDefinition.values()) {
            BASE_PATH = tbd.name();
            code = tbd.getUriCode();
            CONTENT_URI.put(code, Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH));
            sURIMatcher.addURI(AUTHORITY, BASE_PATH, code);
        }
        for (Functions uris : Functions.values()) {
            BASE_PATH = uris.name();
            code = uris.getUriCode();
            CONTENT_URI.put(code, Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH));
            sURIMatcher.addURI(AUTHORITY, BASE_PATH, code);
        }
        // URIs festlegen fuer die einzelnen zusaetzlichen Funktionen
        for (MonMaFunctions uris : MonMaFunctions.values()) {
            BASE_PATH = uris.name();
            code = uris.getUriCode();
            CONTENT_URI.put(code, Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH));
            sURIMatcher.addURI(AUTHORITY, BASE_PATH, code);
        }
    }

    private boolean batchMode;
    private AWLibDBChangeHelper db;

    /**
     * Liefert URI zur AWLibDBDefinition zurueck
     *
     * @param ordinal
     *         ordinal der AWLibDBDefinition
     *
     * @return URI fuer Aufruf
     */
    public static Uri getContentURI(int ordinal) {
        return CONTENT_URI.get(ordinal);
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside a {@link
     * SQLiteDatabase} transaction. All changes will be rolled back if any single one fails.
     */
    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(
            @NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final int numOperations = operations.size();
        batchMode = true;
        final ContentProviderResult[] results = new ContentProviderResult[numOperations];
        Set<Uri> uris = new HashSet<>();
        db = new AWLibDBChangeHelper();
        db.beginTransaction();
        try {
            for (int i = 0; i < numOperations; i++) {
                uris.add(operations.get(i).getUri());
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            for (Uri uri : uris) {
                int uriType = sURIMatcher.match(uri);
                AWLibDBDefinition tbd = AWLibDBDefinition.values()[uriType];
                notifyCursors(tbd);
            }
        } finally {
            db.endTransaction();
            db = null;
            batchMode = false;
        }
        return results;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int uriType = sURIMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new UnsupportedOperationException("Uri " + uri + " nicht gefunden!");
        }
        int result = 0;
        AWLibDBDefinition tbd = AWLibDBDefinition.values()[uriType];
        db = new AWLibDBChangeHelper();
        batchMode = true;
        db.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long erg;
                switch (tbd) {
                    default:
                        erg = db.insert(tbd, null, cv);
                        break;
                }
                if (erg == -1 && AWLIbApplication.getDebugFlag()) {
                    AWLIbApplication.Log("Insert fehlgeschlagen! Werte: " + cv.toString());
                } else {
                    result++;
                }
                db.setTransactionSuccessful();
                notifyCursors(tbd);
            }
        } finally {
            db.endTransaction();
            db = null;
            batchMode = false;
        }
        return result;
    }

    @Nullable
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        try {
            Functions func = Functions.valueOf(method);
            switch (func) {
                case DoVacuum:
                    return doVacuum();
            }
        } catch (IllegalArgumentException e) {
            return super.call(method, arg, extras);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new UnsupportedOperationException("Uri " + uri + " nicht gefunden!");
        }
        if (!batchMode) {
            db = new AWLibDBChangeHelper();
        }
        int rowsDeleted = 0;
        AWLibDBDefinition tbd = AWLibDBDefinition.values()[uriType];
        switch (tbd) {
            default:
                rowsDeleted = db.delete(tbd, selection, selectionArgs);
                notifyCursors(tbd);
        }
        if (!batchMode) {
            db = null;
        }
        return rowsDeleted;
    }

    public Bundle doVacuum() {
        SQLiteDatabase database = AWLibDBHelper.getDatabase();
        database.execSQL("vacuum");
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new UnsupportedOperationException("Uri " + uri + " nicht gefunden!");
        }
        if (!batchMode) {
            db = new AWLibDBChangeHelper();
        }
        AWLibDBDefinition tbd = AWLibDBDefinition.values()[uriType];
        long id;
        switch (tbd) {
            default:
                id = db.insert(tbd, null, values);
        }
        notifyCursors(tbd);
        if (!batchMode) {
            db = null;
        }
        return Uri.parse(tbd.name() + "/" + id);
    }

    /**
     * In Abhaengigkeite der DBDefintion werden verschiedene Uris informiert
     *
     * @param tbd
     *         AWLibDBDefinition
     */
    // TODO: 06.11.2015 Ergaenzen
    public void notifyCursors(AWLibDBDefinition tbd) {
        ContentResolver resolver = getContext().getContentResolver();
        resolver.notifyChange(tbd.getUri(), null);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] from, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = AWLibDBHelper.getInstance().getReadableDatabase();
        int uriType = sURIMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new UnsupportedOperationException("Uri " + uri + " nicht gefunden!");
        }
        AWLibDBDefinition tbd = AWLibDBDefinition.values()[uriType];
        Cursor c = tbd.getCursor(database, from, selection, selectionArgs, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        if (uriType == UriMatcher.NO_MATCH) {
            throw new UnsupportedOperationException("Uri " + uri + " nicht gefunden!");
        }
        if (!batchMode) {
            db = new AWLibDBChangeHelper();
        }
        AWLibDBDefinition tbd = AWLibDBDefinition.values()[uriType];
        int rowsUpdated;
        switch (tbd) {
            default:
                rowsUpdated = db.update(tbd, values, selection, selectionArgs);
                break;
        }
        notifyCursors(tbd);
        if (!batchMode) {
            db = null;
        }
        return rowsUpdated;
    }

    /**
     * Hier koennen Funktionen festgelegt werden. Diese Funktion etspricht einer Methode im
     * ContentProvider. Der Aufruf erfolgt mittels {@link ContentResolver#call(Uri, String, String,
     * Bundle)} , wobei die Uris ueber {@link AWLibContentProvider#getContentURI(int)} ermittel
     * werden koennen.
     */
    public enum Functions {
        DoVacuum;

        /**
         * Uricode fuer Functions wird beginnend mit 500 festgelegt
         *
         * @return Uricode. Berechnung wie folgt: 500 + ordinal, also fuer z.B. DoVacuum wird 500
         * zurueckgegeben
         */
        public int getUriCode() {
            int code = 500 + ordinal();
            return code;
        }
    }

    /**
     * Funktionen fuer interne Calls. Siehe {@link AWLibContentProvider.Functions}
     */
    public enum MonMaFunctions {
        ;

        /**
         * UriCode erhaelt Werte ab 1000 aufwaerts
         *
         * @return Uricode, z.B. fuer DoSaldoUpdate wird 1000 zuruckgegeben.
         */
        public int getUriCode() {
            return 1000 + ordinal();
        }
    }
}
