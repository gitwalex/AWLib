package de.aw.awlib.adapters;/*
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
/**
 *
 */

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;
import android.view.View;

import java.util.List;

import de.aw.awlib.application.AWApplication;
import de.aw.awlib.recyclerview.AWLibViewHolder;

import static android.support.v7.widget.RecyclerView.NO_ID;
import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Adapter fuer RecyclerView mit Cursor.
 */
public class AWCursorAdapter extends AWBaseAdapter implements AWLibViewHolder.OnHolderClickListener,
        AWLibViewHolder.OnHolderLongClickListener {
    private final CursorDataObserver mDataObserver;
    private final String mRowIDColumn;
    private final AWCursorAdapterBinder mBinder;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumnIndex;
    private int removed;
    private SparseIntArray mItemPositions = new SparseIntArray();
    private AWAdapterDataObserver mOnDataChangeListener;

    /**
     * Initialisiert Adapter. Cursor muss eine Spalte '_id' enthalten.
     *
     * @param binder
     *         CursorViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
     */
    public AWCursorAdapter(@NonNull AWCursorAdapterBinder binder) {
        this(binder, "_id");
    }

    /**
     * Initialisiert Adapter.
     *
     * @param binder
     *         CursorViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
     * @param idColumn
     *         Name der Spalte, die den Index enthaelt.
     */
    protected AWCursorAdapter(@NonNull AWCursorAdapterBinder binder, @NonNull String idColumn) {
        super(binder);
        mBinder = binder;
        mDataObserver = new CursorDataObserver();
        mRowIDColumn = idColumn;
        setHasStableIds(true);
    }

    /**
     * Convertiert die Position in der RecyclerView in die Psoition im Adapter unter
     * Beruecksichtigung verschobener und geloeschter Items
     *
     * @param position
     *         Position in der RecyclerView
     * @return Position im Adapter
     */
    private int convertItemPosition(int position) {
        int mPosition = getAdapterPosition(position);
        return mItemPositions.get(mPosition, mPosition);
    }

    private void doLog() {
        List<Long> list = getItemIDs();
        StringBuilder sb = new StringBuilder();
        for (long value : list) {
            sb.append(" ,").append(value);
        }
        AWApplication.Log("Liste:" + sb.toString());
    }

    /**
     * @return die ID der Position, wenn der Cursor gueltig ist. Ansonsten NO_ID
     */
    private long getAdapterItemID(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(mRowIdColumnIndex);
        }
        return NO_ID;
    }

    /**
     * @param position
     *         Position in der RecyclerView
     * @return zur Position der RecyclerView die Position im Adapter unter Beruecksichtigung der
     * geloeschten Items
     */
    private int getAdapterPosition(int position) {
        int mPosition = position;
        for (int index = 0; index < mItemPositions.size(); index++) {
            int key = mItemPositions.keyAt(index);
            if (key <= mPosition) {
                int value = mItemPositions.get(key);
                if (value == NO_POSITION) {
                    mPosition++;
                }
            }
        }
        return mPosition;
    }

    /**
     * @return Anzahl der im Adapter vorhandenen Items abzueglich der bereits entfernten Items
     */
    @Override
    public int getItemCount() {
        int count = 0;
        if (mDataValid && mCursor != null) {
            count = mCursor.getCount() - removed;
        }
        return count < 0 ? 0 : count;
    }

    /**
     * @param position
     *         Position in der RecyclerView
     * @return Position im Adapter unter Beruecksichtigung geloeschter und verschobener Items
     */
    @Override
    public final long getItemId(int position) {
        return getAdapterItemID(convertItemPosition(position));
    }

    private void moveCursor(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mOnDataChangeListener = new AWAdapterDataObserver();
        registerAdapterDataObserver(mOnDataChangeListener);
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder.getItemViewType() != UNDODELETEVIEW) {
            moveCursor(convertItemPosition(position));
            mBinder.onBindViewHolder(holder, mCursor, position);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (getPendingDeleteItemPosition() != NO_POSITION) {
            mItemPositions.put(getAdapterPosition(getPendingDeleteItemPosition()), NO_POSITION);
        }
        unregisterAdapterDataObserver(mOnDataChangeListener);
    }


    @Override
    protected void onViewHolderClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(holder.itemView);
        long id = getItemId(position);
        mBinder.onRecyclerItemClick(v, position, id);
    }

    @Override
    protected boolean onViewHolderLongClicked(AWLibViewHolder holder) {
        View v = holder.itemView;
        int position = getRecyclerView().getChildAdapterPosition(v);
        long id = getItemId(position);
        return mBinder.onRecyclerItemLongClick(v, position, id);
    }

    /**
     * Swap in a new Cursor, returning the old Cursor. The returned old Cursor is <em>not</em>
     * closed. Ausserdem wird auf den neuen Cursor ein Observer registriert, damit bei close()
     * entsprechen die Daten als ungueltig erklaert werden. Vom alten Cursor wird der Oberver
     * entfernt.
     */
    public Cursor swapCursor(Cursor newCursor) {
        final Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            oldCursor.unregisterDataSetObserver(mDataObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            newCursor.registerDataSetObserver(mDataObserver);
            mRowIdColumnIndex = newCursor.getColumnIndexOrThrow(mRowIDColumn);
            mDataValid = true;
        } else {
            mRowIdColumnIndex = -1;
            mDataValid = false;
        }
        notifyDataSetChanged();
        return oldCursor;
    }

    public interface AWCursorAdapterBinder extends AWBaseAdapterBinder {
        void onBindViewHolder(AWLibViewHolder viewHolder, Cursor mCursor, int position);

        /**
         * Wird vom Adapter gerufen, wenn ein Item entfernt wird.
         *
         * @param itemID
         *         ID des Items
         * @param position
         *         Position des Items
         */
        void onItemDismiss(long itemID, int position);

        /**
         * Wird vom Adapter gerufen, wenn ein Item verschoben wird
         *
         * @param itemID
         *         ID des Items
         * @param fromPosition
         *         urspruengliche Position des Items
         * @param toPosition
         *         neue Position des Items
         */
        void onItemMoved(long itemID, int fromPosition, int toPosition);

        void onRecyclerItemClick(View v, int position, long id);

        boolean onRecyclerItemLongClick(View v, int position, long id);
    }

    /**
     * Observer fuer einen Cursor. Wird der Cursor invalide (z.B. durch close()), werden die Daten
     * als ungueltig erklaert.
     */
    private class CursorDataObserver extends DataSetObserver {
        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetChanged();
        }
    }

    private class AWAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            removed = 0;
            mItemPositions.clear();
            super.onChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            for (int i = 0; i < itemCount; i++) {
                mBinder.onItemMoved(getItemId(fromPosition), fromPosition, toPosition);
                int mFromPosition = getAdapterPosition(fromPosition + i);
                int mToPosition = getAdapterPosition(toPosition + i);
                int mFromItem = mItemPositions.get(mFromPosition, mFromPosition);
                int mToItem = mItemPositions.get(mToPosition, mToPosition);
                if (mToPosition == mFromItem) {
                    mItemPositions.delete(mToPosition);
                } else {
                    mItemPositions.put(mToPosition, mFromItem);
                }
                if (mFromPosition == mToItem) {
                    mItemPositions.delete(mFromPosition);
                } else {
                    mItemPositions.put(mFromPosition, mToItem);
                }
            }
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            for (int i = 0; i < itemCount; i++) {
                mBinder.onItemDismiss(getItemId(positionStart), positionStart);
                mItemPositions.put(getAdapterPosition(positionStart + i), NO_POSITION);
                removed++;
            }
            super.onItemRangeRemoved(positionStart, itemCount);
        }
    }
}