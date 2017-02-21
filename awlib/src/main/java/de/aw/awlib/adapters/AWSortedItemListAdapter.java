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

import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Adapter mit einer {@link SortedList}. Dieser Adapter ist Swipeable, aber nicht Dragable
 */
public abstract class AWSortedItemListAdapter<T> extends AWItemListAdapterTemplate<T> {
    private final SortedList<T> sortedItemList;
    private final SortedList<T> removedSortedItemList;
    private ItemGenerator<T> mItemgenerator;
    private int mCount;

    public AWSortedItemListAdapter(@NonNull Class<T> clazz,
                                   @NonNull AWListAdapterBinder<T> binder) {
        super(binder);
        sortedItemList = new SortedList<>(clazz, new MCallback());
        removedSortedItemList = new SortedList<>(clazz, null);
    }

    /**
     * @see SortedList#add(Object)
     */
    @Override
    public final int add(T item) {
        return sortedItemList.add(item);
    }

    @Override
    public final void addAll(Cursor cursor, ItemGenerator<T> generator) {
        mItemgenerator = generator;
        mCount = cursor.getCount();
        int newSize = mCount > 20 ? 20 : mCount;
        sortedItemList.beginBatchedUpdates();
        for (int i = sortedItemList.size(); i < newSize; i++) {
            add(generator.createItem(i));
        }
        sortedItemList.endBatchedUpdates();
    }

    @Override
    public final void addAll(List<T> items) {
        sortedItemList.beginBatchedUpdates();
        for (T item : items) {
            add(item);
        }
        sortedItemList.endBatchedUpdates();
    }

    @Override
    public final void addAll(T[] items) {
        addAll(Arrays.asList(items));
    }

    /**
     * Wird aus dem Adapter gerufen, wenn {@link AWSortedItemListAdapter#areItemsTheSame(Object,
     * Object)} true zuruckgegeben hat. Dann kann hier angegeben werden, ob nicht nur die
     * Suchkritieren identisch sind, sindern auch der Inhalt.
     *
     * @param other
     *         das zu vergleichende Item
     * @return true, wenn die Inhalte gleich sind.
     */
    protected abstract boolean areContentsTheSame(T item, T other);

    /**
     * Wird aus dem Adapter gerufen, wenn {@link AWSortedItemListAdapter#compare(Object, Object)}
     * '0' zuruckgegeben hat. Dann kann hier angegeben werden, ob die Suchkritieren identisch sind.
     *
     * @param other
     *         das zu vergleichende Item
     * @return true, wenn die Suchkriterien gleich sind.
     */
    protected abstract boolean areItemsTheSame(T item, T other);

    /**
     * Wird aus dem Adapter gerufen, um die Reihenfolge festzulegen.
     *
     * @param other
     *         das zu vergleichende Item
     * @return -1, wenn dieses Item vor other liegen soll
     * <p>
     * 1, wenn dieses Item hinter other liegen soll
     * <p>
     * sonst 0. Dann wird {@link AWSortedItemListAdapter#areItemsTheSame(Object, Object)} gerufen
     */
    protected abstract int compare(T item, T other);

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
        return getID(sortedItemList.get(position));
    }

    public final SortedList<T> getRemovedItems() {
        return removedSortedItemList;
    }

    @Override
    public final int indexOf(T item) {
        return sortedItemList.indexOf(item);
    }

    @Override
    public final void onBindViewHolder(AWLibViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (sortedItemList.size() < position + 1) {
            int newSize = position + 20 > mCount ? position + 20 : mCount;
            for (int i = sortedItemList.size(); i < newSize; i++) {
                sortedItemList.add(mItemgenerator.createItem(i));
            }
        }
        if (holder.getItemViewType() != UNDODELETEVIEW) {
            mBinder.onBindViewHolder(holder, sortedItemList.get(position), position);
        }
    }

    @Override
    public final void onItemDismissed(int position) {
        removedSortedItemList.add(sortedItemList.get(position));
        removeItemAt(position);
    }

    @Override
    public final void onItemMoved(int fromPosition, int toPosition) {
        throw new IllegalStateException("Drag wird von einer SortedList nicht unterstuetzt!");
    }

    @Override
    public final boolean remove(T item) {
        removedSortedItemList.add(item);
        return sortedItemList.remove(item);
    }

    @Override
    public final T removeItemAt(int position) {
        T item = sortedItemList.get(position);
        removedSortedItemList.add(item);
        sortedItemList.remove(item);
        return item;
    }

    @Override
    public final void reset() {
        sortedItemList.clear();
        removedSortedItemList.clear();
        notifyDataSetChanged();
    }

    @Override
    public final void updateItemAt(int position, T item) {
        sortedItemList.updateItemAt(position, item);
    }

    private class MCallback extends SortedList.Callback<T> {
        @Override
        public boolean areContentsTheSame(T oldItem, T newItem) {
            return AWSortedItemListAdapter.this.areContentsTheSame(oldItem, newItem);
        }

        @Override
        public boolean areItemsTheSame(T item1, T item2) {
            return AWSortedItemListAdapter.this.areItemsTheSame(item1, item2);
        }

        @Override
        public int compare(T o1, T o2) {
            return AWSortedItemListAdapter.this.compare(o1, o2);
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
            onItemMoved(fromPosition, toPosition);
            mBinder.onItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
            mBinder.onItemDismiss(position);
        }
    }
}
