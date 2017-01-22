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
    private AdapterDataChangedObserver mDataChangedObserver;
    private int mTextResID = R.string.tvGeloescht;
    private AWOnScrollListener mOnScrollListener;
    private SparseIntArray mItemIDs = new SparseIntArray();
    private AdapterDataObserver mOnDataAchangeListener;
    private int removed;

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

    protected abstract int getAdapterCount();

    protected final int getAdapterPosition(int position) {
        int mPosition = position;
        for (int index = 0; index < mItemIDs.size(); index++) {
            int key = mItemIDs.keyAt(index);
            if (mPosition >= key) {
                if (mItemIDs.valueAt(index) == NO_POSITION) {
                    mPosition++;
                }
            }
        }
        int value = mItemIDs.get(mPosition, NO_POSITION);
        if (value != NO_POSITION) {
            mPosition = value;
        }
        return mPosition;
    }

    @Override
    public final int getItemCount() {
        return getAdapterCount() - removed;
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
    public int getItemViewType(int position) {
        if (mPendingDeleteItemPosition == position) {
            return UNDODELETEVIEW;
        }
        return super.getItemViewType(position);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
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
                mItemIDs.clear();
                removed = 0;
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
                        mPendingDeleteItemPosition = NO_POSITION;
                        int mPosition = viewHolder.getAdapterPosition();
                        notifyItemChanged(mPosition);
                    }
                });
                TextView tv = (TextView) viewHolder.findViewById(R.id.tvGeloescht);
                tv.setText(mTextResID);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int mPosition = viewHolder.getAdapterPosition();
                        onItemDismiss(mPosition);
                    }
                });
                break;
            default:
                bindTheViewHolder(viewHolder, position);
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
     *         Position des items im Adapter, das entfernt werden soll
     */
    @CallSuper
    public void onItemDismiss(int position) {
        if (position != NO_POSITION) {
            if (mPendingDeleteItemPosition == position && mDataChangedObserver != null) {
                long mID = getItemId(position);
                mDataChangedObserver.onItemRemoved(mID);
            }
            mItemIDs.put(getAdapterPosition(position), NO_POSITION);
            removed++;
            notifyItemRemoved(position);
            mPendingDeleteItemPosition = NO_POSITION;
        }
    }

    /**
     * Vertauscht zwei Items. Funktioniert nur, wenn {@link AWSimpleItemTouchHelperCallback#setIsDragable(boolean)}
     * mit true gerufen wurde.
     *
     * @param fromPosition
     *         urspruengliche Position
     * @param toPosition
     *         Neue Position
     */
    @CallSuper
    public void onItemMove(int fromPosition, int toPosition) {
        mItemIDs.put(fromPosition, toPosition);
        mItemIDs.put(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        AWApplication.Log("Item Moved. From: " + fromPosition + " To: " + toPosition);
        if (mDataChangedObserver != null) {
            mDataChangedObserver.onItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public boolean onLongClick(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = mRecyclerView.getChildAdapterPosition(v);
        long id = mRecyclerView.getChildItemId(v);
        return mBinder.onRecyclerItemLongClick(v, position, id);
    }

    public void setAdapterDataChangedObserver(AdapterDataChangedObserver observer) {
        mDataChangedObserver = observer;
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

    public interface AdapterDataChangedObserver {
        void onItemMoved(int fromPosition, int toPosition);

        void onItemRemoved(long id);
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

