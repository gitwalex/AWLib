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
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;

import java.util.Arrays;
import java.util.List;

import de.aw.awlib.recyclerview.AWBaseRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Created by alex on 29.01.2017.
 */
public abstract class AWSortedListRecyclerViewAdapter<T extends AWSortedListRecyclerViewAdapter.Item>
        extends AWBaseRecyclerViewAdapter {
    final SortedItemList sortedItemList;
    private ItemGenerator<T> mItemgenerator;

    public AWSortedListRecyclerViewAdapter(@NonNull Class<T> clazz,
                                           @NonNull AWBaseRecyclerViewFragment binder,
                                           int viewHolderLayout) {
        super(binder, viewHolderLayout);
        sortedItemList = new SortedItemList(clazz);
    }

    public int add(T item) {
        return sortedItemList.add(item);
    }

    public void addAll(Cursor cursor, ItemGenerator<T> generator) {
        mItemgenerator = generator;
        int newSize = getAdapterCount() > 20 ? 20 : getAdapterCount();
        for (int i = sortedItemList.size(); i < newSize; i++) {
            sortedItemList.add(generator.createItem(i));
        }
    }

    public void addAll(List<T> items) {
        sortedItemList.beginBatchedUpdates();
        for (T item : items) {
            sortedItemList.add(item);
        }
        sortedItemList.endBatchedUpdates();
    }

    public void addAll(T[] items) {
        addAll(Arrays.asList(items));
    }

    protected void addItem(T item) {
        sortedItemList.add(item);
    }

    public abstract boolean areContentsTheSame(T oldItem, T newItem);

    public abstract boolean areItemsTheSame(T item1, T item2);

    @Override
    protected void bindTheViewHolder(AWLibViewHolder viewHolder, int position) {
        if (sortedItemList.size() < position + 1) {
            int newSize = position + 20 > getAdapterCount() ? position + 20 : getAdapterCount();
            for (int i = sortedItemList.size(); i < newSize; i++) {
                sortedItemList.add(mItemgenerator.createItem(i));
            }
        }
        super.bindTheViewHolder(viewHolder, position);
    }

    public void clear() {
        sortedItemList.clear();
    }

    public abstract int compare(T o1, T o2);

    public T get(int position) {
        return sortedItemList.get(position);
    }


    @Override
    protected long getAdapterItemID(int position) {
        return sortedItemList.get(position).getID();
    }

    public int indexOf(T item) {
        return sortedItemList.indexOf(item);
    }

    public boolean remove(T item) {
        return sortedItemList.remove(item);
    }

    public T removeItemAt(int index) {
        return sortedItemList.removeItemAt(index);
    }

    public void updateItemAt(int index, T item) {
        sortedItemList.updateItemAt(index, item);
    }

    public interface ItemGenerator<T> {
        T createItem(int position);
    }

    public static abstract class Item {
        public abstract long getID();
    }

    public class SortedItemList extends SortedList<T> {
        public SortedItemList(Class<T> klass) {
            super(klass, new MCallback());
        }

        public SortedItemList(Class<T> klass, int initialCapacity) {
            super(klass, new MCallback(), initialCapacity);
        }
    }

    public class MCallback extends SortedList.Callback<T> {
        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            return AWSortedListRecyclerViewAdapter.this.areContentsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areItemsTheSame(T item1, T item2) {
            return AWSortedListRecyclerViewAdapter.this.areItemsTheSame(item1, item2);
        }

        @Override
        public int compare(T o1, T o2) {
            return AWSortedListRecyclerViewAdapter.this.compare(o1, o2);
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
            onItemDismiss(position);
            notifyItemRangeRemoved(position, count);
        }
    }
}
