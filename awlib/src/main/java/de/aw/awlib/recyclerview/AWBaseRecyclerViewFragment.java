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
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.v4.content.Loader;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWBaseRecyclerViewAdapter;
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
public abstract class AWBaseRecyclerViewFragment extends AWLoaderFragment {
    public static final int DEFAULTVIEWTYPE = 0;
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
    private int layout = R.layout.awlib_default_recycler_view;
    private AWBaseRecyclerViewListener mBaseRecyclerViewListener;
    private AWSimpleItemTouchHelperCallback callbackTouchHelper;
    private boolean isDragable;
    private boolean isSwipeable;
    private ItemTouchHelper mTouchHelper;
    private int oneTouchStartDragResID = -1;
    private boolean canUndoSwipe;

    protected void configure(AWBaseRecyclerViewAdapter mAdapter) {
        callbackTouchHelper = getItemTouchCallback(mAdapter);
        if (callbackTouchHelper != null) {
            callbackTouchHelper.setIsDragable(isDragable);
            callbackTouchHelper.setIsSwipeable(isSwipeable);
            callbackTouchHelper.setCanUndoSwipe(canUndoSwipe);
            mTouchHelper = new ItemTouchHelper(callbackTouchHelper);
            mTouchHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    protected abstract AWBaseRecyclerViewAdapter getBaseAdapter();

    private AWBaseRecyclerViewAdapter getCustomAdapter() {
        if (mAdapter == null) {
            mAdapter = getBaseAdapter();
        }
        configure(mAdapter);
        return mAdapter;
    }

    protected AWSimpleItemTouchHelperCallback getItemTouchCallback(
            AWBaseRecyclerViewAdapter mAdapter) {
        return null;
    }

    /**
     * @param position
     *         aktuelle position in RecyclerView
     * @return Liefert als ViewType {@link AWBaseRecyclerViewFragment#DEFAULTVIEWTYPE} zurueck
     */
    public int getItemViewType(int position) {
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
     * Activity kann (muss aber nicht) AWBaseRecyclerViewListener implementieren. In diesem Fall
     * wird die entsprechende Methode bei Bedarf aufgerufen.
     *
     * @see AWBaseRecyclerViewListener
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mBaseRecyclerViewListener = (AWBaseRecyclerViewListener) activity;
        } catch (ClassCastException e) {
            // Nix tun. Activity muss keinen RecyclerListerer implementieren.
        }
    }

    public void onBindPendingDeleteViewHolder(AWLibViewHolder viewHolder) {
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

    /**
     * Ist der Adapter == null, wird ein neuer erstellt und konfiguriert
     */
    @CallSuper
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mAdapter == null) {
            mRecyclerView.setAdapter(getCustomAdapter());
        }
        super.onLoadFinished(loader, cursor);
    }

    @Override
    public void onPause() {
        super.onPause();
        args.putInt(LASTSELECTEDPOSITION, getRecyclerViewPosition());
    }

    /**
     * Ist mittels {@link AWBaseRecyclerViewFragment#setOneTouchStartDragResID(int)} eine resID
     * einer View der Detailview gesetzt worden, wird diese als startDrag-Event konfiguriert.
     *
     * @throws NullPointerException
     *         wenn es keine View mit dieer resID gibt
     */
    @CallSuper
    protected void onPreBindViewHolder(final AWLibViewHolder holder) {
        View handleView;
        holder.itemView.setHapticFeedbackEnabled(true);
        if (oneTouchStartDragResID != -1) {
            handleView = holder.findViewById(oneTouchStartDragResID);
            handleView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        AWBaseRecyclerViewFragment.this.onStartDrag(holder);
                    }
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                        AWBaseRecyclerViewFragment.this.onStopDrag(holder);
                    }
                    return false;
                }
            });
        }
    }

    /**
     * Wird vom Adapter gerufen, wenn ein Item der RecyclerView geclickt wurde. Es wird ggfs. die
     * Activity gerufen, die einen {@link AWBaseRecyclerViewListener} implementiert hat.
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
        noEntryView.setVisibility(View.VISIBLE);
        if (mAdapter == null) {
            mRecyclerView.setAdapter(getCustomAdapter());
        }
        if (mAdapter.getItemCount() > 0) {
            noEntryView.setVisibility(View.GONE);
        }
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        args.putLong(SELECTEDVIEWHOLDERITEM, mSelectedID);
        super.onSaveInstanceState(outState);
    }

    public void onStartDrag(RecyclerView.ViewHolder holder) {
        holder.itemView.setPressed(true);
        mTouchHelper.startDrag(holder);
    }

    public void onStopDrag(AWLibViewHolder holder) {
        holder.itemView.setPressed(false);
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

    public void setCanUndoSwipe(boolean canUndoSwipe) {
        this.canUndoSwipe = canUndoSwipe;
        if (callbackTouchHelper != null) {
            callbackTouchHelper.setCanUndoSwipe(canUndoSwipe);
        }
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }

    /**
     * Konfiguration, ob die RecyclerView Draggable ist
     *
     * @param isDragable
     *         true: ist Draggable
     */
    public void setIsDragable(boolean isDragable) {
        this.isDragable = isDragable;
        if (callbackTouchHelper != null) {
            callbackTouchHelper.setIsDragable(isDragable);
        }
    }

    /**
     * Konfiguration, ob die RecyclerView Swipeable ist
     *
     * @param isSwipeable
     *         true: ist Swipeable
     */
    public void setIsSwipeable(boolean isSwipeable) {
        this.isSwipeable = isSwipeable;
        if (callbackTouchHelper != null) {
            callbackTouchHelper.setIsDragable(isSwipeable);
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
    public void setOneTouchStartDragResID(@IdRes int resID) {
        this.oneTouchStartDragResID = resID;
    }
}