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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Adapter mit einer {@link java.util.ArrayList}. Diese Liste ist Swipe- und Dragable.
 */
public abstract class AWItemListAdapter<T> extends AWItemListAdapterTemplate<T> {
    private final ArrayList<T> removedItemList;
    private final ArrayList<T> itemList;
    private ItemGenerator<T> mItemgenerator;
    private int mCount;

    public AWItemListAdapter(@NonNull AWListAdapterBinder<T> binder) {
        super(binder);
        itemList = new ArrayList<>();
        removedItemList = new ArrayList<>();
    }

    /**
     * @return neue Groesse der Liste
     */
    @Override
    public final int add(T item) {
        itemList.add(item);
        return itemList.size();
    }

    /**
     * Fuegt Items aus einem Cursor hinzu
     *
     * @param cursor
     *         Cursor, mit dem die Items generiert werden sollen
     * @param generator
     *         Generator. Wird zum genererieren der Items gerufen.
     */
    @Override
    public final void addAll(Cursor cursor, ItemGenerator<T> generator) {
        mItemgenerator = generator;
        mCount = cursor.getCount();
        int newSize = mCount > 20 ? 20 : mCount;
        for (int i = itemList.size(); i < newSize; i++) {
            itemList.add(generator.createItem(i));
        }
    }

    /**
     * Fuegt alle Items einer Liste hinzu. Doppelte Items werden nicht erkannt!
     *
     * @param items
     *         Liste mit Items.
     */
    @Override
    public final void addAll(List<T> items) {
        itemList.addAll(items);
    }

    /**
     * Fuegt alle Items einer Liste hinzu. Doppelte Items werden nicht erkannt!
     *
     * @param items
     *         Array mit Items.
     */
    @Override
    public final void addAll(T[] items) {
        addAll(Arrays.asList(items));
    }

    /**
     * @param position
     *         Position des Items
     * @return Liefert ein Item an der Position zuruck.
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final T get(int position) {
        return itemList.get(position);
    }

    /**
     * @return Liefert die Anzahl der Items zuruck
     */
    @Override
    public final int getItemCount() {
        if (mItemgenerator != null) {
            return mCount;
        }
        return itemList.size();
    }

    /**
     * @param position
     *         Position
     * @return Liefert das Item an position zuruck
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final long getItemId(int position) {
        return getID(itemList.get(position));
    }

    /**
     * @return Liefert die Liste der Items zurueck
     */
    public List<T> getItemList() {
        return itemList;
    }

    /**
     * @return Liefert die Liste der entfernten Items zurueck
     */
    public List<T> getRemovedItemList() {
        return removedItemList;
    }

    /**
     * @param item
     *         Item
     * @return Liefert den Index eines Items zuruck, -1 wenn kein Item existiert
     */
    @Override
    public final int indexOf(T item) {
        return itemList.indexOf(item);
    }

    /**
     * Ist der ViewType des Viewholders != UNDOLETEVIEW, wird der {@link AWListAdapterBinder}
     * gerufen.
     * <p>
     * Ist die angefragte Position > size der ItemList UND gibt es einen ursor, der weitere Daten
     * beinhaltet, wird nachgelesen.
     *
     * @param holder
     *         ViewHolder
     * @param position
     *         Position
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final void onBindViewHolder(AWLibViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (itemList.size() < position + 1) {
            int newSize = position + 20 > mCount ? position + 20 : mCount;
            for (int i = itemList.size(); i < newSize; i++) {
                itemList.add(mItemgenerator.createItem(i));
            }
        }
        if (holder.getItemViewType() != UNDODELETEVIEW) {
            mBinder.onBindViewHolder(holder, itemList.get(position), position);
        }
    }

    /**
     * Entfernt ein Item an der Position und ruft {@link AWListAdapterBinder#onItemDismissed(int)}
     *
     * @param position
     *         Position
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final void onItemDismissed(int position) {
        removeItemAt(position);
        notifyItemRemoved(position);
        mBinder.onItemDismiss(position);
    }

    /**
     * Tauscht zwei Items in der Liste und ruft {@link AWListAdapterBinder#onItemMoved(int, int)}
     *
     * @param fromPosition
     *         Urspruenglich Position des Items
     * @param toPosition
     *         Neue Position des Items
     * @throws IndexOutOfBoundsException
     *         wenn size < fromPosition oder fromPosition < 0 oder size < toPosition oder toPosition
     *         < 0
     */
    @Override
    public final void onItemMoved(int fromPosition, int toPosition) {
        Collections.swap(itemList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        mBinder.onItemMoved(fromPosition, toPosition);
    }

    /**
     * Entfernt ein Item
     *
     * @param item
     *         Item
     * @return true, wenn erfolgreich.
     */
    @Override
    public final boolean remove(T item) {
        if (itemList.remove(item)) {
            removedItemList.add(item);
            return true;
        }
        return false;
    }

    /**
     * Entfernt ein Item an position
     *
     * @param position
     *         Position des Items
     * @return true, wenn erfolgreich.
     *
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final T removeItemAt(int position) {
        T item = itemList.remove(position);
        removedItemList.add(item);
        return item;
    }

    /**
     * Setzt die Liste zurueck.
     */
    @Override
    public final void reset() {
        itemList.clear();
        removedItemList.clear();
        notifyDataSetChanged();
    }

    /**
     * Tauscht die Liste aus
     *
     * @param items
     *         Liste mit Items
     */
    public void swap(List<T> items) {
        reset();
        addAll(items);
    }

    /**
     * Tauscht die Liste aus
     *
     * @param items
     *         Array mit Items
     */
    public void swap(T[] items) {
        swap(Arrays.asList(items));
    }

    /**
     * Tauscht das Item an der Stelle position aus.
     *
     * @param position
     *         Position
     * @param item
     *         Item
     * @throws IndexOutOfBoundsException
     *         wenn size < position oder position < 0
     */
    @Override
    public final void updateItemAt(int position, T item) {
        itemList.add(position, item);
    }
}
