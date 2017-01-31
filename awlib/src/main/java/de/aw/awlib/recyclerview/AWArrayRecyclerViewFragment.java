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

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWArrayRecyclerViewAdapter;
import de.aw.awlib.fragments.AWFragment;

/**
 * Erstellt eine Liste ueber Daten eines Arrays.
 * <p/>
 * Ein ArrayAdapter kann ueber {@link AWArrayRecyclerViewFragment#getArrayAdapter()} geliefert
 * werden.
 */
public class AWArrayRecyclerViewFragment<T> extends AWFragment
        implements AWArrayRecyclerViewAdapter.ArrayViewHolderBinder<T>, View.OnClickListener,
        View.OnLongClickListener, AWOnArrayRecyclerViewListener<T> {
    public final static int minCardWidth = 800;
    public final int DEFAULTVIEWTYPE = 0;
    protected RecyclerView mRecyclerView;
    protected AWArrayRecyclerViewAdapter<T> mAdapter;
    protected LayoutManager mLayoutManager;
    /**
     * Die zuletzt ausgewaehlte ID, die selektiert wurde.
     */
    protected long mSelectedID;
    /**
     * Minimale Breite fuer eine Karte mit WertpapierInformationen. Ist die Ausfloesung sehr klein,
     * wird zumindest eine Karte angezeigt - auch wenns sch... aussieht :-(
     */
    private int layout = R.layout.awlib_default_recycler_view;
    private AWOnArrayRecyclerViewListener<T> onArrayRecyclerViewListener;
    private int viewHolderLayout;

    public AWArrayRecyclerViewAdapter<T> getArrayAdapter() {
        return new AWArrayRecyclerViewAdapter<>(this);
    }

    /**
     * @param position
     *         Position in View
     * @param object
     *         aktuelles Object
     * @return Liefert als ViewType {@link AWArrayRecyclerViewFragment#DEFAULTVIEWTYPE} zurueck
     */
    @Override
    public int getItemViewType(int position, T object) {
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
     * Wird aus onClick(...) gerufen, wenn ein Item der RecyclerView geclickt wurde. Es wird ggfs.
     * die Activity gerufen, die einen {@link AWCursorRecyclerViewListener} implementiert hat.
     */
    @Override
    public void onArrayRecyclerItemClick(RecyclerView recyclerView, View view, T object) {
        if (onArrayRecyclerViewListener != null) {
            onArrayRecyclerViewListener.onArrayRecyclerItemClick(mRecyclerView, view, object);
        }
    }

    /**
     * Wird aus onLongClick(...) gerufen, wenn ein Item der RecyclerView long-geclickt wurde.
     */
    @Override
    public boolean onArrayRecyclerItemLongClick(RecyclerView recyclerView, View view, T object) {
        return onArrayRecyclerViewListener != null && onArrayRecyclerViewListener
                .onArrayRecyclerItemLongClick(mRecyclerView, view, object);
    }

    /**
     * Activity kann (muss aber nicht) AWCursorRecyclerViewListener implementieren. In diesem Fall
     * wird die entsprechende Methode bei Bedarf aufgerufen.
     *
     * @see AWCursorRecyclerViewListener
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            onArrayRecyclerViewListener = (AWOnArrayRecyclerViewListener) activity;
        } catch (ClassCastException e) {
            // Nix tun. Activity muss keinen RecyclerListerer implementieren.
        }
    }

    @Override
    protected final boolean onBindView(View view, int resID) {
        return super.onBindView(view, resID);
    }

    /**
     * Wird vom Adapter zum belegen der Views des holder gerufen
     *
     * @param holder
     *         ViewHolder
     * @param object
     *         Object des Arrays
     */
    @Override
    public void onBindViewHolder(AWLibViewHolder holder, T object) {
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layout = args.getInt(LAYOUT);
        viewHolderLayout = args.getInt(VIEWHOLDERLAYOUT);
        mSelectedID = args.getLong(SELECTEDVIEWHOLDERITEM, NOID);
    }

    @Override
    public AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View rowView = inflater.inflate(viewHolderLayout, viewGroup, false);
        return new AWLibViewHolder(rowView);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        args.putInt(LASTSELECTEDPOSITION, getRecyclerViewPosition());
    }

    @Override
    public void onResume() {
        super.onResume();
        int position = args.getInt(LASTSELECTEDPOSITION, 0);
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
        mAdapter = getArrayAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnRecyclerItemClickListener(this);
        mAdapter.setOnRecyclerItemLongClickListener(this);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }
}