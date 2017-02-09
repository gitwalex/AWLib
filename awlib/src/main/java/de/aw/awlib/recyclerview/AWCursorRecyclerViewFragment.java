package de.aw.awlib.recyclerview;

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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWCursorAdapter;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.fragments.AWLoaderManagerEngine;

/**
 * Erstellt eine Liste ueber Daten einer Tabelle.
 * <p/>
 * In der RecyclerView wird als Tag der Name der nutzenden Klasse gespeichert und damit bei
 * OnRecyclerItemClick() bzw. OnRecyclerItemLongClick() im Parent mitgeliefert.
 * <p/>
 * Als Standard erhaelt die RecyclerView als ID den Wert des Layout. Durch args.setInt(VIEWID,
 * value) erhaelt die RecyclerView eine andere ID.
 */
public abstract class AWCursorRecyclerViewFragment extends AWBaseRecyclerViewFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    protected int indexColumn;
    private AWLoaderManagerEngine mLoaderEngine;

    /**
     * Minimale Breite fuer eine Karte mit WertpapierInformationen. Ist die Ausfloesung sehr klein,
     * wird zumindest eine Karte angezeigt - auch wenns sch... aussieht :-(
     */
    protected AWCursorAdapter createBaseAdapter() {
        return new AWCursorAdapter(this, viewHolderLayout);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoaderEngine = new AWLoaderManagerEngine(this);
        mLoaderEngine.startOrRestartLoader(layout, args);
    }

    /**
     * Binden von Daten an eine View, die keine TextView ist.
     *
     * @param holder
     *         AWLibViewHolder. Hier sind alle Views zu finden.
     * @param view
     *         View
     * @param resID
     *         ResID der der Spalte des Cursors. Ist -1, wenn es mehr Views als CursorSpalten gibt.
     * @param cursor
     *         Aktueller Cursor
     * @param cursorPosition
     *         Position innerhalb des Cursors, dessen Daten gebunden werden sollen.
     * @return true, wenn die View vollstaendig bearbeitet wurde. Bei Rueckgabe von false wird davon
     * ausgegangen, dass es sich um eine TextView handelt und der Text aus dem Cursor an der
     * Position gesetzt. Default: false.
     */
    protected boolean onBindView(AWLibViewHolder holder, View view, int resID, Cursor cursor,
                                 int cursorPosition) {
        return false;
    }

    /**
     * @throws IllegalStateException
     *         Wenn eine View bearbeitet wird, die TextView ist und fillView(...) hat false
     *         zurueckgegeben.
     */
    public final void onBindViewHolder(AWLibViewHolder holder, Cursor cursor, int position) {
        if (!onBindingViewHolder(holder, cursor, position)) {
            for (int viewPosition = 0; viewPosition < viewResIDs.length; viewPosition++) {
                int resID = viewResIDs[viewPosition];
                View view = holder.findViewById(resID);
                if (!onBindView(holder, view, resID, cursor, viewPosition) && fromResIDs != null &&
                        viewPosition < fromResIDs.length) {
                    try {
                        AbstractDBHelper mDBHelper =
                                ((AWApplication) getContext().getApplicationContext())
                                        .getDBHelper();
                        TextView tv = (TextView) view;
                        String text = AWDBConvert.convert(mDBHelper, fromResIDs[viewPosition],
                                cursor.getString(viewPosition));
                        tv.setText(text);
                    } catch (ClassCastException e) {
                        throw new IllegalStateException(
                                "View mit ResID " + resID + " [" + getString(resID) +
                                        "] ist keine TextView und muss in onBindView belegt werden.");
                    }
                }
            }
        }
    }

    @Override
    public final void onBindViewHolder(AWLibViewHolder holder, int position) {
    }

    /**
     * Wird in onBindViewHolder() gerufen. Hier koennen Vorarbeiten fuer die Ermittlung der Daten
     * durchgefuehrt werden, z.B. je Holder Daten aus dem Cursor lesen. Hier wird im Holder die ID
     * aus dem Cursor gespeichert. Aussederm wird geprueft, ob der Holder zu der id als Selected
     * markiert wurde. Ist dies so, wird der Holder selected.
     *
     * @param holder
     *         AWLibViewHolder
     * @param cursor
     *         aktueller Cursor.
     * @param position
     *         Position
     */
    protected boolean onBindingViewHolder(AWLibViewHolder holder, Cursor cursor, int position) {
        return false;
    }

    @Override
    protected final boolean onBindingViewHolder(AWLibViewHolder holder, int position) {
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        noEntryView.setVisibility(View.VISIBLE);
        if (cursor != null) {
            indexColumn = cursor.getColumnIndexOrThrow(getString(R.string._id));
            if (cursor.getCount() != 0) {
                noEntryView.setVisibility(View.GONE);
            }
        }
        if (mAdapter == null) {
            mAdapter = createBaseAdapter();
            mRecyclerView.setAdapter(mAdapter);
        }
        ((AWCursorAdapter) mAdapter).swapCursor(cursor); // swap the new cursor in.
    }

    /*
         * (non-Javadoc)
         * @see
         * android.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.content
         * .Loader)
         */
    @Override
    public void onLoaderReset(Loader<Cursor> p1) {
        if (mAdapter != null) {
            ((AWCursorAdapter) mAdapter).swapCursor(null);
        }
    }

    protected void startOrRestartLoader(int loaderID, Bundle args) {
        mLoaderEngine.startOrRestartLoader(loaderID, args);
    }
}