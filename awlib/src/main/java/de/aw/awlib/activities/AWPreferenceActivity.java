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
package de.aw.awlib.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import de.aw.awlib.R;
import de.aw.awlib.preferences.AWPreferencesAllgemein;

/**
 * Activity fuer Preferences
 * <p>
 * Wird {@link AWPreferenceActivity#getPreferencePagerAdapter(FragmentManager)} ueberschrieben, wird
 * der Adaper benutzt und dessen Fragmente angezeigt.
 * <p>
 * Wird kein Adapter geliefert, wird nur ein Fragment gezeigt. Welches, wird ueber {@link
 * AWPreferenceActivity#getAWLibPreferencesAllgemein()} festgelegt. Wird diese Methode nicht
 * ueberschreiben, wird das {@link AWPreferencesAllgemein} angezeigt.
 */
@SuppressWarnings("ConstantConditions")
public class AWPreferenceActivity extends AWMainActivity {
    private static final int layout = R.layout.awlib_activity_preferences;

    /**
     * Liefert ein Fragment zueruck, welches als einzelnes PreferencsFragment gezeigt wird. Wird
     * diese Methode nicht ueberschreiben, wird das {@link AWPreferencesAllgemein} angezeigt.
     *
     * @return ein AWPreferencesAllgemein-Fragment
     */
    public AWPreferencesAllgemein getAWLibPreferencesAllgemein() {
        return new AWPreferencesAllgemein();
    }

    /**
     * Liefert einen Adapter zurueck. In der Default-Implementierung wird null zurueckgeliefert,
     * dann wird das Fragment aus {@link }{@link AWPreferenceActivity#getAWLibPreferencesAllgemein()}
     * angezeigt.
     *
     * @param fm
     *         SupportFragmentManager
     *
     * @return PagerAdapter oder null (Default)
     */
    public FragmentPagerAdapter getPreferencePagerAdapter(FragmentManager fm) {
        return null;
    }

    /**
     * Diese Methode kann hier nicht verwendet werden, da die contentView festgelegt ist.
     *
     * @throws UnsupportedOperationException
     *         bei Aufruf
     */
    @Override
    protected void onCreate(Bundle savedInstanceState, int layout) {
        throw new UnsupportedOperationException("Diese Methode kann hier nicht verwendet werden");
    }

    /**
     * Wird ein Adapter in {@link AWPreferenceActivity#getPreferencePagerAdapter(FragmentManager)}
     * geliefert, wird dieser zur Anzeige der Preferences genutzt.
     * <p>
     * Andernfalls das Fragment welches in {@link AWPreferenceActivity#getAWLibPreferencesAllgemein()}
     * geliefert wird.
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, layout);
        final FragmentManager fm = getSupportFragmentManager();
        FragmentPagerAdapter adapter = getPreferencePagerAdapter(fm);
        if (adapter != null) {
            ViewPager pager = (ViewPager) findViewById(R.id.awlib_pager);
            pager.setVisibility(View.VISIBLE);
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(1);
            pager.setCurrentItem(args.getInt(LASTSELECTEDPOSITION, 0));
            TabLayout tabLayout = (TabLayout) findViewById(R.id.awlib_tabhost_main);
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(pager);
            setTitle(R.string.settings);
        } else {
            findViewById(R.id.container4fragment).setVisibility(View.VISIBLE);
            fm.beginTransaction().add(R.id.container4fragment, getAWLibPreferencesAllgemein())
                    .commit();
        }
    }
}
