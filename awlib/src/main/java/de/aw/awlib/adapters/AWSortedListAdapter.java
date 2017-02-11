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

import android.database.Cursor;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import de.aw.awlib.databinding.SortedListModel;
import de.aw.awlib.recyclerview.AWLibViewHolder;
import de.aw.awlib.recyclerview.AWSortedListRecyclerViewFragment;

/**
 * Created by alex on 29.01.2017.
 */
public class AWSortedListAdapter<T extends SortedListModel<T>> extends AWBaseAdapter {
    private final SortedItemList sortedItemList;
    private final AWSortedListRecyclerViewFragment<T> mBinder;
    private ItemGenerator<T> mItemgenerator;
    private int mCount;

    public AWSortedListAdapter(@NonNull Class<T> clazz,
                               @NonNull AWSortedListRecyclerViewFragment<T> binder) {
        super(binder);
        mBinder = binder;
        sortedItemList = new SortedItemList(clazz);
    }

    public final int add(T item) {
        return sortedItemList.add(item);
    }

    public final void addAll(Cursor cursor, ItemGenerator<T> generator) {
        mItemgenerator = generator;
        mCount = cursor.getCount();
        int newSize = mCount > 20 ? 20 : mCount;
        for (int i = sortedItemList.size(); i < newSize; i++) {
            sortedItemList.add(generator.createItem(i));
        }
    }

    public final void addAll(List<T> items) {
        sortedItemList.beginBatchedUpdates();
        for (T item : items) {
            sortedItemList.add(item);
        }
        sortedItemList.endBatchedUpdates();
    }

    public final void addAll(T[] items) {
        addAll(Arrays.asList(items));
    }

    public final void addItem(T item) {
        sortedItemList.add(item);
    }

    private boolean areContentsTheSame(T oldItem, T newItem) {
        return oldItem.areContentsTheSame(newItem);
    }

    private boolean areItemsTheSame(T item1, T item2) {
        return item1.areItemsTheSame(item2);
    }

    public void clear() {
        sortedItemList.clear();
    }

    private int compare(T o1, T o2) {
        return o1.compare(o2);
    }

    public final T get(int position) {
        return sortedItemList.get(position);
    }

    @Override
    public final int getItemCount() {
        if (mItemgenerator != null) {
            return mCount;
        }
        return sortedItemList.size();
    }

    @Override
    public final long getItemId(int position) {
        return sortedItemList.get(position).getID();
    }

    public final int indexOf(T item) {
        return sortedItemList.indexOf(item);
    }

    @Override
    public final void onBindViewHolder(AWLibViewHolder holder, int position) {
        if (sortedItemList.size() < position + 1) {
            int newSize = position + 20 > mCount ? position + 20 : mCount;
            for (int i = sortedItemList.size(); i < newSize; i++) {
                sortedItemList.add(mItemgenerator.createItem(i));
            }
        }
        super.onBindViewHolder(holder, position);
    }

    @CallSuper
    @Override
    public void onItemDismiss(int position) {
        sortedItemList.removeItemAt(position);
    }

    @Override
    protected void onItemMove(int fromPosition, int toPosition) {
    }

    @Override
    protected void onViewHolderClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(holder.itemView);
        T item = sortedItemList.get(position);
        mBinder.onRecyclerItemClick(v, position, item);
    }

    @Override
    protected boolean onViewHolderLongClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(v);
        T item = sortedItemList.get(position);
        return mBinder.onRecyclerItemLongClick(v, position, item);
    }

    public final boolean remove(T item) {
        return sortedItemList.remove(item);
    }

    public final T removeItemAt(int index) {
        return sortedItemList.removeItemAt(index);
    }

    public final void updateItemAt(int index, T item) {
        sortedItemList.updateItemAt(index, item);
    }

    public interface ItemGenerator<T> {
        T createItem(int position);
    }

    private class SortedItemList extends SortedList<T> {
        SortedItemList(Class<T> klass) {
            super(klass, new MCallback());
        }

        public SortedItemList(Class<T> klass, int initialCapacity) {
            super(klass, new MCallback(), initialCapacity);
        }
    }

    private class MCallback extends SortedList.Callback<T> {
        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            return AWSortedListAdapter.this.areContentsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areItemsTheSame(T item1, T item2) {
            return AWSortedListAdapter.this.areItemsTheSame(item1, item2);
        }

        @Override
        public int compare(T o1, T o2) {
            return AWSortedListAdapter.this.compare(o1, o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            onItemMove(fromPosition, toPosition);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }
    }
}
