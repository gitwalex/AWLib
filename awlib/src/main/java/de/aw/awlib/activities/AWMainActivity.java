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
package de.aw.awlib.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import de.aw.awlib.R;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.application.ApplicationConfig;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.views.AWBottomSheetCalculator;

/**
 * Template fuer Activities. Implementiert das globale Menu sowie die entsprechenden Reaktionen
 * darauf. Ausserdem wird dafuer gesorgt, dass bei Auswahl des MenuButtons des Geraetes der
 * OverFlow-Butten angezeigt wird. Es wird ein Bundle args bereitgestellt, welches immer gesichert
 * bzw. restored wird.
 */
public abstract class AWMainActivity extends AppCompatActivity
        implements AWInterface, View.OnClickListener {
    /**
     * Layout fuer alle Activities. Beinhaltet ein FrameLayout als container ("container") und einen
     * DetailLayout ("containerDetail").
     */
    private static final int layout = R.layout.awlib_activity_main;
    /**
     * ID fuer Fragment-Container. Hier koennen Fragmente eingehaengt werden
     */
    protected static int container;
    /**
     * Bundle fuer Argumente. Wird in SaveStateInstance gesichert und in onCreate
     * wiederhergestellt.
     */
    protected final Bundle args = new Bundle();
    protected AbstractDBHelper mDBHelper;
    /**
     * Default FloatingActionButton. Rechts unten, Icon ist 'Add', standardmaessig View.GONE
     */
    private FloatingActionButton mDefaultFAB;
    private Toolbar mToolbar;
    private MainAction mainAction;

    public FloatingActionButton getDefaultFAB() {
        return mDefaultFAB;
    }

    /**
     * Als Default wird hier nohelp zurueckgeliefert.
     *
     * @return ein Helpfile unter /assets/html
     */
    protected String getHelpFile() {
        return "nohelp.html";
    }

    public MainAction getMainAction() {
        return mainAction;
    }

    /**
     * @return Liefert die Toolbar der View zurueck.
     */
    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * Hides a Keyboard
     *
     * @see "stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard"
     */
    public void hide_keyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
    }

    /**
     * Allgemeine Aufgaben fuer onCreate: - rufen von onCreate(Bundle, layout) mit Standardlayout. -
     * ContentView ist container in activity_container
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, layout);
    }

    /**
     * - Initialisiert IntermediateProgress in ActionBar -  ermitteln der gesicherten Argumente -
     * HomeButton intialisieren - Ist der DetailContainer Visible, wird er auch (wieder) angezeigt.
     */
    protected void onCreate(Bundle savedInstanceState, int layout) {
        container = R.id.container4fragment;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            args.putAll(extras);
        }
        String nextActivity = args.getString(NEXTACTIVITY);
        if (nextActivity != null) {
            try {
                Intent intent = new Intent(this, Class.forName(nextActivity));
                intent.putExtras(args);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
            }
        }
        super.onCreate(savedInstanceState);
        ApplicationConfig mAppConfig =
                ((AWApplication) getApplicationContext()).getApplicationConfig();
        mDBHelper = mAppConfig.getDBHelper();
        setContentView(layout);
        mainAction = args.getParcelable(AWLIBACTION);
        mDefaultFAB = (FloatingActionButton) findViewById(R.id.awlib_defaultFAB);
        mToolbar = (Toolbar) findViewById(R.id.awlib_toolbar);
        setSupportActionBar(mToolbar);
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
            setSubTitle(args.getCharSequence(ACTIONBARSUBTITLE));
        }
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        bar.setHomeButtonEnabled(true);
        supportInvalidateOptionsMenu();
    }

    /**
     * Installiert ein Menu mit folgenden Items:
     * <p>
     * Rechner
     * <p>
     * Hilfe
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.awlib_activity_menu, menu);
        return true;
    }

    /**
     * Reagiert auf die MenuItems.
     * <p>
     * Bie Rechner wird ein BottomSheet mit einem Rechner gezeigt, bei Hilfe startet eine WebView
     * mit einem Hilfetext. Die ID des Hilfetextes wird ueber
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isConsumed = false;
        Intent intent;
        int i = item.getItemId();
        if (i == R.id.awlib_menu_item_calculator) {
            AWBottomSheetCalculator calc = new AWBottomSheetCalculator();
            calc.show(getSupportFragmentManager(), null);
            isConsumed = true;
        } else if (i == R.id.awlib_menu_item_hilfe) {
            intent = new Intent(this, AWWebViewActivity.class);
            intent.putExtra(ID, getHelpFile());
            startActivity(intent);
            isConsumed = true;
        } else if (i == android.R.id.home) {
            setResult(RESULT_OK);
        }
        return isConsumed;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSubTitle(args.getString(ACTIONBARSUBTITLE));
    }

    /**
     * Sicherung aller Argumente
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putAll(args);
        super.onSaveInstanceState(outState);
    }

    /**
     * Setzt den SubTitle in der SupportActionBar
     *
     * @param subTitle
     *         Text des Subtitles
     */
    public void setSubTitle(CharSequence subTitle) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setSubtitle(subTitle);
        }
        args.putCharSequence(ACTIONBARSUBTITLE, subTitle);
    }

    /**
     * Setzt den SubTitle in der SupportActionBar
     *
     * @param subTitleResID
     *         resID des Subtitles
     */
    public void setSubTitle(int subTitleResID) {
        setSubTitle(getString(subTitleResID));
    }
}