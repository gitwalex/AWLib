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
import android.support.annotation.CallSuper;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWBaseRecyclerViewAdapter;
import de.aw.awlib.adapters.AWCursorRecyclerViewAdapter;
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
public abstract class AWCursorRecyclerViewFragment extends AWBaseRecyclerViewFragment {
    protected int indexColumn;

    /**
     * Minimale Breite fuer eine Karte mit WertpapierInformationen. Ist die Ausfloesung sehr klein,
     * wird zumindest eine Karte angezeigt - auch wenns sch... aussieht :-(
     */
    protected AWBaseRecyclerViewAdapter getBaseAdapter() {
        return new AWCursorRecyclerViewAdapter(this, viewHolderLayout);
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
    public final void onBindViewHolder(AWLibViewHolder holder, Cursor cursor) {
        onPreBindViewHolder(cursor, holder);
        for (int viewPosition = 0; viewPosition < viewResIDs.length; viewPosition++) {
            int resID = viewResIDs[viewPosition];
            View view = holder.findViewById(resID);
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
        noEntryView.setVisibility(View.VISIBLE);
        if (cursor != null) {
            indexColumn = cursor.getColumnIndexOrThrow(getString(R.string._id));
            if (cursor.getCount() != 0) {
                noEntryView.setVisibility(View.GONE);
            }
        }
        if (mAdapter == null) {
            mAdapter = getBaseAdapter();
            mRecyclerView.setAdapter(mAdapter);
        }
        ((AWCursorRecyclerViewAdapter) mAdapter).swapCursor(cursor); // swap the new cursor in.
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
            ((AWCursorRecyclerViewAdapter) mAdapter).swapCursor(null);
        }
    }

    /**
     * Wird in onBindViewHolder() gerufen. Hier koennen Vorarbeiten fuer die Ermittlung der Daten
     * durchgefuehrt werden, z.B. je Holder Daten aus dem Cursor lesen. Hier wird im Holder die ID
     * aus dem Cursor gespeichert. Aussederm wird geprueft, ob der Holder zu der id als Selected
     * markiert wurde. Ist dies so, wird der Holder selected.
     *
     * @param cursor
     *         aktueller Cursor.
     * @param holder
     *         AWLibViewHolder
     */
    @CallSuper
    protected void onPreBindViewHolder(Cursor cursor, AWLibViewHolder holder) {
        int indexID = cursor.getColumnIndex(getString(R.string._id));
        if (indexID != -1) {
            long id = cursor.getLong(indexID);
            holder.setID(id);
        }
    }
}