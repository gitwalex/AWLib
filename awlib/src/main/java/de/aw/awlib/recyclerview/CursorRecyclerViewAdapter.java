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
package de.aw.awlib.recyclerview;/*
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
/**
 *
 */

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Adapter fuer RecyclerView mit Cursor.
 */
public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<AWLibViewHolder>
        implements AWLibViewHolder.OnClickListener, AWLibViewHolder.OnLongClickListener {
    private final CursorViewHolderBinder cursorViewHolderBinder;
    private final String mRowIDColumn;
    private final AdapterDataObserver mDataObserver;
    private Cursor mCursor;
    private boolean mDataValid;
    private RecyclerView mRecyclerView;
    private int mRowIdColumnIndex;
    private OnCursorRecyclerViewListener onRecyclerItemClickListener;
    private OnCursorRecyclerViewListener onRecyclerItemLongClickListener;

    /**
     * Initialisiert Adapter. Cursor muss eine Spalte '_id' enthalten.
     *
     * @param binder
     *         CursorViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
     */
    protected CursorRecyclerViewAdapter(@NonNull CursorViewHolderBinder binder) {
        this(binder, "_id");
    }

    /**
     * Initialisiert Adapter.
     *
     * @param binder
     *         CursorViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
     * @param idColumn
     *         Spalte, die als ID verwendet werden soll
     */
    protected CursorRecyclerViewAdapter(@NonNull CursorViewHolderBinder binder,
                                        @NonNull String idColumn) {
        mDataObserver = new AdapterDataObserver();
        cursorViewHolderBinder = binder;
        mRowIDColumn = idColumn;
        setHasStableIds(true);
    }

    /**
     * @return Anzahl der Element im Cursor. Ist der Cursor ungueltig, wird 0 zurueckgeliefert.
     */
    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    /**
     * @return die ID der Position, wenn der Cursor gueltig ist. Ansonsten NO_ID
     */
    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumnIndex);
        }
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        mCursor.moveToPosition(position);
        return cursorViewHolderBinder.getItemViewType(mCursor, position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    /**
     * Ist der Cursor gueltig, wird der CursorViewHolderBinder aus dem Konstructor aufgerufen
     *
     * @param viewHolder
     *         aktueller viewHolder
     * @param position
     *         position des Holders
     *
     * @throws IllegalStateException
     *         wenn der Cursor als invald erklaert wurde oder die Position vom Cursor nicht erreicht
     *         werden kann
     */
    @Override
    public void onBindViewHolder(AWLibViewHolder viewHolder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        cursorViewHolderBinder.onBindViewHolder(viewHolder, position, mCursor);
    }

    @Override
    public void onClick(AWLibViewHolder holder) {
        if (onRecyclerItemClickListener != null) {
            View v = holder.getView();
            int position = mRecyclerView.getChildAdapterPosition(v);
            long id = mRecyclerView.getChildItemId(v);
            onRecyclerItemClickListener.onRecyclerItemClick(mRecyclerView, v, position, id);
        }
    }

    /**
     * Ist der Cursor gueltig, wird der {@link CursorViewHolderBinder#onCreateViewHolder(ViewGroup,
     * int)} aus dem Konstructor aufgerufen
     */
    @Override
    public AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        AWLibViewHolder holder = cursorViewHolderBinder.onCreateViewHolder(viewGroup, itemType);
        holder.setOnClickListener(this);
        holder.setOnLongClickListener(this);
        return holder;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    @Override
    public boolean onLongClick(AWLibViewHolder holder) {
        if (onRecyclerItemLongClickListener != null) {
            View v = holder.getView();
            int position = mRecyclerView.getChildAdapterPosition(v);
            long id = mRecyclerView.getChildItemId(v);
            return onRecyclerItemLongClickListener
                    .onRecyclerItemLongClick(mRecyclerView, v, position, id);
        }
        return false;
    }

    public void setOnRecyclerItemClickListener(
            OnCursorRecyclerViewListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public void setOnRecyclerItemLongClickListener(
            OnCursorRecyclerViewListener onRecyclerItemLongClickListener) {
        this.onRecyclerItemLongClickListener = onRecyclerItemLongClickListener;
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
     * Bindet Daten eines Cursors an einen AWLibViewHolder
     */
    protected interface CursorViewHolderBinder {
        /**
         * Wird vom Adapter gerufen, um den ViewType zu ermitteln
         *
         * @param cursor
         * @param position
         *         aktuelle position in RecyclerView
         *
         * @return ViewType
         */
        int getItemViewType(Cursor cursor, int position);

        /**
         * Belegt Views eines ViewHolders mit Daten.
         *
         * @param viewHolder
         *         AWLibViewHolder
         * @param position
         *         position innerhalb RecyclerView
         * @param cursor
         *         aktueller Cursor
         */
        void onBindViewHolder(AWLibViewHolder viewHolder, int position, Cursor cursor);

        /**
         * Erstellt auf Anforderung einen neuen AWLibViewHolder anhand des listLayout fuer die
         * Liste.
         *
         * @param viewGroup
         *         ViewGroup
         * @param itemType
         *         Typ der View gemaess {@link RecyclerView.Adapter#getItemViewType(int)}
         *
         * @return neuen Viewholder
         */
        AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType);
    }

    /**
     * Observer fuer einen Cursor. Wird der Cursor invalide (z.B. durch close()), werden die Daten
     * als ungueltig erklaert.
     */
    private class AdapterDataObserver extends DataSetObserver {
        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetChanged();
        }
    }
}