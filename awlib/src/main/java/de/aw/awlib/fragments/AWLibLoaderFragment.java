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
package de.aw.awlib.fragments;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;

import de.aw.awlib.R;
import de.aw.awlib.database.AWLibAbstractDBDefinition;

/**
 * LoaderFragment. Laedt mittels LoaderManager einen Cursor. Es werden folgende Argumente erwartet:
 * LAYOUT: Layout des Fragments
 * <p>
 * <p>
 * AWLibAbstractDBDefinition: AWLibAbstractDBDefinition des Fragments
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
 * von {@link AWLibLoaderFragment#onCreateLoader(int, Bundle)} sowie {@link
 * AWLibLoaderFragment#onLoadFinished(Loader, Cursor)} super(...) zu rufen! }
 */
public abstract class AWLibLoaderFragment extends AWLibFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ISRUNNING = "ISRUNNING";
    private View mProgressbar;

    @Override
    final protected boolean onBindView(View view, int resID) {
        return super.onBindView(view, resID);
    }

    /**
     * Startet den Loader neu
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        startOrRestartLoader(layout, args);
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
     * {@link AWLibAbstractDBDefinition#getOrderString()} sortiert.
     *
     * @throws NullPointerException
     *         wenn args.getParcelable(DBDEFINITION) Null liefert
     * @throws NullPointerException
     *         wenn weder PROJECTION noch FROMRESIDS belegt sind
     * @see LoaderManager.LoaderCallbacks#onCreateLoader(int, Bundle)
     */
    @Override
    @CallSuper
    public Loader<Cursor> onCreateLoader(int p1, Bundle args) {
        if (mProgressbar != null) {
            mProgressbar.setVisibility(View.VISIBLE);
        }
        AWLibAbstractDBDefinition tbd = args.getParcelable(DBDEFINITION);
        assert tbd != null;
        Uri mUri = tbd.getUri();
        String[] projection = args.getStringArray(PROJECTION);
        int[] fromResIDs = args.getIntArray(FROMRESIDS);
        if (projection != null && fromResIDs != null) {
            LogError(getClass()
                    .getSimpleName() + ": PROJECTION und FROMRESIDS sind belegt! Pruefen!");
        }
        if (projection == null) {
            // Null: Also fromResIDs
            projection = tbd.columnNames(fromResIDs);
        }
        if (projection == null) {
            // IMMER noch Null - Fehler!
            throw new NullPointerException(
                    "Weder PROJECTION noch FROMRESIDS belegt. Weiss nicht, was tun");
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
        return new CursorLoader(getActivity(), mUri, projection, selection, selectionArgs, orderBy);
    }

    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mProgressbar != null) {
            mProgressbar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> p1) {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mProgressbar != null) {
            mProgressbar.setVisibility(View.INVISIBLE);
        }
    }

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();
        startOrRestartLoader(layout, args);
    }

    @CallSuper
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressbar = view.findViewById(R.id.awlib_default_recyclerview_progressbar);
    }

    /**
     * Initialisiert oder restartet einen Loader.
     *
     * @param loaderID
     *         id des loaders, der (nach-) gestartet werden soll</br>
     * @param args
     *         Argumente fuer Cursor
     */
    final protected void startOrRestartLoader(int loaderID, Bundle args) {
        LoaderManager lm = getLoaderManager();
        Loader<Cursor> loader = lm.getLoader(loaderID);
        if (loader != null && !loader.isReset()) {
            lm.restartLoader(loaderID, args, this);
        } else {
            lm.initLoader(loaderID, args, this);
        }
    }
}
