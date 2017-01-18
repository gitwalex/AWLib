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

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
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

import static de.aw.awlib.activities.AWInterface.NOID;

/**
 * Basis-Adapter fuer RecyclerView. Unterstuetzt Swipe und Drag.
 */
public abstract class AWBaseRecyclerViewAdapter extends RecyclerView.Adapter<AWLibViewHolder>
        implements AWLibViewHolder.OnClickListener, AWLibViewHolder.OnLongClickListener {
    public static final int UNDODELETEVIEW = -1;
    protected final int viewHolderLayout;
    private final AWBaseRecyclerViewFragment mBinder;
    private RecyclerView mRecyclerView;
    private SparseArray<PendingDeleteItemWorker> mPendingDeleteItems = new SparseArray<>();
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
        if (mPendingDeleteItems.get(position) != null) {
            return UNDODELETEVIEW;
        }
        return super.getItemViewType(position);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public int getRemoved() {
        return removed;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public final void onBindViewHolder(final AWLibViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case UNDODELETEVIEW:
                final PendingDeleteItemWorker mPendingDeleteItem =
                        mPendingDeleteItems.get(position);
                if (mPendingDeleteItem != null) {
                    mBinder.onBindPendingDeleteViewHolder(viewHolder);
                    View view = viewHolder.itemView.findViewById(R.id.tvDoUndo);
                    final Runnable mRunnable = mPendingDeleteItem.getRunnable();
                    view.postDelayed(mRunnable, 10000);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.removeCallbacks(mRunnable);
                            int mPosition = viewHolder.getAdapterPosition();
                            mPendingDeleteItems.delete(mPosition);
                            notifyItemChanged(mPosition);
                        }
                    });
                    TextView tv = (TextView) viewHolder.findViewById(R.id.tvGeloescht);
                    if (mPendingDeleteItem.getTextResID() != NOID) {
                        tv.setText(mPendingDeleteItem.getTextResID());
                    }
                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.post(mPendingDeleteItem.getRunnable());
                            int mPosition = viewHolder.getAdapterPosition();
                            mPendingDeleteItems.delete(mPosition);
                            notifyItemChanged(mPosition);
                        }
                    });
                }
                break;
            default:
                bindTheViewHolder(viewHolder, position);
        }
    }

    @Override
    public void onClick(AWLibViewHolder holder) {
        int position = mRecyclerView.getChildAdapterPosition(holder.itemView);
        switch (holder.getItemViewType()) {
            case UNDODELETEVIEW:
                break;
            default:
                View v = holder.getView();
                long id = mRecyclerView.getChildItemId(v);
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
        mRecyclerView = null;
    }

    /**
     * Entfernt ein Item an der Position. Funktioniert nur, wenn {@link
     * AWSimpleItemTouchHelperCallback#setIsSwipeable(boolean)} mit true gerufen wurde.
     *
     * @param position
     *         Position des items im Adapter, das entfernt werden soll
     */
    public void onItemDismiss(int position) {
        removed++;
        notifyItemRemoved(position);
    }

    /**
     * Vertauscht zwei Items. Funktioniert nur, wenn {@link AWSimpleItemTouchHelperCallback#setIsDragable(boolean)}
     * mit true gerufen wurde.
     *
     * @param fromPosition
     *         urspruengliche Position
     * @param toPosition
     *         Neue Position
     * @return Immer true
     */
    public boolean onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        AWApplication.Log("Item Moved. From: " + fromPosition + " To: " + toPosition);
        return true;
    }

    @Override
    public boolean onLongClick(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = mRecyclerView.getChildAdapterPosition(v);
        long id = mRecyclerView.getChildItemId(v);
        return mBinder.onRecyclerItemLongClick(v, position, id);
    }

    public void setPendingDeleteItem(PendingDeleteItemWorker worker) {
        mPendingDeleteItems.put(worker.getPosition(), worker);
    }

    public static class PendingDeleteItemWorker {
        private final int mPosition;
        private final Runnable mRunnable;
        private int textResID = NOID;

        public PendingDeleteItemWorker(int position, Runnable runnable) {
            mPosition = position;
            mRunnable = runnable;
        }

        public int getPosition() {
            return mPosition;
        }

        public Runnable getRunnable() {
            return mRunnable;
        }

        public int getTextResID() {
            return textResID;
        }

        public void setTextResID(int textResID) {
            this.textResID = textResID;
        }
    }
}

