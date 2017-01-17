package de.aw.awlib.recyclerview;

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
        getAdapter().onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    protected void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position,
                            long id) {
        getAdapter().onItemDismiss(position);
    }
}