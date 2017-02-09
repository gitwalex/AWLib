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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

import de.aw.awlib.recyclerview.AWLibViewHolder;
import de.aw.awlib.recyclerview.AWOnArrayRecyclerViewListener;

/**
 * Adapter fuer RecyclerView mit List oder Array.
 */
public class AWArrayAdapter<T> extends RecyclerView.Adapter<AWLibViewHolder>
        implements AWLibViewHolder.OnHolderClickListener,
        AWLibViewHolder.OnHolderLongClickListener {
    private final ArrayViewHolderBinder<T> arrayViewHolderBinder;
    private RecyclerView mRecyclerView;
    private List<T> mValues;
    private AWOnArrayRecyclerViewListener<T> onRecyclerItemClickListener;
    private AWOnArrayRecyclerViewListener<T> onRecyclerItemLongClickListener;

    /**
     * Initialisiert Adapter.
     *
     * @param binder
     *         ArrayViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
     */
    public AWArrayAdapter(@NonNull ArrayViewHolderBinder<T> binder) {
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
     * @throws IndexOutOfBoundsException
     *         wenn nicht genuegend Elemente in mValues
     */
    @Override
    public void onBindViewHolder(AWLibViewHolder viewHolder, int position) {
        arrayViewHolderBinder.onBindViewHolder(viewHolder, mValues.get(position));
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
    public void onViewHolderClick(AWLibViewHolder holder) {
        if (onRecyclerItemClickListener != null) {
            View v = holder.itemView;
            int position = mRecyclerView.getChildAdapterPosition(v);
            T object = mValues.get(position);
            onRecyclerItemClickListener.onArrayRecyclerItemClick(mRecyclerView, v, object);
        }
    }

    @Override
    public boolean onViewHolderLongClick(AWLibViewHolder holder) {
        if (onRecyclerItemLongClickListener != null) {
            View v = holder.itemView;
            int position = mRecyclerView.getChildAdapterPosition(v);
            T object = mValues.get(position);
            return onRecyclerItemLongClickListener
                    .onArrayRecyclerItemLongClick(mRecyclerView, v, object);
        }
        return false;
    }

    public void setOnRecyclerItemClickListener(
            AWOnArrayRecyclerViewListener<T> onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public void setOnRecyclerItemLongClickListener(
            AWOnArrayRecyclerViewListener<T> onRecyclerItemLongClickListener) {
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
    public interface ArrayViewHolderBinder<T> {
        /**
         * Wird vom Adapter gerufen, um den ViewType zu ermitteln
         *
         * @param object
         *         aus mValues
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
         * @return neuen Viewholder
         */
        AWLibViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType);
    }
}