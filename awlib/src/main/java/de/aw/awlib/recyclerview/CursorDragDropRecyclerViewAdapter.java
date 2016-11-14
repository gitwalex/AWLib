/*
 * MonMa: Eine freie Android-App fuer Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 23730 Neustadt/Germany]
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

package de.aw.awlib.recyclerview;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.aw.awlib.AWLIbApplication;

/**
 * Adapter fuer RecyclerView, der Drag/Drop und Swipe unterstuetzt. Der Adapter baut eine Liste mit
 * allen Inhalten  des Cursor auf, daher sollte er nur mit einer geringen Menge von Elementen
 * verwendet werden.
 */
public class CursorDragDropRecyclerViewAdapter extends CursorRecyclerViewAdapter {
    /**
     * Liste der Positionen des Cursoers. Wird fuer das Mapping der angezeigten und die
     * tatsaechlichen Position im Adapter verwendet.
     */
    private final List<Integer> mItems = new ArrayList<>();

    /**
     * Initialisiert und baut die Liste der Positionen auf
     */
    public CursorDragDropRecyclerViewAdapter(CursorViewHolderBinder binder) {
        super(binder);
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
        return mItems.size();
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
     *
     * @return die Position. Entfernet/verchobene Items werden beruecksichtig
     */
    @Override
    public long getItemId(int position) {
        Integer mPosition = mItems.get(position);
        return super.getItemId(mPosition);
    }

    /**
     * Entfernt ein Item an der Position. Funktioniert nur, wenn {@link
     * SimpleItemTouchHelperCallback#setIsSwipeable(boolean)} mit true gerufen wurde.
     *
     * @param position
     *         Position des items im Adapter, das entfern werden soll
     */
    public void onItemDismiss(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Vertauscht zwei Items. Funktioniert nur, wenn {@link SimpleItemTouchHelperCallback#setIsDragable(boolean)}
     * mit true gerufen wurde.
     *
     * @param fromPosition
     *         urspruengliche Position
     * @param toPosition
     *         Neue Position
     *
     * @return Immer true
     */
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        AWLIbApplication.Log("Item Moved. From: " + fromPosition + " To: " + toPosition);
        return true;
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
