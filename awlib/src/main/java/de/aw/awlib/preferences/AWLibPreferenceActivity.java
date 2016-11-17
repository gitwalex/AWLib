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
/**
 *
 */
package de.aw.awlib.preferences;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibMainActivity;

/**
 * @author alex
 */
public class AWLibPreferenceActivity extends AWLibMainActivity {
    private static final int layout = R.layout.awlib_activity_preferences;
    private static final int[] tableTitlesResIDs =
            new int[]{R.string.TitleAllgemeinPrefs, R.string.TitleFinanzuebersichtPrefs,
                    R.string.TitleHBCIPrefs, R.string.TitleWertpapieruebersichtPrefs,
                    R.string.TitleImportPrefs};
    private ViewPager pager;

    public FragmentPagerAdapter getPreferencePagerAdapter(FragmentManager fm) {
        return new AWLibPreferenceViewPagerAdapter(this, fm, tableTitlesResIDs);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, layout);
        pager = (ViewPager) findViewById(R.id.awlib_pager);
        final FragmentManager fm = getSupportFragmentManager();
        FragmentPagerAdapter adapter = getPreferencePagerAdapter(fm);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(tableTitlesResIDs.length);
        pager.setCurrentItem(args.getInt(LASTSELECTEDPOSITION, 0));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.awlib_tabhost_main);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(pager);
        getSupportActionBar().setTitle(R.string.settings);
    }
}
