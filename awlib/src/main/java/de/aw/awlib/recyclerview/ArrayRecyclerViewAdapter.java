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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

/**
 * Adapter fuer RecyclerView mit List oder Array.
 */
public class ArrayRecyclerViewAdapter<T> extends RecyclerView.Adapter<AWLibViewHolder>
        implements AWLibViewHolder.OnClickListener, AWLibViewHolder.OnLongClickListener {
    private final ArrayViewHolderBinder<T> arrayViewHolderBinder;
    private RecyclerView mRecyclerView;
    private List<T> mValues;
    private OnArrayRecyclerViewListener onRecyclerItemClickListener;
    private OnArrayRecyclerViewListener onRecyclerItemLongClickListener;

    /**
     * Initialisiert Adapter.
     *
     * @param binder
     *         ArrayViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
     */
    protected ArrayRecyclerViewAdapter(@NonNull ArrayViewHolderBinder<T> binder) {
        arrayViewHolderBinder = binder;
    }

    /**
     * @return Anzahl der Element in mValues. Ist mValues null, wird 0 zurueckgeliefert.
     */
    @Override
    public int getItemCount() {
        return (mValues != null) ? mValues.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return arrayViewHolderBinder.getItemViewType(position, mValues.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    /**
     * Hat mValues genug Elemente, wird der ArrayViewHolderBinder aus dem Konstructor aufgerufen
     *
     * @param viewHolder
     *         aktueller viewHolder
     * @param position
     *         position des Holders
     *
     * @throws IndexOutOfBoundsException
     *         wenn nicht genuegend Elemente in mValues
     */
    @Override
    public void onBindViewHolder(AWLibViewHolder viewHolder, int position) {
        arrayViewHolderBinder.onBindViewHolder(viewHolder, mValues.get(position));
    }

    @Override
    public void onClick(AWLibViewHolder holder) {
        if (onRecyclerItemClickListener != null) {
            View v = holder.getView();
            int position = mRecyclerView.getChildAdapterPosition(v);
            T object = mValues.get(position);
            onRecyclerItemClickListener.onArrayRecyclerItemClick(mRecyclerView, v, object);
        }
    }

    /**
     * Ist der Cursor gueltig, wird der {@link ArrayViewHolderBinder#onCreateViewHolder(ViewGroup,
     * int)} aus dem Konstructor aufgerufen
     */
    @Override
    public AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        AWLibViewHolder holder = arrayViewHolderBinder.onCreateViewHolder(viewGroup, itemType);
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
            T object = mValues.get(position);
            return onRecyclerItemLongClickListener
                    .onArrayRecyclerItemLongClick(mRecyclerView, v, object);
        }
        return false;
    }

    public void setOnRecyclerItemClickListener(
            OnArrayRecyclerViewListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public void setOnRecyclerItemLongClickListener(
            OnArrayRecyclerViewListener onRecyclerItemLongClickListener) {
        this.onRecyclerItemLongClickListener = onRecyclerItemLongClickListener;
    }

    /**
     * Setzt ein neues Array mit Werten
     *
     * @param newValues
     *         Array mit Werten
     */
    public final void swapValues(@NonNull T[] newValues) {
        swapValues(Arrays.asList(newValues));
    }

    /**
     * Setzt eine List mit Werten
     *
     * @param value
     *         ArrayList mit Werten
     */
    public void swapValues(List<T> value) {
        mValues = value;
        notifyDataSetChanged();
    }

    /**
     * Bindet Daten eines Cursors an einen AWLibViewHolder
     */
    protected interface ArrayViewHolderBinder<T> {
        /**
         * Wird vom Adapter gerufen, um den ViewType zu ermitteln
         *
         * @param object
         *         aus mValues
         *
         * @return ViewType
         */
        int getItemViewType(int position, T object);

        /**
         * Belegt Views eines ViewHolders mit Daten.
         *
         * @param viewHolder
         *         AWLibViewHolder
         * @param object
         *         Object aus mValues
         */
        void onBindViewHolder(AWLibViewHolder viewHolder, T object);

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
}