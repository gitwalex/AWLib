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

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * AWLibViewHolder fuer RecyclerView
 */
public class AWLibViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private boolean isSelectable;
    private boolean isSelected;
    private OnHolderClickListener mOnHolderClickListener;
    private OnHolderLongClickListener mOnLonClickListener;
    private ViewDataBinding viewDataBinding;

    /**
     * Erstellt AWLibViewHolder.
     *
     * @param view
     *         View fuer den Holder
     */
    public AWLibViewHolder(View view) {
        super(view);
    }

    public ViewDataBinding getViewDataBinding() {
        if (viewDataBinding == null) {
            viewDataBinding = DataBindingUtil.bind(itemView);
        }
        return viewDataBinding;
    }

    /**
     * @return ob der Holder auswaehlbar ist. Siehe {@link AWLibViewHolder#setSelectable(boolean)}
     */
    public boolean isSelectable() {
        return isSelectable;
    }

    /**
     * @return ob der Holder durch eine vorherige Selection ausgewaehlt ist. Siehe {@link
     * AWLibViewHolder#setSelected(boolean)}
     */
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void onClick(View v) {
        if (mOnHolderClickListener != null) {
            mOnHolderClickListener.onViewHolderClick(this);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return mOnLonClickListener != null && mOnLonClickListener.onViewHolderLongClick(this);
    }

    /**
     * Setzt einen OnHolderClickListener auf die View
     *
     * @param onHolderClickListener
     *         OnHolderClickListener
     */
    public void setOnClickListener(OnHolderClickListener onHolderClickListener) {
        mOnHolderClickListener = onHolderClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Setzt einen OnLongClickListenerauf die View
     *
     * @param onHolderLongClickListener
     *         OnLongClickListenerauf
     */
    public void setOnLongClickListener(OnHolderLongClickListener onHolderLongClickListener) {
        mOnLonClickListener = onHolderLongClickListener;
        itemView.setOnLongClickListener(this);
    }

    /**
     * Setzt ein Flag, ob der Holder selecatble ist.
     *
     * @param selectable
     *         Flag. true: ist selectable
     */
    public void setSelectable(boolean selectable) {
        isSelectable = selectable;
    }

    /**
     * Setzt ein Flag, ob der Holder selectiert wurde. Es wird itemView.setSelected(flag)
     * durchgefuehrt
     *
     * @param isSelected
     *         Flag. true: ist selectiert
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        itemView.setSelected(isSelected);
    }

    /**
     * Setzt das uebergebene Tag mit dem Key direkt in der View.
     *
     * @param resID
     *         resID des TAG
     * @param object
     *         TAG
     */
    public void setTag(int resID, Object object) {
        itemView.setTag(resID, object);
    }

    /**
     * Wird bei Click auf View gerufen, wenn durch {@link AWLibViewHolder#setOnClickListener(OnHolderClickListener)}
     * ein Listener gesetzt wurde.
     */
    public interface OnHolderClickListener {
        void onViewHolderClick(AWLibViewHolder holder);
    }

    /**
     * Wird bei LongClick auf View gerufen, wenn durch {@link AWLibViewHolder#setOnLongClickListener(OnHolderLongClickListener)}
     * ein Listener gesetzt wurde.
     */
    public interface OnHolderLongClickListener {
        boolean onViewHolderLongClick(AWLibViewHolder holder);
    }
}
