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
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Template eines Adapters mit Liste.
 */
public abstract class AWItemListAdapterTemplate<T> extends AWBaseAdapter {
    /**
     * Anzahl der (nach-) zu lesenden Items aus einem Cursor.
     */
    private static final int LOADITEMS = 20;
    protected final AWListAdapterBinder<T> mBinder;
    private ItemGenerator<T> mItemgenerator;
    private Cursor mCursor;
    private T mPendingChangedItem;
    private T mPendingDeleteItem;

    public AWItemListAdapterTemplate(@NonNull AWListAdapterBinder<T> binder) {
        super(binder);
        mBinder = binder;
    }

    /**
     * Fuegt ein Item der Liste hinzu.
     */
    public abstract int add(T item);

    /**
     * Fuegt alle Items einer Liste hinzu.
     *
     * @param items
     *         Liste mit Items.
     */
    public abstract void addAll(List<T> items);

    /**
     * Fuegt alle Items zu einer Liste hinzu.
     *
     * @param items
     *         Array mit Items.
     */
    public abstract void addAll(T[] items);

    @Override
    public final void cancelPendingChangeItem() {
        mPendingChangedItem = null;
        super.cancelPendingChangeItem();
    }

    @Override
    public final void cancelPendingDelete() {
        mPendingDeleteItem = null;
        super.cancelPendingDelete();
    }

    /**
     * Erstellt Items aus einem Cursor. Es werden maximal {@link AWItemListAdapterTemplate#LOADITEMS}
     * Items generiert
     *
     * @param start
     *         Startposition des Cursors, ab der Items generiert werden sollen
     * @return Liste mit neuen Items
     */
    protected final List<T> fillItemList(int start) {
        List<T> newItemList = new ArrayList<>();
        int newSize = mCursor.getCount() > LOADITEMS ? start + LOADITEMS : mCursor.getCount();
        for (int i = start; i < newSize; i++) {
            mCursor.moveToPosition(i);
            newItemList.add(mItemgenerator.createItem(mCursor, i));
        }
        return newItemList;
    }

    /**
     * @param position
     *         Position des Items
     * @return Liefert ein Item an der Position zuruck.
     */
    public abstract T get(int position);

    /**
     * @return Liefert die ID zuruck.
     */
    protected abstract long getID(T item);

    /**
     * @return die Anzahl der Items. Gibt es einen Cursor, wird die Anzahl der Cursorzeilen
     * zurueckgeliefert. Damit wird erreicht, dass beim Start nur eine durch {@link
     * AWItemListAdapterTemplate#LOADITEMS} festgelegte Anzahl von Items geberiert werden koennen.
     * Erbende Klassen muessen in
     * <p>
     * {@link AWItemListAdapterTemplate#onBindViewHolder(AWLibViewHolder, int)} entsprechend
     * nachlesen.
     */
    @Override
    public final int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return getItemListCount();
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
    public abstract long getItemId(int position);

    /**
     * @return Liefert die aktuelle Anzahl der Items in der Liste zuruck
     */
    public abstract int getItemListCount();

    /**
     * @return Das aktuelle PendingChangedItem. Ist keins gesetzt, dann null.
     */
    public final T getPendingChangedItem() {
        return mPendingChangedItem;
    }

    /**
     * @return Das aktuelle PendingDeleteItem. Ist keins gesetzt, dann null.
     */
    public final T getPendingDeleteItem() {
        return mPendingDeleteItem;
    }

    /**
     * @param item
     *         Item
     * @return Liefert die Position des Items
     */
    public abstract int getPosition(T item);

    /**
     * @return Liefert die Liste der entfernten Items zurueck
     */
    public abstract List<T> getRemovedItemList();

    /**
     * @param item
     *         Item
     * @return Liefert den Index eines Items zuruck
     */
    public abstract int indexOf(T item);

    @CallSuper
    @Override
    public void onBindViewHolder(AWLibViewHolder holder, int position) {
        if (holder.getItemViewType() != UNDODELETEVIEW) {
            mBinder.onBindViewHolder(holder, get(position), position);
        }
        super.onBindViewHolder(holder, position);
    }

    /**
     * Wird gerufen, wenn ein Item entfernt wird.
     *
     * @param position
     *         Position
     */
    @Override
    protected final void onItemDismissed(int position) {
        onItemDismissed(mPendingDeleteItem, position);
    }

    /**
     * Wird gerufen, wenn ein Item geloscht wird
     *
     * @param mPendingDeleteItem
     *         Item
     * @param position
     *         Position des Items
     */
    protected abstract void onItemDismissed(T mPendingDeleteItem, int position);

    /**
     * Wird gerufen, wenn ein Item die Position aendert
     *
     * @param fromPosition
     *         Urspruenglich Position des Items
     * @param toPosition
     *         Neue Position des Items
     */
    @Override
    public abstract void onItemMoved(int fromPosition, int toPosition);

    /**
     * Ruft bei Klick auf Item in der RecyclerView
     * <p>
     * {@link AWListAdapterBinder#onRecyclerItemClick(View, int, Object)}
     *
     * @param holder
     *         ViewHolder
     */
    @Override
    protected final void onViewHolderClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(holder.itemView);
        T item = get(position);
        mBinder.onRecyclerItemClick(v, position, item);
    }

    /**
     * Ruft bei LongKlick auf Item in der RecyclerView .
     * <p>
     * {@link AWListAdapterBinder#onRecyclerItemLongClick(View, int, Object)}
     *
     * @param holder
     *         ViewHolder
     */
    @Override
    protected final boolean onViewHolderLongClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(v);
        T item = get(position);
        return mBinder.onRecyclerItemLongClick(v, position, item);
    }

    /**
     * Kalkuliert Positionen von Items neu. Die Default-Implementierung macht hier nichts.
     */
    public void recalculatePositions() {
    }

    /**
     * Entfernt ein Item
     *
     * @param item
     *         Item
     * @return true, wenn erfolgreich.
     */
    public abstract boolean remove(T item);

    /**
     * Entfernt ein Item an position
     *
     * @param position
     *         Position des Items
     * @return das Item
     */
    public final T removeItemAt(int position) {
        T item = get(position);
        remove(item);
        return item;
    }

    /**
     * Setzt die Liste zurueck.
     */
    public abstract void reset();

    /**
     * Hier kann ein Item gesetzt werden, dass eine separate View anzeigt. Diese View ist vom Binder
     * entsprechend zu setzen (in getItemViewType, OnCreateViewHolder). Wenn dann die RecyclerView
     * bewegt wird oder ein anderes Item zu gesetzt wird, wird die View wieder zureuckgesetzt
     *
     * @param item
     *         Item
     */
    public final void setPendingChangedItem(T item, @LayoutRes int layout) {
        super.setPendingChangedItemPosition(getPosition(item), layout);
        mPendingChangedItem = item;
    }

    /**
     * Hier kann die Position eines Items gesetzt werden, dass eine separate View anzeigt. Diese
     * View ist vom Binder entsprechend zu setzen (in getItemViewType, OnCreateViewHolder). Wenn
     * dann die RecyclerView bewegt wird oder ein anderes Item zu gesetzt wird, wird die View wieder
     * zureuckgesetzt
     *
     * @param position
     *         Position des Items
     */
    @Override
    public final void setPendingChangedItemPosition(int position, @LayoutRes int layout) {
        super.setPendingChangedItemPosition(position, layout);
        mPendingChangedItem = get(position);
    }

    /**
     * Hier kann eine Item zu loeschen vorgemerkt werden. In diesem Fall wird eine View mit
     * 'Geloescht' bzw. 'Rueckgaengig' angezeigt. Wenn dann die RecyclerView bewegt wird oder ein
     * anderes Item zu Loeschung vorgemerjt wird, wird das Item tatsaechlich aus dem Adapter
     * entfernt.
     * <p>
     * Der Binder wird durch {@link AWBaseAdapterBinder#onItemDismissed(int)} informiert.
     *
     * @param item
     *         Item
     */
    public final void setPendingDeleteItem(T item) {
        super.setPendingDeleteItemPosition(getPosition(item));
        mPendingDeleteItem = item;
    }

    @Override
    public final void setPendingDeleteItemPosition(int position) {
        super.setPendingDeleteItemPosition(position);
        mPendingDeleteItem = get(position);
    }

    /**
     * Tauscht den Cursor aus
     *
     * @param cursor
     *         cursor
     * @param generator
     *         Itemgenerator
     */
    @CallSuper
    public void swap(Cursor cursor, ItemGenerator<T> generator) {
        mItemgenerator = generator;
        mCursor = cursor;
    }

    /**
     * Tauscht die Liste aus
     *
     * @param items
     *         Liste mit Items
     */
    public abstract void swap(List<T> items);

    /**
     * Tauscht die Liste aus
     *
     * @param items
     *         Array mit Items
     */
    public final void swap(T[] items) {
        if (items != null) {
            swap(Arrays.asList(items));
        } else {
            reset();
        }
    }

    /**
     * Tauscht das Item an der Stelle position aus.
     *
     * @param position
     *         Position
     * @param item
     *         Item
     */
    public abstract void updateItemAt(int position, T item);

    /**
     * Generator fuer Items. Wird im Zusammenhang mit einem Cursor verwendet. Siehe {@link
     * AWItemListAdapterTemplate#swap(Cursor, ItemGenerator)}
     */
    public interface ItemGenerator<T> {
        /**
         * Erstellt ein Item zur position
         *
         * @param position
         *         Position
         * @return Erstelltes Item
         */
        T createItem(Cursor c, int position);
    }

    /**
     * Binder fuer Adapter-Aktionen
     */
    public interface AWListAdapterBinder<T> extends AWBaseAdapterBinder {
        int getItemViewType(T item, int position);

        /**
         * Wird zum Binden des ViewHolders gerufen
         *
         * @param holder
         *         ViewHolder
         * @param item
         *         Item zum binden
         * @param position
         *         Position des Items
         */
        void onBindViewHolder(AWLibViewHolder holder, T item, int position);

        /**
         * Wird vom Adapter gerufen, wenn ein Item entfernt wird.
         *
         * @param item
         *         Item
         * @param position
         *         Position des Items
         */
        void onItemDismiss(T item, int position);

        /**
         * Wird vom Adapter gerufen, wenn ein Item verschoben wird
         *
         * @param fromPosition
         *         urspruengliche Position des Items
         * @param toPosition
         *         neue Position des Items
         */
        void onItemMoved(int fromPosition, int toPosition);

        /**
         * Wird bei Click auf RecyclerView gerufen
         *
         * @param v
         *         ItemView
         * @param position
         *         Position des Items
         * @param item
         *         Item
         */
        void onRecyclerItemClick(View v, int position, T item);

        /**
         * Wird bei LongClick auf RecyclerView gerufen
         *
         * @param v
         *         ItemView
         * @param position
         *         Position des Items
         * @param item
         *         Item
         */
        boolean onRecyclerItemLongClick(View v, int position, T item);
    }
}
