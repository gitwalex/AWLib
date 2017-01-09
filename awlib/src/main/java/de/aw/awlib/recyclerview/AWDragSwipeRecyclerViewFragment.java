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
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;

import de.aw.awlib.adapters.AWCursorDragDropRecyclerViewAdapter;
import de.aw.awlib.adapters.AWCursorRecyclerViewAdapter;

/**
 * RecyclerViewFragment. Bietet Moeglichkeit der Konfiguration Dragging (durch setzten von {@link
 * AWDragSwipeRecyclerViewFragment#setIsDragable(boolean)} oder {@link
 * AWDragSwipeRecyclerViewFragment#setOneTouchStartDragResID(int)}) oder Swipe (durch setzten von
 * {@link AWDragSwipeRecyclerViewFragment#setIsSwipeable(boolean)}). Dieses Fragment sollte nur
 * benutzt werden, wenn es relativ wenige Elemente gibt.
 *
 * @see AWCursorDragDropRecyclerViewAdapter
 */
public class AWDragSwipeRecyclerViewFragment extends AWCursorRecyclerViewFragment {
    private Drawable backgroundOnStartDrag;
    private AWSimpleItemTouchHelperCallback callbackTouchHelper;
    private boolean isDragable;
    private boolean isSwipeable;
    private ItemTouchHelper mTouchHelper;
    private int oneTouchStartDragResID = -1;

    /**
     * @return Liefert einen AWCursorDragDropRecyclerViewAdapter zurueck.
     */
    public AWCursorRecyclerViewAdapter getCursorAdapter() {
        AWCursorDragDropRecyclerViewAdapter mAdapter =
                new AWCursorDragDropRecyclerViewAdapter(this, viewHolderLayout);
        callbackTouchHelper = getItemTouchCallback(mAdapter);
        callbackTouchHelper.setIsDragable(isDragable);
        callbackTouchHelper.setIsSwipeable(isSwipeable);
        mTouchHelper = new ItemTouchHelper(callbackTouchHelper);
        mTouchHelper.attachToRecyclerView(mRecyclerView);
        return mAdapter;
    }

    @NonNull
    protected AWSimpleItemTouchHelperCallback getItemTouchCallback(
            AWCursorDragDropRecyclerViewAdapter mAdapter) {
        return new AWSimpleItemTouchHelperCallback(mAdapter);
    }

    /**
     * Ist mittels {@link AWDragSwipeRecyclerViewFragment#setOneTouchStartDragResID(int)} eine resID
     * einer View der Detailview gesetzt worden, wird diese als startDrag-Event konfiguriert.
     *
     * @throws NullPointerException
     *         wenn es keine View mit dieer resID gibt
     */
    @CallSuper
    @Override
    protected void onPreBindViewHolder(Cursor cursor, final AWLibViewHolder holder) {
        super.onPreBindViewHolder(cursor, holder);
        if (oneTouchStartDragResID != -1) {
            View handleView = holder.findViewById(oneTouchStartDragResID);
            handleView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        AWDragSwipeRecyclerViewFragment.this.onStartDrag(holder);
                    }
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_UP) {
                        AWDragSwipeRecyclerViewFragment.this.onStopDrag(holder);
                    }
                    return false;
                }
            });
        }
    }

    private void onStartDrag(AWLibViewHolder holder) {
        holder.itemView.setPressed(true);
        mTouchHelper.startDrag(holder);
    }

    private void onStopDrag(AWLibViewHolder holder) {
        holder.itemView.setPressed(false);
    }

    /**
     * Konfiguration, ob die RecyclerView Draggable ist
     *
     * @param isDragable
     *         true: ist Draggable
     */
    public void setIsDragable(boolean isDragable) {
        this.isDragable = isDragable;
        if (callbackTouchHelper != null) {
            callbackTouchHelper.setIsDragable(isDragable);
        }
    }

    /**
     * Konfiguration, ob die RecyclerView Swipeable ist
     *
     * @param isSwipeable
     *         true: ist Swipeable
     */
    public void setIsSwipeable(boolean isSwipeable) {
        this.isSwipeable = isSwipeable;
        if (callbackTouchHelper != null) {
            callbackTouchHelper.setIsDragable(isSwipeable);
        }
    }

    /**
     * Durch setzen der resID der DetailView wird dieses Item als OneToch-Draghandler benutzt, d.h.
     * dass bei einmaligen beruehren dieses Items der Drag/Drop-Vorgang startet. Die resID muss in
     * onCreate() gesetzt werden.
     *
     * @param resID
     *         resID der View, bei deren Beruehrung der Drag/Drop Vorgand starten soll
     */
    public void setOneTouchStartDragResID(@IdRes int resID) {
        this.oneTouchStartDragResID = resID;
    }
}
