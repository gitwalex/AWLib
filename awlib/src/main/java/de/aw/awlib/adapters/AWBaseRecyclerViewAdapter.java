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
import android.support.annotation.StringRes;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
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
    private static final int UNDODELETEVIEW = -1;
    protected final int viewHolderLayout;
    private final AWBaseRecyclerViewFragment mBinder;
    private RecyclerView mRecyclerView;
    private int mPendingDeleteItemPosition = NO_POSITION;
    private int mTextResID = R.string.tvGeloescht;
    private AWOnScrollListener mOnScrollListener;
    private AdapterDataObserver mOnDataAchangeListener;
    private int removed;
    private SparseIntArray mItemPositions = new SparseIntArray();

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
     * Liefet zur Position die Position im Adapter unter Beruecksichtigung der geloeschten Items
     *
     * @param position
     *         Position in der RecyclerView
     * @return die Position im Adapter
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
     * @return Liefert die Anzahl der im Adapter vorhandenen Items abzueglich der bereits entfernten
     * Items
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
     * Ermittelt zur Position in der RecyclerView die Position im Adapter unter beruecksichtigung
     * geloeschter und verschoberner Items
     *
     * @param position
     *         Position in der RecyclerView
     * @return Position im Adapter
     */
    @Override
    public final long getItemId(int position) {
        return getAdapterItemID(convertItemPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (mPendingDeleteItemPosition == position) {
            return UNDODELETEVIEW;
        }
        return super.getItemViewType(position);
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
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
    public final void onBindViewHolder(final AWLibViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case UNDODELETEVIEW:
                mBinder.onBindPendingDeleteViewHolder(viewHolder);
                View view = viewHolder.itemView.findViewById(R.id.tvDoUndo);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int mPosition = mPendingDeleteItemPosition;
                        mPendingDeleteItemPosition = NO_POSITION;
                        notifyItemChanged(mPosition);
                    }
                });
                TextView tv = (TextView) viewHolder.findViewById(R.id.tvGeloescht);
                tv.setText(mTextResID);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemDismiss(mPendingDeleteItemPosition);
                    }
                });
                break;
            default:
                bindTheViewHolder(viewHolder, convertItemPosition(position));
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
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mRecyclerView = null;
        unregisterAdapterDataObserver(mOnDataAchangeListener);
    }

    /**
     * Entfernt ein Item an der Position. Funktioniert nur, wenn {@link
     * AWSimpleItemTouchHelperCallback#setIsSwipeable(boolean)} mit true gerufen wurde.
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
     * Vertauscht zwei Items. Funktioniert nur, wenn {@link AWSimpleItemTouchHelperCallback#setIsDragable(boolean)}
     * mit true gerufen wurde. Beruecksichtigt verschobene und geloeschte Items.
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

    public void setPendingDeleteItem(int position) {
        if (mPendingDeleteItemPosition != NO_POSITION) {
            onItemDismiss(mPendingDeleteItemPosition);
        }
        mPendingDeleteItemPosition = position;
        notifyItemChanged(position);
    }

    public void setTextResID(@StringRes int textresID) {
        mTextResID = textresID;
    }

    public static class Item {
    }

    public class SortedItemList extends SortedList<Item> {
        public SortedItemList(Callback<Item> callback) {
            super(Item.class, callback);
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

