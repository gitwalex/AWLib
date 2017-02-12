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
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWBaseAdapter;
import de.aw.awlib.adapters.AWCursorAdapter;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.database.AbstractDBHelper;

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
        implements AWCursorAdapter.AWCursorRecyclerViewBinder {
    protected int indexColumn;
    private AWCursorAdapter mAdapter;
    //    protected AWCursorAdapter createBaseAdapter() {
    //    }

    @Override
    protected final AWBaseAdapter createBaseAdapter() {
        mAdapter = createCursorAdapter();
        return mAdapter;
    }

    protected AWCursorAdapter createCursorAdapter() {
        return new AWCursorAdapter(this);
    }

    public AWCursorAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = createCursorAdapter();
        }
        return mAdapter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startOrRestartLoader(layout, args);
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
     * Belegt anhand der viewResIDs in Args die View. Das Format wird automatisch konvertiert.
     * <p>
     * Sollte nur gerufen werden, wenn die View nicht anderweitig belegt wird, sonden z.B. durch
     * Databinding
     *
     * @throws NullPointerException
     *         wenn viewResIDs oder fromResIDs null ist oder die in viewResIDs aufgefuehrte View
     *         nicht gefunden wird
     * @throws IllegalStateException
     *         Wenn eine View bearbeitet wird, die TextView ist und fillView(...) hat false
     *         zurueckgegeben.
     */
    public void onBindViewHolder(AWLibViewHolder holder, Cursor cursor, int position) {
        for (int viewPosition = 0; viewPosition < viewResIDs.length; viewPosition++) {
            int resID = viewResIDs[viewPosition];
            View view = holder.itemView.findViewById(resID);
            if (!onBindView(holder, view, resID, cursor, viewPosition) && fromResIDs != null &&
                    viewPosition < fromResIDs.length) {
                try {
                    AbstractDBHelper mDBHelper =
                            ((AWApplication) getContext().getApplicationContext()).getDBHelper();
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

    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        if (cursor != null) {
            indexColumn = cursor.getColumnIndexOrThrow(getString(R.string._id));
        }
        setAdapter(getAdapter());
        getAdapter().swapCursor(cursor); // swap the new cursor in.
    }

    /*
         * (non-Javadoc)
         * @see
         * android.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.content
         * .Loader)
         */
    @Override
    public void onLoaderReset(Loader<Cursor> p1) {
        if (getAdapter() != null) {
            getAdapter().swapCursor(null);
        }
    }

    protected void setAdapter(AWCursorAdapter adapter) {
        mAdapter = adapter;
        super.setAdapter(adapter);
    }
}