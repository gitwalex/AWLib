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

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.CallSuper;
import android.support.v4.content.Loader;
import android.view.View;

import de.aw.awlib.adapters.AWSortedListAdapter;
import de.aw.awlib.databinding.SortedListModel;

/**
 * Template fuer eine RecyclerView mit {@link AWSortedListAdapter<T>}
 */
public abstract class AWSortedListRecyclerViewFragment<T extends SortedListModel<T>>
        extends AWBaseRecyclerViewFragment
        implements AWSortedListAdapter.AWSortedListRecyclerViewBinder<T> {
    private AWOnArrayRecyclerViewListener<T> mSortedListRecyclerViewListener;
    private AWSortedListAdapter<T> mAdapter;

    @Override
    protected final AWSortedListAdapter<T> createBaseAdapter() {
        mAdapter = createSortedListAdapter();
        return mAdapter;
    }

    protected abstract AWSortedListAdapter<T> createSortedListAdapter();

    /**
     * @return Liefert den Adapter zurueck
     */
    public AWSortedListAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = createBaseAdapter();
        }
        return mAdapter;
    }

    /**
     * Activity kann (muss aber nicht) AWSortedListRecyclerViewListener implementieren. In diesem
     * Fall wird die entsprechende Methode bei Bedarf aufgerufen.
     *
     * @see AWBaseRecyclerViewListener
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mSortedListRecyclerViewListener = (AWOnArrayRecyclerViewListener<T>) activity;
        } catch (ClassCastException e) {
            // nix tun...
        }
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder holder, T item, int position) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getAdapter().reset();
    }

    @CallSuper
    public void onRecyclerItemClick(View v, int position, T item) {
        if (mSortedListRecyclerViewListener != null) {
            mSortedListRecyclerViewListener.onArrayRecyclerItemClick(mRecyclerView, v, item);
        }
        super.onRecyclerItemClick(v, position, item.getID());
    }

    @CallSuper
    public boolean onRecyclerItemLongClick(View v, int position, T item) {
        boolean consumed = super.onRecyclerItemLongClick(v, position, item.getID());
        if (mSortedListRecyclerViewListener != null) {
            consumed = mSortedListRecyclerViewListener
                    .onArrayRecyclerItemLongClick(mRecyclerView, v, item);
        }
        return consumed;
    }
}
