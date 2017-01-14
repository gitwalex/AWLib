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

import android.support.v7.widget.RecyclerView;

import de.aw.awlib.adapters.AWCursorDragDropRecyclerViewAdapter;

/**
 * TouchHelper fuer Drag- und Swipe. Bei
 */
public class AWDragSwipeHelperCallback extends AWSimpleItemTouchHelperCallback {
    public AWDragSwipeHelperCallback(AWCursorDragDropRecyclerViewAdapter adapter) {
        super(adapter);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    protected void onSwiped(AWCursorDragDropRecyclerViewAdapter adapter,
                            RecyclerView.ViewHolder viewHolder, int direction, int position,
                            long id) {
        mAdapter.onItemDismiss(position);
    }
}