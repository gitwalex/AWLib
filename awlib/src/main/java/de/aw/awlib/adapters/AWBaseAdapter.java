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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.recyclerview.AWLibViewHolder;
import de.aw.awlib.recyclerview.AWSimpleItemTouchHelperCallback;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static android.support.v7.widget.RecyclerView.OnScrollListener;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static de.aw.awlib.recyclerview.AWBaseRecyclerViewFragment.SWIPEDVIEW;

/**
 * Basis-Adapter fuer RecyclerView. Unterstuetzt Swipe und Drag.
 */
public abstract class AWBaseAdapter extends RecyclerView.Adapter<AWLibViewHolder>
        implements AWLibViewHolder.OnHolderClickListener,
        AWLibViewHolder.OnHolderLongClickListener {
    public static final int UNDODELETEVIEW = -1;
    private final AWBaseAdapterBinder mBinder;
    private RecyclerView mRecyclerView;
    private int mPendingDeleteItemPosition = NO_POSITION;
    private int mTextResID = R.string.tvGeloescht;
    private AWOnScrollListener mOnScrollListener;
    private AWSimpleItemTouchHelperCallback callbackTouchHelper;
    private ItemTouchHelper mTouchHelper;
    private int onTouchStartDragResID = -1;
    private OnDragListener mOnDragListener;
    private OnSwipeListener mOnSwipeListener;
    private int mPendingSwipeItem = NO_POSITION;

    /**
     * Initialisiert Adapter.
     *
     * @param binder
     *         Binder fuer onBindView
     */
    public AWBaseAdapter(AWBaseAdapterBinder binder) {
        mBinder = binder;
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

    @Override
    public final int getItemViewType(int position) {
        if (mPendingDeleteItemPosition == position) {
            return UNDODELETEVIEW;
        }
        if (mPendingSwipeItem == position) {
            return SWIPEDVIEW;
        }
        return getViewType(position);
    }

    protected final int getPendingDeleteItemPosition() {
        return mPendingDeleteItemPosition;
    }

    public int getPendingSwipeItem() {
        return mPendingSwipeItem;
    }

    public final RecyclerView getRecyclerView() {
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
        return mBinder.getItemViewType(position);
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
    }

    /**
     * Wird aus {@link AWBaseAdapter#onBindViewHolder(AWLibViewHolder, int)}  gerufen.
     * <p>
     * Erbende Klassen muesen pruefen, ob der ItemViewType < 0 ist, in diesem Fall wird eine View
     * gezeigt, die hier bearbeitet wurde.
     */
    @CallSuper
    @Override
    public void onBindViewHolder(final AWLibViewHolder holder, int position) {
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
                TextView tv = (TextView) holder.itemView.findViewById(R.id.tvGeloescht);
                tv.setText(mTextResID);
                view = holder.itemView.findViewById(R.id.llGeloescht);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemDismiss(mPendingDeleteItemPosition);
                        mPendingDeleteItemPosition = NO_POSITION;
                    }
                });
                break;
            default:
                if (onTouchStartDragResID != -1) {
                    holder.itemView.setHapticFeedbackEnabled(true);
                    View handleView = holder.itemView.findViewById(onTouchStartDragResID);
                    handleView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (MotionEventCompat.getActionMasked(event) ==
                                    MotionEvent.ACTION_DOWN) {
                                AWBaseAdapter.this.onStartDrag(holder);
                            }
                            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                                AWBaseAdapter.this.onStopDrag(holder);
                            }
                            return false;
                        }
                    });
                }
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
                rowView = mBinder.onCreateViewHolder(inflater, viewGroup, itemType);
        }
        AWLibViewHolder holder = new AWLibViewHolder(rowView);
        holder.setOnClickListener(this);
        holder.setOnLongClickListener(this);
        return holder;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mRecyclerView = null;
    }

    public void onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder from,
                          RecyclerView.ViewHolder to) {
        onItemMoved(from.getAdapterPosition(), to.getAdapterPosition());
        notifyItemMoved(from.getAdapterPosition(), to.getAdapterPosition());
        mOnDragListener.onDragged(recyclerView, from, to);
    }

    /**
     * Entfernt ein Item an der Position. Funktioniert nur, wenn {@link
     * AWBaseAdapter#setOnSwipeListener(OnSwipeListener)} mit einem SwipeListener gerufen wurde.
     *
     * @param position
     *         Position des items im Adapter, das entfernt werden soll.
     */
    public void onItemDismiss(int position) {
        if (position != NO_POSITION) {
            notifyItemRemoved(position);
            mBinder.onItemDismiss(getItemId(position));
        }
    }

    protected void onItemMoved(int fromPosition, int toPosition) {
        mBinder.onItemMoved(getItemId(fromPosition), getItemId(toPosition));
        AWApplication.Log("Item Moved. From: " + fromPosition + " To: " + toPosition);
    }

    private void onStartDrag(RecyclerView.ViewHolder holder) {
        holder.itemView.setPressed(true);
        mTouchHelper.startDrag(holder);
    }

    private void onStopDrag(AWLibViewHolder holder) {
        holder.itemView.setPressed(false);
    }

    public void onSwiped(AWLibViewHolder viewHolder, int direction, int position, long id) {
        mOnSwipeListener.onSwiped(viewHolder, direction, position, id);
    }

    @Override
    public final void onViewHolderClick(AWLibViewHolder holder) {
        switch (holder.getItemViewType()) {
            case UNDODELETEVIEW:
                break;
            default:
                onViewHolderClicked(holder);
        }
    }

    protected abstract void onViewHolderClicked(AWLibViewHolder holder);

    @Override
    public final boolean onViewHolderLongClick(AWLibViewHolder holder) {
        return onViewHolderLongClicked(holder);
    }

    protected abstract boolean onViewHolderLongClicked(AWLibViewHolder holder);

    public void removePendingSwipeItem() {
        int position = mPendingSwipeItem;
        mPendingSwipeItem = NO_POSITION;
        notifyItemChanged(position);
    }

    /**
     * Setzt den OnDragListener. In diesem Fall wird die RecyclerView Dragable     *
     *
     * @param listener
     *         OnDragListener
     */
    public final void setOnDragListener(OnDragListener listener) {
        mOnDragListener = listener;
        configure();
    }

    /**
     * Setzt den OnSwipeListener. In diesem Fall wird die RecyclerView Swipeable
     *
     * @param listener
     *         OnSwipeListener
     */
    public final void setOnSwipeListener(OnSwipeListener listener) {
        mOnSwipeListener = listener;
        configure();
    }

    /**
     * Durch setzen der resID der DetailView wird diese View als OneToch-Draghandler benutzt, d.h.
     * dass bei einmaligen beruehren dieses Items der Drag/Drop-Vorgang startet. Die resID muss in
     * onCreate() gesetzt werden.
     *
     * @param resID
     *         resID der View, bei deren Beruehrung der Drag/Drop Vorgand starten soll
     */
    public final void setOnTouchStartDragResID(@IdRes int resID) {
        this.onTouchStartDragResID = resID;
    }

    public final void setPendingDeleteItem(int position) {
        int mPending = mPendingDeleteItemPosition;
        if (mPendingDeleteItemPosition != NO_POSITION) {
            onItemDismiss(mPending);
            mPendingDeleteItemPosition = NO_POSITION;
        }
        if (mPending != position) {
            mPendingDeleteItemPosition = position;
            notifyItemChanged(position);
        }
    }

    public void setPendingSwipeItem(int position) {
        if (mPendingSwipeItem != NO_POSITION) {
            removePendingSwipeItem();
        }
        this.mPendingSwipeItem = position;
        notifyItemChanged(position);
    }

    public final void setTextResID(@StringRes int textresID) {
        mTextResID = textresID;
    }

    public interface OnSwipeListener {
        void onSwiped(AWLibViewHolder viewHolder, int direction, final int position, final long id);
    }

    public interface OnDragListener {
        void onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder from,
                       RecyclerView.ViewHolder to);
    }

    public interface AWBaseAdapterBinder {
        int getItemViewType(int position);

        View onCreateViewHolder(LayoutInflater inflater, ViewGroup viewGroup, int itemType);

        void onItemDismiss(long itemId);

        void onItemMoved(long fromID, long toID);
    }

    private class AWOnScrollListener extends OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case SCROLL_STATE_DRAGGING:
                    if (mPendingDeleteItemPosition != NO_POSITION) {
                        onItemDismiss(mPendingDeleteItemPosition);
                        mPendingDeleteItemPosition = NO_POSITION;
                        removePendingSwipeItem();
                    }
                    break;
            }
            super.onScrollStateChanged(recyclerView, newState);
        }
    }
}

