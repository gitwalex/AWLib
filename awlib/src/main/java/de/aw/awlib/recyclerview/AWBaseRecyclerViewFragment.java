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
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWBaseRecyclerViewAdapter;
import de.aw.awlib.fragments.AWFragment;

/**
 * Erstellt eine Liste ueber Daten einer Tabelle.
 * <p/>
 * In der RecyclerView wird als Tag der Name der nutzenden Klasse gespeichert und damit bei
 * OnRecyclerItemClick() bzw. OnRecyclerItemLongClick() im Parent mitgeliefert.
 * <p/>
 * Als Standard erhaelt die RecyclerView als ID den Wert des Layout. Durch args.setInt(VIEWID,
 * value) erhaelt die RecyclerView eine andere ID.
 */
public abstract class AWBaseRecyclerViewFragment extends AWFragment {
    public final static int minCardWidth = 800;
    protected AWBaseRecyclerViewAdapter mAdapter;
    protected LayoutManager mLayoutManager;
    protected RecyclerView mRecyclerView;
    protected int viewHolderLayout;
    /**
     * Die zuletzt ausgewaehlte ID, die selektiert wurde.
     */
    protected long mSelectedID;
    protected View noEntryView;
    /**
     * Minimale Breite fuer eine Karte mit WertpapierInformationen. Ist die Ausfloesung sehr klein,
     * wird zumindest eine Karte angezeigt - auch wenns sch... aussieht :-(
     */
    protected int layout = R.layout.awlib_default_recycler_view;
    private int onTouchStartDragResID = -1;
    private AWBaseRecyclerViewAdapter.OnDragListener mOnDragListener;
    private AWBaseRecyclerViewAdapter.OnSwipeListener mOnSwipeListener;
    private AWCursorRecyclerViewListener mBaseRecyclerViewListener;

    /**
     * @return einen BaseAdapter
     */
    protected abstract AWBaseRecyclerViewAdapter createBaseAdapter();

    /**
     * Initialisiert den Adapter, dieser Adapter wird ggfs. neu erstellt
     *
     * @return Adapter
     */
    private AWBaseRecyclerViewAdapter getCustomAdapter() {
        if (mAdapter == null) {
            mAdapter = createBaseAdapter();
        }
        if (mOnDragListener != null) {
            mAdapter.setOnDragListener(mOnDragListener);
        }
        if (mOnSwipeListener != null) {
            mAdapter.setOnSwipeListener(mOnSwipeListener);
        }
        mAdapter.setOnTouchStartDragResID(onTouchStartDragResID);
        return mAdapter;
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
     * Activity kann (muss aber nicht) AWCursorRecyclerViewListener implementieren. In diesem Fall
     * wird die entsprechende Methode bei Bedarf aufgerufen.
     *
     * @see AWCursorRecyclerViewListener
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof AWCursorRecyclerViewListener) {
            mBaseRecyclerViewListener = (AWCursorRecyclerViewListener) activity;
        }
    }

    @Override
    protected final boolean onBindView(View view, int resID) {
        return super.onBindView(view, resID);
    }

    /**
     * @throws IllegalStateException
     *         Wenn eine View bearbeitet wird, die TextView ist und fillView(...) hat false
     *         zurueckgegeben.
     */
    @CallSuper
    public void onBindViewHolder(AWLibViewHolder holder, int position) {
        onPreBindViewHolder(holder);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        args.putInt(LASTSELECTEDPOSITION, getRecyclerViewPosition());
    }

    /**
     * Wird aus {@link AWBaseRecyclerViewFragment#onBindViewHolder(AWLibViewHolder, int)} gerufen
     */
    protected void onPreBindViewHolder(final AWLibViewHolder holder) {
    }

    /**
     * Wird vom Adapter gerufen, wenn ein Item der RecyclerView geclickt wurde. Es wird ggfs. die
     * Activity gerufen, die einen {@link AWCursorRecyclerViewListener} implementiert hat.
     */
    @CallSuper
    public void onRecyclerItemClick(View view, int position, long id) {
        mSelectedID = id;
        if (mBaseRecyclerViewListener != null) {
            mBaseRecyclerViewListener
                    .onRecyclerItemClick(mRecyclerView, view, position, id, viewHolderLayout);
        }
    }

    /**
     * Wird vom Adapter gerufen, wenn ein Item der RecyclerView long-geclickt wurde.
     */
    @CallSuper
    public boolean onRecyclerItemLongClick(View view, int position, long id) {
        mSelectedID = id;
        return mBaseRecyclerViewListener != null && mBaseRecyclerViewListener
                .onRecyclerItemLongClick(mRecyclerView, view, position, id, viewHolderLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        int position = args.getInt(LASTSELECTEDPOSITION);
        if (mAdapter == null) {
            mRecyclerView.setAdapter(getCustomAdapter());
        }
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
        mRecyclerView.setAdapter(getCustomAdapter());
        noEntryView = view.findViewById(R.id.awlib_tvNoEntries);
        getActivity().getWindow()
                     .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }

    /**
     * Setzt den OnDragListener. In diesem Fall wird die RecyclerView Dragable     *
     *
     * @param listener
     *         OnDragListener
     */
    public void setOnDragListener(AWBaseRecyclerViewAdapter.OnDragListener listener) {
        mOnDragListener = listener;
        if (mAdapter != null) {
            mAdapter.setOnDragListener(listener);
        }
    }

    /**
     * Setzt den OnSwipeListener. In diesem Fall wird die RecyclerView Swipeable
     *
     * @param listener
     *         OnSwipeListener
     */
    public void setOnSwipeListener(AWBaseRecyclerViewAdapter.OnSwipeListener listener) {
        mOnSwipeListener = listener;
        if (mAdapter != null) {
            mAdapter.setOnSwipeListener(listener);
        }
    }

    /**
     * Durch setzen der resID der DetailView wird dieses Item als OneToch-Draghandler benutzt, d.h.
     * dass bei einmaligen beruehren dieses Items der Drag/Drop-Vorgang startet. Die resID muss in
     * onCreate() gesetzt werden.
     *
     * @param resID
     *         resID der View, bei deren Beruehrung der Drag/Drop Vorgand starten soll
     */
    public void setOnTouchStartDragResID(@IdRes int resID) {
        this.onTouchStartDragResID = resID;
        if (mAdapter != null) {
            mAdapter.setOnTouchStartDragResID(resID);
        }
    }
}