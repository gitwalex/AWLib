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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import de.aw.awlib.preferences.AWLibPreferenceActivity;

public abstract class AWLibActivityMainScreen extends AWLibMainActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int layout = R.layout.awlib_activity_main_screen;
    protected DrawerLayout mDrawerLayout;
    protected ViewPager pager;
    private DrawerToggle mDrawerToggle;

    protected abstract FragmentPagerAdapter getFragmentPagerAdapter();

    protected int getNavigationMenuID() {
        return R.menu.awlib_navigationdrawer;
    }

    public abstract int getNavigationTitel();

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Drawer ist offen - close
            mDrawerLayout.closeDrawers();
        }
        if (pager != null && pager.getCurrentItem() != 0) {
            //  finish(), if the user is currently looking at the first step on this activity
            pager.setCurrentItem(pager.getCurrentItem() - 1);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.onCreate(savedInstanceState, layout);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState, int layout) {
        super.onCreate(savedInstanceState, layout);
        ActionBar bar = getSupportActionBar();
        bar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        bar.setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new DrawerToggle(this);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.inflateMenu(getNavigationMenuID());
        view.setNavigationItemSelectedListener(this);
        FragmentPagerAdapter adapter = getFragmentPagerAdapter();
        if (adapter != null) {
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setVisibility(View.VISIBLE);
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(1);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabhost_main);
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(pager);
        } else {
            findViewById(R.id.container4fragment).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.awlib_nav_Settings) {
            Intent intent = new Intent();
            intent.setClass(AWLibActivityMainScreen.this, AWLibPreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed;
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            default:
                consumed = super.onOptionsItemSelected(item);
                break;
        }
        return consumed;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDrawerLayout.removeDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private class DrawerToggle extends ActionBarDrawerToggle
            implements DrawerLayout.DrawerListener {
        private CharSequence savedSubtitel;

        public DrawerToggle(AWLibActivityMainScreen activity) {
            super(activity, mDrawerLayout, activity.getNavigationTitel(), R.string.Bearbeiten);
        }

        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            ActionBar bar = getSupportActionBar();
            bar.setTitle(AWLibActivityMainScreen.this.getNavigationTitel());
            bar.setSubtitle(savedSubtitel);
        }

        /**
         * Called when a drawer has settled in a completely open state.
         */
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            ActionBar bar = getSupportActionBar();
            bar.setTitle(R.string.Bearbeiten);
            savedSubtitel = bar.getSubtitle();
            bar.setSubtitle(null);
        }
    }
}