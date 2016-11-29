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
package de.aw.awlib.fragments;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWMainActivity;

/**
 * Template fuer Actions. Setzt in der Toolbar ein NavigationsIcon, startet die Action und
 * informiert die rufende Activity ueber {@link AWMainActivity#onActionFinishClicked(int)}. Der
 * Titel der Toolbar muss von Activity gesetzt werden.
 */
public abstract class AWFragmentActionBar extends AWFragment {
    private OnActionFinishListener mOnActionFinishClickedListener;

    /**
     * Wird gerufen, wenn das Menuitem 'Save' gewaehlt wurde. Ruft die Activity mit der layoutID
     */
    @CallSuper
    protected void onActionFinishClicked() {
        mOnActionFinishClickedListener.onActionFinishClicked(layout);
    }

    /**
     * Context muss OnActionFinishClickedListener implementieren. Ist das nicht der Fall, gibt es
     * eine IllegalStateException
     *
     * @throws IllegalStateException
     *         wenn OnActionFinishClickedListener nicht implementiert.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnActionFinishClickedListener = (OnActionFinishListener) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass()
                    .getSimpleName() + " muss OnActionFinishClickedListener implementieren");
        }
    }

    /**
     * Fuegt ein Save-MenuItem hinzu
     */
    @CallSuper
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.awlib_action_save, menu);
    }

    /**
     * Wird 'Save' gewaehlt, wird {@link AWFragmentActionBar#onActionFinishClicked()} gerufen.
     */
    @CallSuper
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.awlib_menu_item_save) {
            onActionFinishClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Interface fuer rufende Activity. Muss implementiert werden.
     */
    public interface OnActionFinishListener {
        /**
         * Methode wird von Fragmemt gerufen, wenn eine Action beendet wird.
         *
         * @param layout
         *         layoutID des Fragments
         */
        void onActionFinishClicked(@LayoutRes int layout);
    }
}
