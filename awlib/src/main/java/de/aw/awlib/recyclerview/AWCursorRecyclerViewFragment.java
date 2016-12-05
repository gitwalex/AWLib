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
package de.aw.awlib.recyclerview;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.database.AWAbstractDBDefinition;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.fragments.AWLoaderFragment;

/**
 * Erstellt eine Liste ueber Daten einer Tabelle.
 * <p/>
 * In der RecyclerView wird als Tag der Name der nutzenden Klasse gespeichert und damit bei
 * OnRecyclerItemClick() bzw. OnRecyclerItemLongClick() im Parent mitgeliefert.
 * <p/>
 * Als Standard erhaelt die RecyclerView als ID den Wert des Layout. Durch args.setInt(VIEWID,
 * value) erhaelt die RecyclerView eine andere ID.
 */
public abstract class AWCursorRecyclerViewFragment extends AWLoaderFragment {
    public final static int minCardWidth = 800;
    public final int DEFAULTVIEWTYPE = 0;
    protected RecyclerView mRecyclerView;
    protected AWCursorRecyclerViewAdapter mAdapter;
    protected LayoutManager mLayoutManager;
    /**
     * Die zuletzt ausgewaehlte ID, die selektiert wurde.
     */
    protected long mSelectedID;
    protected int indexColumn;
    private int[] fromResIDs;
    /**
     * Minimale Breite fuer eine Karte mit WertpapierInformationen. Ist die Ausfloesung sehr klein,
     * wird zumindest eine Karte angezeigt - auch wenns sch... aussieht :-(
     */
    private int layout = R.layout.awlib_default_recycler_view;
    private View noEntryView;
    private AWOnCursorRecyclerViewListener onCursorRecyclerViewListener;
    private AWAbstractDBDefinition tbd;
    private int viewHolderLayout;
    private int[] viewResIDs;

    public AWCursorRecyclerViewAdapter getCursorAdapter() {
        return new AWCursorRecyclerViewAdapter(this);
    }

    /**
     * @param cursor
     *         Cursor
     * @param position
     *         aktuelle position in RecyclerView
     *
     * @return Liefert als ViewType {@link AWCursorRecyclerViewFragment#DEFAULTVIEWTYPE} zurueck
     */
    public int getItemViewType(Cursor cursor, int position) {
        return DEFAULTVIEWTYPE;
    }

    /**
     * In der DefaultImplementierung wird hier ein neuer LinearLayoutManager zurueckgegeben.
     *
     * @return LinearLayoutManager
     */
    public LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    /**
     * Ermittelt die aktuell angezeigte Position der RecyclerView
     *
     * @return Position des aktuellen Items der RecyclerView
     */
    private int getRecyclerViewPosition() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        return manager.findFirstVisibleItemPosition();
    }

    /**
     * Berechnet die Anzahl der Columns anhand der Displaybreite. Dabei wird von einer Cardbreite
     * von minCardWidth ausgegangen.
     *
     * @return Anzahl der Cards, die in eine Zeile passen. Ist mindestens eins.
     */
    protected int maxRecyclerViewColumns() {
        return maxRecyclerViewColumns(minCardWidth);
    }

    /**
     * Berechnet die Anzahl der Columns anhand der Displaybreite.
     *
     * @param minCardWidth
     *         minimale Breits einer Card in dp
     *
     * @return Anzahl der Cards, die in eine Zelie passen. Ist mindestens eins.
     */
    protected int maxRecyclerViewColumns(int minCardWidth) {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int columns = width / minCardWidth;
        if (columns == 0) {
            columns = 1;
        }
        return columns;
    }

    /**
     * Activity kann (muss aber nicht) AWOnCursorRecyclerViewListener implementieren. In diesem Fall
     * wird die entsprechende Methode bei Bedarf aufgerufen.
     *
     * @see AWOnCursorRecyclerViewListener
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            onCursorRecyclerViewListener = (AWOnCursorRecyclerViewListener) activity;
        } catch (ClassCastException e) {
            // Nix tun. Activity muss keinen RecyclerListerer implementieren.
        }
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
     *
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
    public final void onBindViewHolder(AWLibViewHolder holder, int position, Cursor cursor) {
        onPreBindViewHolder(cursor, holder);
        for (int viewPosition = 0; viewPosition < viewResIDs.length; viewPosition++) {
            int viewResID = viewResIDs[viewPosition];
            View view = holder.findViewById(viewResID);
            if (!onBindView(holder, view, viewResID, cursor, viewPosition)) {
                TextView tv;
                int resID = viewResIDs[viewPosition];
                try {
                    if (!(viewPosition < fromResIDs.length)) {
                        throw new IllegalStateException(
                                "Anzahl der viewResID ist groesser als die der fromResIDs");
                    }
                    tv = (TextView) view;
                    String text = AWDBConvert
                            .convert(tbd, fromResIDs[viewPosition], cursor.getString(viewPosition));
                    tv.setText(text);
                } catch (ClassCastException e) {
                    throw new IllegalStateException(
                            "View mit ResID " + resID + " [" + getString(resID) +
                                    "] ist keine TextView und muss in bindView belegt werden.");
                }
            }
        }
    }

    /**
     * Uebernehmen der Argumente
     *
     * @see android.app.Fragment#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = args.getInt(LAYOUT);
        viewHolderLayout = args.getInt(VIEWHOLDERLAYOUT);
        viewResIDs = args.getIntArray(VIEWRESIDS);
        fromResIDs = args.getIntArray(FROMRESIDS);
        mSelectedID = args.getLong(SELECTEDVIEWHOLDERITEM, NOID);
        tbd = args.getParcelable(DBDEFINITION);
    }

    public AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View rowView = inflater.inflate(viewHolderLayout, viewGroup, false);
        return new AWLibViewHolder(rowView);
    }

    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
        indexColumn = cursor.getColumnIndexOrThrow(getString(R.string._id));
        if (cursor.getCount() == 0) {
            noEntryView.setVisibility(View.VISIBLE);
        } else {
            noEntryView.setVisibility(View.GONE);
        }
        if (mAdapter == null) {
            mAdapter = getCursorAdapter();
            mRecyclerView.setAdapter(mAdapter);
        }
        mAdapter.swapCursor(cursor); // swap the new cursor in.
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
            mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        args.putInt(LASTSELECTEDPOSITION, getRecyclerViewPosition());
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
        //        int indexID = cursor.getColumnIndexOrThrow(tbd.columnName(R.string._id));
        //        long id = cursor.getLong(indexID);
        //        boolean selected = (id == mSelectedID);
        //        holder.setSelected(selected);
    }

    /**
     * Wird vom Adapter gerufen, wenn ein Item der RecyclerView geclickt wurde. Es wird ggfs. die
     * Activity gerufen, die einen {@link AWOnCursorRecyclerViewListener} implementiert hat.
     */
    public void onRecyclerItemClick(RecyclerView recyclerView, View view, int position, long id) {
        if (onCursorRecyclerViewListener != null) {
            onCursorRecyclerViewListener
                    .onRecyclerItemClick(mRecyclerView, view, position, id, viewHolderLayout);
        }
    }

    /**
     * Wird vom Adapter gerufen, wenn ein Item der RecyclerView long-geclickt wurde.
     */
    public boolean onRecyclerItemLongClick(RecyclerView recyclerView, View view, int position,
                                           long id) {
        return onCursorRecyclerViewListener != null && onCursorRecyclerViewListener
                .onRecyclerItemLongClick(mRecyclerView, view, position, id, viewHolderLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        int position = args.getInt(LASTSELECTEDPOSITION);
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        args.putLong(SELECTEDVIEWHOLDERITEM, mSelectedID);
        super.onSaveInstanceState(outState);
    }

    /**
     * Folgende Aktivitaeten:
     * <p/>
     * Ermitteln der ViewGroup, in der die RecyclerView eingehaengt wird. Muss die id 'recyclerView'
     * enthalten.
     * <p/>
     * Setzen eines OnScrollListeners auf die RecyclerView. Soll ein eigener OnScrollListener
     * gelten, muss MonMaRecyclerView.OnScrollListener ueberschrieben werden.
     * <p/>
     * Ermitteln des LayoutManagers. Default: LinearLayoutManager.
     * <p/>
     * Setzen des Tag der RecyclerView: SimpleClassName der Klasse, die von MonMaRecyclerView erbt.
     * <p/>
     * Setzen der ID der RecyclerView: Default ist die layout-ID, kann durch setzen von VIEWID in
     * args uebersteuert werden.
     * <p/>
     * Scroll zur zuletzt gewaehlten Position der RecyclerView.
     *
     * @see android.app.ListFragment#onViewCreated(View, Bundle)
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.awlib_defaultRecyclerView);
        mRecyclerView.setTag(this.getClass().getSimpleName());
        // Setzen der RecyclerView-ID. Standard: layout-Value. Alternativ:
        // args(VIEWID).
        int mRecyclerViewID = args.getInt(VIEWID, layout);
        mRecyclerView.setId(mRecyclerViewID);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = getLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = getCursorAdapter();
        mRecyclerView.setAdapter(mAdapter);
        noEntryView = view.findViewById(R.id.awlib_tvNoEntries);
        getActivity().getWindow()
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }
}