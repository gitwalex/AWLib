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
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.aw.awlib.R;
import de.aw.awlib.recyclerview.AWBaseRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Created by alex on 17.01.2017.
 */
public abstract class AWBaseRecyclerViewAdapter extends RecyclerView.Adapter<AWLibViewHolder>
        implements AWLibViewHolder.OnClickListener, AWLibViewHolder.OnLongClickListener {
    protected final int viewHolderLayout;
    private final AWBaseRecyclerViewFragment mBinder;
    private RecyclerView mRecyclerView;
    private LongSparseArray<Runnable> mPendingDeleteItems = new LongSparseArray<>();

    /**
     * Initialisiert Adapter.
     *
     * @param viewHolderLayout
     *         Layout der ItemView
     */
    public AWBaseRecyclerViewAdapter(AWBaseRecyclerViewFragment binder, int viewHolderLayout) {
        this.viewHolderLayout = viewHolderLayout;
        mBinder = binder;
    }

    public AWBaseRecyclerViewFragment getBinder() {
        return mBinder;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder viewHolder, int position) {
        final int mPosition = position;
        final long id = getItemId(position);
        final Runnable mRunnable = mPendingDeleteItems.get(id);
        if (mRunnable != null) {
            mBinder.onBindPendingDeleteViewHolder(viewHolder);
            View view = viewHolder.itemView.findViewById(R.id.tvDoUndo);
            view.postDelayed(mRunnable, 10000);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.removeCallbacks(mRunnable);
                    mPendingDeleteItems.delete(id);
                    notifyItemChanged(mPosition);
                }
            });
        }
        mBinder.onBindViewHolder(viewHolder, position);
    }

    @Override
    public void onClick(AWLibViewHolder holder) {
        if (mPendingDeleteItems.get(holder.getID()) == null) {
            View v = holder.getView();
            int position = mRecyclerView.getChildAdapterPosition(v);
            long id = mRecyclerView.getChildItemId(v);
            mBinder.onRecyclerItemClick(v, position, id);
        }
    }

    @Override
    public AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        final View rowView = inflater.inflate(viewHolderLayout, viewGroup, false);
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

    @Override
    public boolean onLongClick(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = mRecyclerView.getChildAdapterPosition(v);
        long id = mRecyclerView.getChildItemId(v);
        return mBinder.onRecyclerItemLongClick(v, position, id);
    }

    public void setPendingDeleteItem(long id, Runnable runnable) {
        mPendingDeleteItems.put(id, runnable);
    }
}
