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
package de.aw.awlib;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Template fuer Actions. Setzt in der Toolbar ein NavigationsIcon, startet die Action und
 * informiert die rufende Activity ueber {@link AWLibMainActivity#onActionFinishClicked(int, int)}.
 * Der Titel der Toolbar muss von Activity gesetzt werden.
 */
public abstract class AWLibFragmentActionBar extends AWLibFragment {
    private String mActionBarSubtitle;
    private Toolbar mToolbar;

    /**
     * @return Liefert die RessourceID des Drawables, welches neben dem Text gezeigt werden soll.
     * Als Default wird das Speichern-Symbol (R.drawable.ic_action_save) geliefert.
     */
    protected int getActionBarImageRessource() {
        return R.drawable.ic_action_save;
    }

    protected void onActionFinishClicked(int itemResID) {
        AWLibMainActivity.hide_keyboard(getActivity());
        ((AWLibMainActivity) getActivity()).onActionFinishClicked(layout, itemResID);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        bar.setHomeAsUpIndicator(getActionBarImageRessource());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar = ((AWLibMainActivity) getActivity()).getToolbar();
        mToolbar.setNavigationIcon(getActionBarImageRessource());
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWLibFragmentActionBar.this.onActionFinishClicked(getActionBarImageRessource());
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
}
