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
package de.aw.awlib.preferences;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter fuer ViewPager
 */
public class AWLibPreferenceViewPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT;
    private final String tabtitles[];

    public AWLibPreferenceViewPagerAdapter(Context context, FragmentManager fm,
                                           int[] tableTitlesResIDs) {
        super(fm);
        PAGE_COUNT = tableTitlesResIDs.length;
        tabtitles = new String[PAGE_COUNT];
        int i = 0;
        for (int resID : tableTitlesResIDs) {
            tabtitles[i] = context.getString(resID);
            i++;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment f = null;
        switch (position) {
            case 0:
                f = new AWLibPreferencesAllgemein();
                break;
        }
        return f;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}