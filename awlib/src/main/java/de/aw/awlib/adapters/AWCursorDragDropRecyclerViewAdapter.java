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
import android.util.LongSparseArray;
import android.util.SparseIntArray;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.recyclerview.AWDragSwipeHelperCallback;
import de.aw.awlib.recyclerview.AWDragSwipeRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Adapter fuer RecyclerView, der Drag/Drop und Swipe unterstuetzt. Der Adapter baut eine Liste mit
 * allen Inhalten  des Cursor auf, daher sollte er nur mit einer geringen Menge von Elementen
 * verwendet werden.
 */
public class AWCursorDragDropRecyclerViewAdapter extends AWCursorRecyclerViewAdapter {
    /**
     * Liste der Positionen des Cursors. Wird fuer das Mapping der angezeigten und die
     * tatsaechlichen Position im Adapter verwendet.
     */
    private final List<Integer> mItems = new ArrayList<>();
    private final AWDragSwipeRecyclerViewFragment mBinder;
    private SparseIntArray mItemList = new SparseIntArray();
    private LongSparseArray<Runnable> mPendingDeleteItems = new LongSparseArray<>();
    private int removed = 0;

    /**
     * Initialisiert und baut die Liste der Positionen auf
     */
    public AWCursorDragDropRecyclerViewAdapter(AWDragSwipeRecyclerViewFragment binder,
                                               int viewHolderLayout) {
        super(binder, viewHolderLayout);
        mBinder = binder;
    }

    /**
     * Hier wird die Liste der Positionen im Adapter aufgebaut.
     *
     * @param cursor
     *         Cursor des Adapters
     */
    private void fillItems(Cursor cursor) {
        mItems.clear();
        if (cursor != null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                mItems.add(i);
            }
        }
    }

    /**
     * @return Anzahl der Items. Dabei werden ggfs. entfernte Items beruecksichtigt.
     */
    @Override
    public int getItemCount() {
        return super.getItemCount() - removed;
    }

    /**
     * @return Liste der IDs der Items.
     */
    public List<Long> getItemIDs() {
        List<Long> mItemIDList = new ArrayList<>();
        for (int i = 0; i < mItems.size(); i++) {
            mItemIDList.add(getItemId(i));
        }
        return mItemIDList;
    }

    /**
     * @param position
     *         Position des Items, zu dem die ID benoetigt wird
     * @return die Position. Entfernte/verschobene Items werden beruecksichtig
     */
    @Override
    public long getItemId(int position) {
        Integer mPosition = mItems.get(position);
        return super.getItemId(mPosition);
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder viewHolder, final int position) {
        super.onBindViewHolder(viewHolder, position);
        final long id = getItemId(position);
        final Runnable mRunnable = mPendingDeleteItems.get(id);
        if (mRunnable != null) {
            mBinder.onBindPendingDeleteViewHolder(viewHolder, mCursor);
            View view = viewHolder.itemView.findViewById(R.id.tvDoUndo);
            view.postDelayed(mRunnable, 10000);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.removeCallbacks(mRunnable);
                    mPendingDeleteItems.delete(id);
                    notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public void onClick(AWLibViewHolder holder) {
        if (mPendingDeleteItems.get(holder.getID()) == null) {
            super.onClick(holder);
        }
    }

    /**
     * Entfernt ein Item an der Position. Funktioniert nur, wenn {@link
     * AWDragSwipeHelperCallback#setIsSwipeable(boolean)} mit true gerufen wurde.
     *
     * @param position
     *         Position des items im Adapter, das entfernt werden soll
     */
    public void onItemDismiss(int position) {
        removed++;
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Vertauscht zwei Items. Funktioniert nur, wenn {@link AWDragSwipeHelperCallback#setIsDragable(boolean)}
     * mit true gerufen wurde.
     *
     * @param fromPosition
     *         urspruengliche Position
     * @param toPosition
     *         Neue Position
     * @return Immer true
     */
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        AWApplication.Log("Item Moved. From: " + fromPosition + " To: " + toPosition);
        return true;
    }

    public void setPendingDeleteItem(long id, Runnable runnable) {
        mPendingDeleteItems.put(id, runnable);
    }

    /**
     * Setzt einen neuen Cursor und baut die Liste der Positionen neu auf.
     */
    @Override
    public Cursor swapCursor(Cursor newCursor) {
        fillItems(newCursor);
        return super.swapCursor(newCursor);
    }
}
