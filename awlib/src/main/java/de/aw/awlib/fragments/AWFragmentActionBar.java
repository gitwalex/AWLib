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
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibMainActivity;

/**
 * Template fuer Actions. Setzt in der Toolbar ein NavigationsIcon, startet die Action und
 * informiert die rufende Activity ueber {@link AWLibMainActivity#onActionFinishClicked(int, int)}.
 * Der Titel der Toolbar muss von Activity gesetzt werden.
 */
public abstract class AWFragmentActionBar extends AWFragment {
    private String mActionBarSubtitle;
    private OnActionFinishListener mOnActionFinishClickedListener;
    private Toolbar mToolbar;

    /**
     * @return Liefert die RessourceID des Drawables, welches neben dem Text gezeigt werden soll.
     * Als Default wird das Speichern-Symbol (R.drawable.ic_action_save) geliefert.
     */
    protected int getActionBarImageRessource() {
        return R.drawable.ic_action_save;
    }

    protected void onActionFinishClicked(int itemResID) {
        mOnActionFinishClickedListener.onActionFinishClicked(layout, itemResID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        bar.setHomeAsUpIndicator(getActionBarImageRessource());
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar = ((AWLibMainActivity) getActivity()).getToolbar();
        mToolbar.setNavigationIcon(getActionBarImageRessource());
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWFragmentActionBar.this.onActionFinishClicked(getActionBarImageRessource());
            }
        });
        mToolbar.setSubtitle(mActionBarSubtitle);
    }

    protected void setActionBarSubTitle(String title) {
        mActionBarSubtitle = title;
        if (mToolbar != null) {
            mToolbar.setSubtitle(title);
        }
    }

    protected void setActionBarSubTitle(int resID) {
        setActionBarSubTitle(getString(resID));
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
         * @param itemResiD
         *         resID, die geclicked wurde
         */
        void onActionFinishClicked(@LayoutRes int layout, @IntegerRes int itemResiD);
    }
}
