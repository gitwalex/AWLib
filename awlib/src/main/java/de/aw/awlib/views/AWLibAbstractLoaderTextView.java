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
package de.aw.awlib.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.AttributeSet;
import android.widget.TextView;

import de.aw.awlib.AWLibInterface;
import de.aw.awlib.R;
import de.aw.awlib.database.AWLibDBDefinition;

/**
 * Convenience-Klasse fuer TextView, die mit einem Loader hinterlegt ist. ID der TextView wird
 * entweder durch xml vorgegeben. Alternativ wird die resID benutzt, die in initialize() uebergeben
 * wird.
 */
public abstract class AWLibAbstractLoaderTextView extends TextView
        implements AWLibInterface, LoaderManager.LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener {
    private final Bundle args = new Bundle();
    private boolean isInitialized;
    private LoaderManager mLoaderManager;
    private LoaderTextViewListener mLoaderTextViewListener;
    private Object mTag;

    public AWLibAbstractLoaderTextView(Context context) {
        super(context);
    }

    public AWLibAbstractLoaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWLibAbstractLoaderTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected abstract String convertValue(Cursor data);

    /**
     * Initialisiert die TextView/ Loader und startet Loader
     *
     * @param lm
     *         LoaderManager
     * @param tbd
     *         DBDefinition fuer Loader
     * @param resID
     *         des Items, welches geladen wird. Wird fuer Anzeige der Daten im korrekten Format
     *         benoetigt.
     * @param projection
     *         Item, welches geladen werden soll.
     * @param selection
     *         Selection. Kann null sein
     * @param selectionArgs
     *         Argumente der Selection. Kann null sein.
     *
     * @throws LoaderTextViewException
     *         wenn Argumente nicht vollstaendig sind oder nicht zueinander passen.
     */
    public void initialize(LoaderManager lm, AWLibDBDefinition tbd, int resID, String projection,
                           String selection, String[] selectionArgs) {
        isInitialized = true;
        mLoaderManager = lm;
        setArguments(tbd, resID, projection, selection, selectionArgs);
        // Starten des Loaders
        startOrRestartLoader(getId(), args);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        AWLibDBDefinition tbd = args.getParcelable(DBDEFINITION);
        String[] projection = args.getStringArray(PROJECTION);
        String selection = args.getString(SELECTION);
        String[] selectionArgs = args.getStringArray(SELECTIONARGS);
        return new CursorLoader(getContext(), tbd.getUri(), projection, selection, selectionArgs,
                null);
    }

    /**
     * Belegt die TextView mit den Daten aus dem Cursor.
     *
     * @see LoaderManager.LoaderCallbacks#onLoadFinished(Loader, Object)
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String text;
        if (data.moveToFirst()) {
            int rows = data.getCount();
            int cols = data.getColumnCount();
            if (rows > 1) {
                throw new LoaderTextViewException(
                        "SQL-Statement liefert zu viele Daten( Rows: " + rows + ", Cols: )" + cols);
            }
            text = convertValue(data);
        } else {
            text = getContext().getString(R.string.na);
        }
        setText(text);
        if (mLoaderTextViewListener != null) {
            mLoaderTextViewListener.onTextChanged(text, mTag);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // keine Reference auf Daten gehalten. Nichts zu tun
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mLoaderTextViewListener.changedSharedPreferences(sharedPreferences, key);
    }

    /**
     * Entfernt den TextChangeListener
     */
    public void removeTextChangeListener() {
        mLoaderTextViewListener = null;
    }

    /**
     * Setzt die Argumente fuer die Selection neu und restartet den Loader.
     */
    public void restart(String selection, String[] selectionArgs) {
        args.putString(SELECTION, selection);
        args.putStringArray(SELECTIONARGS, selectionArgs);
        startOrRestartLoader(getId(), args);
    }

    private void setArguments(AWLibDBDefinition tbd, int resID, String projection, String selection,
                              String[] selectionArgs) {
        if (tbd == null) {
            throw new LoaderTextViewException("DBDefinition ist null.");
        }
        if (projection == null) {
            throw new LoaderTextViewException("Projection muss Element enthalten.");
        }
        if (resID == 0) {
            throw new LoaderTextViewException(
                    "resID muss mit einem Wert initialisiert sein. Wird fuer Konvertieren der " + "Daten benoetigt.");
        }
        String[] projectionArray = new String[]{projection};
        // setzten der ID, wenn noch nicht vorhanden.
        if (getId() == NO_ID) {
            setId(resID);
        }
        args.putParcelable(DBDEFINITION, tbd);
        args.putStringArray(PROJECTION, projectionArray);
        args.putString(SELECTION, selection);
        args.putStringArray(SELECTIONARGS, selectionArgs);
        args.putInt(FROMRESIDS, resID);
    }

    public void setTextChangeListener(LoaderTextViewListener t) {
        mLoaderTextViewListener = t;
    }

    /**
     * Setzt den TextChangeListener. Dieser wird gerufen, wenn sich am Inhalt des Textfeldes etwas
     * aendert.
     *
     * @param t
     *         TextChangeListener
     * @param tag
     *         TAG, welches zur Unterscheidung benutzt werden kann. Wird beim Aufruf onTextChanged
     *         mitgeliefert
     */
    public void setTextChangeListener(LoaderTextViewListener t, Object tag) {
        setTextChangeListener(t);
        this.mTag = tag;
    }

    private void startOrRestartLoader(int loaderID, Bundle args) {
        Loader<Cursor> loader = mLoaderManager.getLoader(loaderID);
        if (loader != null && !loader.isReset()) {
            mLoaderManager.restartLoader(loaderID, args, this);
        } else {
            mLoaderManager.initLoader(loaderID, args, this);
        }
    }

    /**
     * Interface fuer Aenderungen am Inhalt der TextView. Wird in onLoadFinished gerufen.
     */
    public interface LoaderTextViewListener {
        void changedSharedPreferences(SharedPreferences sharedPreferences, String key);

        /**
         * Liefert eine Aenderung des Textes aus einer LoaderTextView, wenn sich aufgrund einer
         * Datenbankaenderung der Text geaendert hat.
         *
         * @param text
         *         neuer Text der TextView
         * @param tag
         *         TAG, welches beim setzten des TextChangeListeners gesetzt wird. Kann zur
         *         Unterscheidung dienen,
         */
        void onTextChanged(String text, Object tag);
    }

    /**
     * RuntimeException, die geworfen wird, wenn Argumente nicht vollstaendig oder falsch sind.
     */
    @SuppressWarnings("serial")
    public class LoaderTextViewException extends RuntimeException {
        public LoaderTextViewException(String detailMessage) {
            super(detailMessage + ", Argumente: " + args);
        }
    }
}
