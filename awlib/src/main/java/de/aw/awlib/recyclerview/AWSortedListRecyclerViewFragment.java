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
import android.support.v7.widget.RecyclerView;
import android.view.View;

import de.aw.awlib.adapters.AWSortedListRecyclerViewAdapter;
import de.aw.awlib.databinding.Model;

/**
 * Created by alex on 31.01.2017.
 */
public abstract class AWSortedListRecyclerViewFragment<T extends Model>
        extends AWBaseRecyclerViewFragment {
    private AWOnArrayRecyclerViewListener<T> mSortedListRecyclerViewListener;

    @Override
    protected abstract AWSortedListRecyclerViewAdapter createBaseAdapter();

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
    public void onRecyclerItemClick(RecyclerView rc, View v, int position, T item) {
        if (mSortedListRecyclerViewListener != null) {
            mSortedListRecyclerViewListener.onArrayRecyclerItemClick(rc, v, item);
        }
        super.onRecyclerItemClick(v, position, item.getID());
    }

    @CallSuper
    public boolean onRecyclerItemLongClick(RecyclerView rc, View v, int position, T item) {
        boolean consumed = super.onRecyclerItemLongClick(v, position, item.getID());
        if (mSortedListRecyclerViewListener != null) {
            consumed = mSortedListRecyclerViewListener.onArrayRecyclerItemLongClick(rc, v, item);
        }
        return consumed;
    }
}
