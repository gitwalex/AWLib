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
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseIntArray;

import java.util.List;

import de.aw.awlib.application.AWApplication;
import de.aw.awlib.recyclerview.AWCursorRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

import static android.support.v7.widget.RecyclerView.NO_ID;
import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Adapter fuer RecyclerView mit Cursor.
 */
public class AWCursorRecyclerViewAdapter extends AWBaseRecyclerViewAdapter
        implements AWLibViewHolder.OnClickListener, AWLibViewHolder.OnLongClickListener {
    protected final int viewHolderLayout;
    private final CursorDataObserver mDataObserver;
    private final String mRowIDColumn;
    private final AWCursorRecyclerViewFragment mBinder;
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
    public AWCursorRecyclerViewAdapter(@NonNull AWCursorRecyclerViewFragment binder,
                                       int viewHolderLayout) {
        this(binder, "_id", viewHolderLayout);
    }

    /**
     * Initialisiert Adapter.
     *
     * @param binder
     *         CursorViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
     * @param idColumn
     *         Spalte, die als ID verwendet werden soll
     */
    protected AWCursorRecyclerViewAdapter(@NonNull AWCursorRecyclerViewFragment binder,
                                          @NonNull String idColumn, int viewHolderLayout) {
        super(binder, viewHolderLayout);
        mBinder = binder;
        mDataObserver = new CursorDataObserver();
        mRowIDColumn = idColumn;
        this.viewHolderLayout = viewHolderLayout;
        setHasStableIds(true);
    }

    /**
     * Ist der Cursor gueltig, wird der CursorViewHolderBinder aus dem Konstructor aufgerufen
     *
     * @param viewHolder
     *         aktueller viewHolder
     * @param position
     *         position des Holders
     * @throws IllegalStateException
     *         wenn der Cursor als invald erklaert wurde oder die Position vom Cursor nicht erreicht
     *         werden kann
     */
    @Override
    protected void bindTheViewHolder(AWLibViewHolder viewHolder, int position) {
        moveCursor(convertItemPosition(position));
        mBinder.onBindViewHolder(viewHolder, mCursor);
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

    private Cursor moveCursor(int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        return mCursor;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mOnDataChangeListener = new AWAdapterDataObserver();
        registerAdapterDataObserver(mOnDataChangeListener);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (getPendingDeleteItemPosition() != NO_POSITION) {
            mItemPositions.put(getAdapterPosition(getPendingDeleteItemPosition()), NO_POSITION);
        }
        unregisterAdapterDataObserver(mOnDataChangeListener);
    }

    @CallSuper
    @Override
    public void onItemDismiss(int position) {
        if (position != NO_POSITION) {
            notifyItemRemoved(position);
        }
    }

    @CallSuper
    @Override
    protected void onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        AWApplication.Log("Item Moved. From: " + fromPosition + " To: " + toPosition);
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
                mItemPositions.put(getAdapterPosition(positionStart + i), NO_POSITION);
                removed++;
            }
            super.onItemRangeRemoved(positionStart, itemCount);
        }
    }
}