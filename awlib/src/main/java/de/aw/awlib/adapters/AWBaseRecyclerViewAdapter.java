package de.aw.awlib.adapters;

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

import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.recyclerview.AWBaseRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;
import de.aw.awlib.recyclerview.AWSimpleItemTouchHelperCallback;

import static android.support.v7.widget.RecyclerView.NO_ID;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.support.v7.widget.RecyclerView.OnScrollListener;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;

/**
 * Basis-Adapter fuer RecyclerView. Unterstuetzt Swipe und Drag.
 */
public abstract class AWBaseRecyclerViewAdapter extends RecyclerView.Adapter<AWLibViewHolder>
        implements AWLibViewHolder.OnClickListener, AWLibViewHolder.OnLongClickListener {
    public static final int UNDODELETEVIEW = -1;
    protected final int viewHolderLayout;
    private final AWBaseRecyclerViewFragment mBinder;
    private RecyclerView mRecyclerView;
    private int mPendingDeleteItemPosition = NO_POSITION;
    private int mTextResID = R.string.tvGeloescht;
    private AWOnScrollListener mOnScrollListener;
    private AdapterDataObserver mOnDataAchangeListener;
    private int removed;
    private SparseIntArray mItemPositions = new SparseIntArray();
    private AWSimpleItemTouchHelperCallback callbackTouchHelper;
    private ItemTouchHelper mTouchHelper;
    private int onTouchStartDragResID = -1;
    private OnDragListener mOnDragListener;
    private OnSwipeListener mOnSwipeListener;

    /**
     * Initialisiert Adapter.
     *
     * @param binder
     *         Binder fuer onBindView
     * @param viewHolderLayout
     *         Layout der ItemView
     */
    public AWBaseRecyclerViewAdapter(AWBaseRecyclerViewFragment binder, int viewHolderLayout) {
        this.viewHolderLayout = viewHolderLayout;
        mBinder = binder;
    }

    /**
     * Wird gerufen, wenn es sich beim erstellten ViewHolder nicht um eine DeleteView handelt.
     *
     * @param viewHolder
     *         viewHolder
     * @param position
     *         Position
     */
    protected void bindTheViewHolder(final AWLibViewHolder viewHolder, int position) {
        mBinder.onBindViewHolder(viewHolder, position);
    }


    private void configure() {
        if (callbackTouchHelper == null) {
            callbackTouchHelper = new AWSimpleItemTouchHelperCallback(this);
        }
        callbackTouchHelper.setIsSwipeable(mOnSwipeListener != null);
        callbackTouchHelper.setIsDragable(mOnDragListener != null);
        mTouchHelper = new ItemTouchHelper(callbackTouchHelper);
    }

    /**
     * Convertiert die Position in der RecyclerView in die Psoition im Adapter unter
     * Beruecksichtigung verschobener und geloeschter Items
     *
     * @param position
     *         Position in der RecyclerView
     * @return Position im Adapter
     */
    private int convertItemPosition(int position) {
        int mPosition = getAdapterPosition(position);
        return mItemPositions.get(mPosition, mPosition);
    }

    /**
     * @return Liefert die Anzahl der im Adapter vorhandenen Items
     */
    protected abstract int getAdapterCount();

    /**
     * Liefert die ID des Items im Adapter
     *
     * @param position
     *         Position im Adapter
     * @return {@link RecyclerView#NO_ID}
     */
    protected long getAdapterItemID(int position) {
        return NO_ID;
    }

    /**
     * @param position
     *         Position in der RecyclerView
     * @return zur Position der RecyclerView die Position im Adapter unter Beruecksichtigung der
     * geloeschten Items
     */
    private int getAdapterPosition(int position) {
        int mPosition = position;
        for (int index = 0; index < mItemPositions.size(); index++) {
            int key = mItemPositions.keyAt(index);
            if (key <= mPosition) {
                int value = mItemPositions.get(key);
                if (value == NO_POSITION) {
                    mPosition++;
                }
            }
        }
        return mPosition;
    }

    /**
     * @return Anzahl der im Adapter vorhandenen Items abzueglich der bereits entfernten Items
     */
    @Override
    public final int getItemCount() {
        int count = getAdapterCount() - removed;
        return count < 0 ? 0 : count;
    }

    /**
     * @return Liste der IDs der Items, die nach remove bzw. drag noch vorhanden ist.
     */
    public List<Long> getItemIDs() {
        int size = getItemCount();
        List<Long> mItemIDList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            mItemIDList.add(getItemId(i));
        }
        return mItemIDList;
    }

    /**
     * @param position
     *         Position in der RecyclerView
     * @return Position im Adapter unter Beruecksichtigung geloeschter und verschobener Items
     */
    @Override
    public final long getItemId(int position) {
        return getAdapterItemID(convertItemPosition(position));
    }

    @Override
    public final int getItemViewType(int position) {
        if (mPendingDeleteItemPosition == position) {
            return UNDODELETEVIEW;
        }
        return getViewType(position);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Liefert den Typ der View zu eine Position im Adapter
     *
     * @param position
     *         Position im Adapter
     * @return Typ der View. Siehe {@link RecyclerView.Adapter#getItemViewType}
     */
    public int getViewType(int position) {
        return super.getItemViewType(position);
    }

    public boolean isViewEnabled(RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @CallSuper
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        if (mTouchHelper != null) {
            mTouchHelper.attachToRecyclerView(mRecyclerView);
        }
        mOnScrollListener = new AWOnScrollListener();
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mOnDataAchangeListener = new AdapterDataObserver() {
            @Override
            public void onChanged() {
                removed = 0;
                mItemPositions.clear();
                super.onChanged();
            }
        };
        registerAdapterDataObserver(mOnDataAchangeListener);
    }

    @Override
    public final void onBindViewHolder(final AWLibViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case UNDODELETEVIEW:
                View view = holder.itemView.findViewById(R.id.llUndo);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int mPosition = mPendingDeleteItemPosition;
                        mPendingDeleteItemPosition = NO_POSITION;
                        notifyItemChanged(mPosition);
                    }
                });
                TextView tv = (TextView) holder.findViewById(R.id.tvGeloescht);
                tv.setText(mTextResID);
                view = holder.findViewById(R.id.llGeloescht);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemDismiss(mPendingDeleteItemPosition);
                    }
                });
                break;
            default:
                if (onTouchStartDragResID != -1) {
                    holder.itemView.setHapticFeedbackEnabled(true);
                    View handleView = holder.findViewById(onTouchStartDragResID);
                    handleView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (MotionEventCompat.getActionMasked(event) ==
                                    MotionEvent.ACTION_DOWN) {
                                AWBaseRecyclerViewAdapter.this.onStartDrag(holder);
                            }
                            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                                AWBaseRecyclerViewAdapter.this.onStopDrag(holder);
                            }
                            return false;
                        }
                    });
                }
                bindTheViewHolder(holder, convertItemPosition(position));
        }
    }

    @Override
    public void onClick(AWLibViewHolder holder) {
        switch (holder.getItemViewType()) {
            case UNDODELETEVIEW:
                break;
            default:
                View v = holder.getView();
                long id = mRecyclerView.getChildItemId(v);
                int position = mRecyclerView.getChildAdapterPosition(holder.itemView);
                mBinder.onRecyclerItemClick(v, position, id);
        }
    }

    @Override
    public AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View rowView;
        switch (itemType) {
            case UNDODELETEVIEW:
                rowView = inflater.inflate(R.layout.can_undo_view, viewGroup, false);
                break;
            default:
                rowView = inflater.inflate(viewHolderLayout, viewGroup, false);
        }
        AWLibViewHolder holder = new AWLibViewHolder(rowView);
        holder.setOnClickListener(this);
        holder.setOnLongClickListener(this);
        return holder;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mPendingDeleteItemPosition != NO_POSITION) {
            mItemPositions.put(getAdapterPosition(mPendingDeleteItemPosition), NO_POSITION);
        }
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mRecyclerView = null;
        unregisterAdapterDataObserver(mOnDataAchangeListener);
    }

    public void onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder from,
                          RecyclerView.ViewHolder to) {
        onItemMove(from.getAdapterPosition(), to.getAdapterPosition());
        mOnDragListener.onDragged(recyclerView, from, to);
    }

    /**
     * Entfernt ein Item an der Position. Funktioniert nur, wenn {@link
     * AWBaseRecyclerViewAdapter#setOnSwipeListener(OnSwipeListener)} mit einem SwipeListener
     * gerufen wurde.
     *
     * @param position
     *         Position des items im Adapter, das entfernt werden soll. Diese kann durch {@link
     *         AWBaseRecyclerViewAdapter#getAdapterPosition(int)} ermittelt werden.
     */
    @CallSuper
    public void onItemDismiss(int position) {
        if (position != NO_POSITION) {
            mPendingDeleteItemPosition = NO_POSITION;
            mItemPositions.put(getAdapterPosition(position), NO_POSITION);
            notifyItemRemoved(position);
            removed++;
        }
    }

    /**
     * Vertauscht zwei Items. Funktioniert nur, wenn {@link AWBaseRecyclerViewAdapter#setOnDragListener(OnDragListener)}
     * } mit einem OnDragListener gerufen wurde. Beruecksichtigt verschobene und geloeschte Items.
     *
     * @param fromPosition
     *         urspruengliche Position in der RecyclerView
     * @param toPosition
     *         Neue Position in der RecyclerView
     */
    @CallSuper
    public void onItemMove(int fromPosition, int toPosition) {
        int mFromPosition = getAdapterPosition(fromPosition);
        int mToPosition = getAdapterPosition(toPosition);
        int mFromItem = mItemPositions.get(mFromPosition, mFromPosition);
        int mToItem = mItemPositions.get(mToPosition, mToPosition);
        if (mToPosition == mFromItem) {
            mItemPositions.delete(mToPosition);
        } else {
            mItemPositions.put(mToPosition, mFromItem);
        }
        if (mFromPosition == mToItem) {
            mItemPositions.delete(mFromPosition);
        } else {
            mItemPositions.put(mFromPosition, mToItem);
        }
        notifyItemMoved(fromPosition, toPosition);
        AWApplication.Log("Item Moved. From: " + fromPosition + " To: " + toPosition);
    }

    @Override
    public boolean onLongClick(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = mRecyclerView.getChildAdapterPosition(v);
        long id = mRecyclerView.getChildItemId(v);
        return mBinder.onRecyclerItemLongClick(v, position, id);
    }

    private void onStartDrag(RecyclerView.ViewHolder holder) {
        holder.itemView.setPressed(true);
        mTouchHelper.startDrag(holder);
    }

    private void onStopDrag(AWLibViewHolder holder) {
        holder.itemView.setPressed(false);
    }

    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position, long id) {
        mOnSwipeListener.onSwiped(viewHolder, direction, position, id);
    }

    /**
     * Setzt den OnDragListener. In diesem Fall wird die RecyclerView Dragable     *
     *
     * @param listener
     *         OnDragListener
     */
    public void setOnDragListener(OnDragListener listener) {
        mOnDragListener = listener;
        configure();
    }

    /**
     * Setzt den OnSwipeListener. In diesem Fall wird die RecyclerView Swipeable
     *
     * @param listener
     *         OnSwipeListener
     */
    public void setOnSwipeListener(OnSwipeListener listener) {
        mOnSwipeListener = listener;
        configure();
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
    }

    public void setPendingDeleteItem(int position) {
        int mPending = mPendingDeleteItemPosition;
        if (mPending != NO_POSITION) {
            onItemDismiss(mPending);
        }
        if (mPending != position) {
            mPendingDeleteItemPosition = position;
            notifyItemChanged(position);
        }
    }

    public void setTextResID(@StringRes int textresID) {
        mTextResID = textresID;
    }

    public interface OnSwipeListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, final int position,
                      final long id);
    }

    public interface OnDragListener {
        void onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder from,
                       RecyclerView.ViewHolder to);
    }

    private class AWAdapterDataObserver extends AdapterDataObserver {
        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    }

    private class AWOnScrollListener extends OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case SCROLL_STATE_DRAGGING:
                    if (mPendingDeleteItemPosition != NO_POSITION) {
                        onItemDismiss(mPendingDeleteItemPosition);
                    }
                    break;
            }
            super.onScrollStateChanged(recyclerView, newState);
        }
    }
}

