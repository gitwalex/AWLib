package de.aw.awlib.database;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import de.aw.awlib.activities.AWBasicActivity;
import de.aw.awlib.fragments.AWFragment;

import static de.aw.awlib.activities.AWInterface.DBDEFINITION;
import static de.aw.awlib.activities.AWInterface.FROMRESIDS;
import static de.aw.awlib.activities.AWInterface.GROUPBY;
import static de.aw.awlib.activities.AWInterface.ORDERBY;
import static de.aw.awlib.activities.AWInterface.PROJECTION;
import static de.aw.awlib.activities.AWInterface.SELECTION;
import static de.aw.awlib.activities.AWInterface.SELECTIONARGS;
import static de.aw.awlib.application.AWApplication.Log;
import static de.aw.awlib.application.AWApplication.LogError;

/**
 * LoaderFragment. Laedt mittels LoaderManager einen Cursor. Es werden folgende Argumente erwartet:
 * LAYOUT: Layout des Fragments
 * <p>
 * <p>
 * AWAbstractDBDefinition: AWAbstractDBDefinition des Fragments
 * <p>
 * VIEWRESIDS: VIEWRESIDS des Fragments FROMRESIDs: FROMRESIDs des Fragments
 * <p>
 * Ausserdem folgende optionale Argumente:
 * <p>
 * PROJECTION: Welche Spalten als Ergebnis erwartet werden. Ist diese nicht vorhanden, werden die
 * Spalten gemaess FROMRESIDs  geliefert
 * <p>
 * SELECTION: Selection fuer Cursor
 * <p>
 * SELECTIONARGS: Argumente fuer Selection
 * <p>
 * GROUPBY: GroupBy-Clause fuer Cursor
 * <p>
 * ORDERBY: OrderBy-Clause fuer Cursor. Ist diese nicht belegt, wird die OrderBy-Clause der
 * Tabellendefinition verwendet.
 * <p>
 * Beim Start des Loader wird in der ActionBar die ProgressBar-Indeterminate-Visibility auf true
 * gesetzt. Nach dem Laden wird diese wieder abgeschaltet. Daher ist zwingend beim Ueberschreiben
 * von {@link AWLoaderManagerEngine#onCreateLoader(int, Bundle)} sowie {@link
 * AWLoaderManagerEngine#onLoadFinished(Loader, Cursor)} super(...) zu rufen! }
 */
public class AWLoaderManagerEngine implements LoaderManager.LoaderCallbacks<Cursor> {
    private final Callback mCallback;
    private final Context mContext;
    private final LoaderManager mLoaderManager;
    private Cursor mCursor;

    public AWLoaderManagerEngine(AWBasicActivity activity) {
        mContext = activity;
        mLoaderManager = activity.getSupportLoaderManager();
        try {
            //noinspection unchecked
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            Log("Activity must implement AWLoaderManagerEngine.Callback)");
            throw e;
        }
    }

    public AWLoaderManagerEngine(AWFragment fragment) {
        mContext = fragment.getContext();
        mLoaderManager = fragment.getLoaderManager();
        try {
            //noinspection unchecked
            mCallback = (Callback) fragment;
        } catch (ClassCastException e) {
            Log("Fragment must implement AWLoaderManagerEngine.Callback)");
            throw e;
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Aufbau des Select-Statements.
     * <p/>
     * Ueber args kann folgendes gesteuert werden:
     * <p/>
     * args.getStringArray(PROJECTION): Columns, die ermittelt werden sollen. Ist das Feld nicht
     * belegt, werden die Spalten gemaess args.getIntArray(FROMRESIDS) geholt.
     * <p>
     * args.getString(SELECTION): Where-Clause
     * <p>
     * args.getStringArray(SELECTIONARGS): Argumente fuer SELECTION
     * <p>
     * args.getString(GROUPBY): GroupBy-Clause
     * <p>
     * args.getString(ORDERBY): OrderBy-Clause. Ist dies nicht belegt, wird der Cursor gemaess
     * {@link AbstractDBHelper#getOrderString(AWAbstractDBDefinition)} sortiert.
     *
     * @throws NullPointerException
     *         wenn args.getParcelable(DBDEFINITION) Null liefert
     * @throws NullPointerException
     *         wenn weder PROJECTION noch FROMRESIDS belegt sind
     * @see LoaderManager.LoaderCallbacks#onCreateLoader(int, Bundle)
     */
    @Override
    public Loader<Cursor> onCreateLoader(int p1, Bundle args) {
        mCallback.setCursorLoaderArguments(p1, args);
        AWAbstractDBDefinition tbd = args.getParcelable(DBDEFINITION);
        if (tbd != null) {
            Uri mUri = tbd.getUri();
            String[] projection = args.getStringArray(PROJECTION);
            int[] fromResIDs = args.getIntArray(FROMRESIDS);
            if (projection != null && fromResIDs != null) {
                LogError(getClass().getSimpleName() +
                        ": PROJECTION und FROMRESIDS sind belegt! Pruefen!");
            }
            if (projection == null) {
                // Null: Also fromResIDs
                projection = tbd.columnNames(fromResIDs);
            }
            if (projection == null) {
                projection = tbd.columnNames(tbd.getTableItems());
            }
            String selection = args.getString(SELECTION);
            String[] selectionArgs = args.getStringArray(SELECTIONARGS);
            String groupBy = args.getString(GROUPBY);
            if (groupBy != null) {
                if (selection == null) {
                    selection = " 1=1";
                }
                selection = selection + " GROUP BY " + groupBy;
            }
            String orderBy = args.getString(ORDERBY);
            if (orderBy == null) {
                orderBy = tbd.getOrderString();
            }
            return new CursorLoader(mContext, mUri, projection, selection, selectionArgs, orderBy);
        }
        return null;
    }

    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        mCallback.onLoadFinished(loader, cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> p1) {
        mCallback.onLoaderReset(p1);
    }

    /**
     * Initialisiert oder restartet einen Loader.
     *
     * @param loaderID
     *         id des loaders, der (nach-) gestartet werden soll</br>
     * @param args
     *         Argumente fuer Cursor
     */
    public void startOrRestartLoader(int loaderID, Bundle args) {
        Loader<Cursor> loader = mLoaderManager.getLoader(loaderID);
        if (loader != null && !loader.isReset()) {
            mLoaderManager.restartLoader(loaderID, args, this);
        } else {
            mLoaderManager.initLoader(loaderID, args, this);
        }
    }

    public interface Callback {
        /**
         * Wird gerufen, wenn der Loader den Cursor erhalten hat
         *
         * @param loader
         *         loader
         * @param cursor
         *         cursor mit Daten
         */
        void onLoadFinished(Loader<Cursor> loader, Cursor cursor);

        /**
         * Wird gerufen, wenn der Loader resettet wird
         *
         * @param loader
         *         loader
         */
        void onLoaderReset(Loader<Cursor> loader);

        /**
         * Wird vor Erstellung des Loaders gerufen. Letzte Moeglichkeit, Argumente fuer den Loader
         * zu setzen
         *
         * @param loaderID
         *         id des Loader, der initialisiert wird
         * @param args
         *         Bundle. Hier sollen die Argumente gesetzt werden
         */
        void setCursorLoaderArguments(int loaderID, Bundle args);
    }
}
