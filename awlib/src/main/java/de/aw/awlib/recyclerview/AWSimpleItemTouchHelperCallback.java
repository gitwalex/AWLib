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

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import de.aw.awlib.adapters.AWCursorDragDropRecyclerViewAdapter;

/**
 * Helper fuer Drag- und/oder Swipe-RecyclerView
 */
public abstract class AWSimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final AWCursorDragDropRecyclerViewAdapter mAdapter;
    private boolean isDragable;
    private boolean isSwipeable;

    /**
     * @param adapter
     *         AWCursorDragDropRecyclerViewAdapter
     */
    public AWSimpleItemTouchHelperCallback(@NonNull AWCursorDragDropRecyclerViewAdapter adapter) {
        mAdapter = adapter;
    }

    @NonNull
    public AWCursorDragDropRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Setzt die MovementFlags. In der Default-Implementation wird Dragging bei nach oben/unten
     * unterstuetzt, ausserdem Swipe bei links ooder rechts.
     * <p>
     * handelt es sich beim LayoutMagaer um einen GGridLayoutManager, wird nur Dragging (in alle
     * Richtungen) unterstuetzt.
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // Set movement flags based on the layout manager
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags =
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    @Override
    public final boolean isItemViewSwipeEnabled() {
        return isSwipeable;
    }

    @Override
    public final boolean isLongPressDragEnabled() {
        return isDragable;
    }

    /**
     * Wird gerufen, wenn Items der RecyclerView bewegt werden.
     */
    @Override
    public abstract boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                   RecyclerView.ViewHolder target);

    @Override
    public final void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        long id = viewHolder.getItemId();
        onSwiped(viewHolder, direction, position, id);
    }

    /**
     * Wird bei swipe gerufen.
     *
     * @param viewHolder
     *         ViewHolder
     * @param direction
     *         Richtung des Swipe
     * @param position
     *         Position des Items in der RecyclerView
     * @param id
     *         ID des Items
     */
    protected abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int direction,
                                     int position, long id);

    /**
     * Steuert, ob eine RecyclerView Dragable ist
     *
     * @param isDragable
     *         true: ist Dragable
     */
    public final void setIsDragable(boolean isDragable) {
        this.isDragable = isDragable;
    }

    /**
     * Steuert, ob eine RecyclerView Swapable ist
     *
     * @param isSwipeable
     *         true: ist Swapable
     */
    public final void setIsSwipeable(boolean isSwipeable) {
        this.isSwipeable = isSwipeable;
    }
}