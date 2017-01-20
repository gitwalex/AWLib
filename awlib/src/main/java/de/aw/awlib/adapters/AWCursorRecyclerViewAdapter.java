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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.aw.awlib.application.AWApplication;
import de.aw.awlib.recyclerview.AWCursorRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Adapter fuer RecyclerView mit Cursor.
 */
public class AWCursorRecyclerViewAdapter extends AWBaseRecyclerViewAdapter
        implements AWLibViewHolder.OnClickListener, AWLibViewHolder.OnLongClickListener {
    protected final int viewHolderLayout;
    private final AdapterDataObserver mDataObserver;
    private final String mRowIDColumn;
    private final AWCursorRecyclerViewFragment mBinder;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumnIndex;
    private int removed;
    private Map<Long, Long> mItemIDs = new HashMap<>();

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
        mDataObserver = new AdapterDataObserver();
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
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        mBinder.onBindViewHolder(viewHolder, mCursor);
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
     * @return Anzahl der Element im Cursor. Ist der Cursor ungueltig, wird 0 zurueckgeliefert.
     */
    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount() - removed;
        }
        return 0;
    }

    /**
     * @return Liste der IDs der Items, die nach remove bzw. drag noch vorhanden ist.
     */
    public List<Long> getItemIDs() {
        List<Long> mItemIDList = new ArrayList<>();
        if (mDataValid && mCursor != null) {
            int size = mCursor.getCount();
            for (int i = 0; i < size; i++) {
                mCursor.moveToPosition(i);
                long mID = mCursor.getLong(mRowIdColumnIndex);
                Long value = mItemIDs.get(mID);
                if (value == null || value != NO_POSITION) {
                    mItemIDList.add(mID);
                }
            }
        }
        return mItemIDList;
    }

    /**
     * @return die ID der Position, wenn der Cursor gueltig ist. Ansonsten NO_ID
     */
    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            long mID = mCursor.getLong(mRowIdColumnIndex);
            //            while (mItemIDs.get(mID) != null) {
            //                Long mPositionID = mItemIDs.get(mID);
            //                if (mPositionID == NO_POSITION) {
            //                    position++;
            //                    mCursor.moveToPosition(position);
            //                    mID = mCursor.getLong(mRowIdColumnIndex);
            //                } else {
            //                    mID = mPositionID;
            //                    break;
            //                }
            //            }
            return mID;
        }
        return super.getItemId(position);
    }

    @Override
    protected void onItemDismiss(int position) {
        super.onItemDismiss(position);
        if (mDataValid && mCursor != null) {
            mItemIDs.put(getItemId(position), (long) NO_POSITION);
            removed++;
        }
        doLog();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(fromPosition);
            long fromID = mCursor.getLong(mRowIdColumnIndex);
            mCursor.moveToPosition(toPosition);
            long toID = mCursor.getLong(mRowIdColumnIndex);
            mItemIDs.put(fromID, toID);
            mItemIDs.put(fromID, toID);
        }
        super.onItemMove(fromPosition, toPosition);
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
        removed = 0;
        mItemIDs.clear();
        doLog();
        notifyDataSetChanged();
        return oldCursor;
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