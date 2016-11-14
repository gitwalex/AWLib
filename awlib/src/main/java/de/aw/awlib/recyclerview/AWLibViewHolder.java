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

package de.aw.awlib.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * AWLibViewHolder fuer RecyclerView
 */
public class AWLibViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {
    private boolean isSelectable;
    private boolean isSelected;
    private OnClickListener mOnClickListener;
    private OnLongClickListener mOnLonClickListener;

    /**
     * Erstellt AWLibViewHolder.
     *
     * @param view
     *         View fuer den Holder
     */
    public AWLibViewHolder(View view) {
        super(view);
    }

    /**
     * @param id
     *         Id der gesuchten View
     *
     * @return liefert die View zur id zurueck. Wird zur id keine View gefunden, wird null
     * zuruckgegeben.
     */
    public View findViewById(int id) {
        return itemView.findViewById(id);
    }

    /**
     * Liefert ein Tag aus der View zuruck. Es kann ein Defaultwert vorgegeben werden. dieser wird
     * zurueckgeliefert, wenn kein entsprechendes Tag in der View gefunden wird.
     *
     * @param resID
     *         resID des TAG
     * @param defaultvalue
     *         DefaultValue, der zurueckgeliefert wird, wenn kein Tag mit dieser resID gefunden
     *         wird.
     */
    public Object getTag(int resID, Object defaultvalue) {
        Object obj = itemView.getTag(resID);
        if (obj == null) {
            return defaultvalue;
        }
        return obj;
    }

    /**
     * @return View des Holders
     */
    public View getView() {
        return itemView;
    }

    /**
     * @return ob der Holder auswaelbar ist. Siehe {@link AWLibViewHolder#setSelectable(boolean)}
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
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return mOnLonClickListener != null && mOnLonClickListener.onLongClick(this);
    }

    /**
     * Setzt einen OnClickListener auf die View
     *
     * @param onClickListener
     *         OnClickListener
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        itemView.setOnClickListener(this);
    }

    /**
     * Setzt einen OnLongClickListenerauf die View
     *
     * @param onLongClickListener
     *         OnLongClickListenerauf
     */
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        mOnLonClickListener = onLongClickListener;
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
     * Wird bei Click auf View gerufen, wenn durch {@link AWLibViewHolder#setOnClickListener(OnClickListener)}
     * ein Listener gesetzt wurde.
     */
    public interface OnClickListener {
        void onClick(AWLibViewHolder holder);
    }

    /**
     * Wird bei LongClick auf View gerufen, wenn durch {@link AWLibViewHolder#setOnLongClickListener(OnLongClickListener)}
     * ein Listener gesetzt wurde.
     */
    public interface OnLongClickListener {
        boolean onLongClick(AWLibViewHolder holder);
    }
}
