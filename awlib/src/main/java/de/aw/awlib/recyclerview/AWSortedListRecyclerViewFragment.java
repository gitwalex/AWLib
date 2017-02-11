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
import android.support.annotation.CallSuper;
import android.view.View;

import de.aw.awlib.adapters.AWSortedListAdapter;
import de.aw.awlib.databinding.Model;

/**
 * Created by alex on 31.01.2017.
 */
public abstract class AWSortedListRecyclerViewFragment<T extends Model<T>>
        extends AWBaseRecyclerViewFragment {
    private AWOnArrayRecyclerViewListener<T> mSortedListRecyclerViewListener;

    @Override
    protected abstract AWSortedListAdapter createBaseAdapter();

    @Override
    public AWSortedListAdapter getAdapter() {
        return (AWSortedListAdapter) super.getAdapter();
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
            mSortedListRecyclerViewListener = (AWOnArrayRecyclerViewListener) activity;
        } catch (ClassCastException e) {
            // nix tun...
        }
    }

    @CallSuper
    public void onRecyclerItemClick(View v, int position, T item) {
        if (mSortedListRecyclerViewListener != null) {
            mSortedListRecyclerViewListener.onArrayRecyclerItemClick(mRecyclerView, v, item);
        }
        super.onRecyclerItemClick(v, position, item.getID());
    }

    @Override
    public final void onRecyclerItemClick(View view, int position, long id) {
        super.onRecyclerItemClick(view, position, id);
    }

    @Override
    public final boolean onRecyclerItemLongClick(View view, int position, long id) {
        return super.onRecyclerItemLongClick(view, position, id);
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
