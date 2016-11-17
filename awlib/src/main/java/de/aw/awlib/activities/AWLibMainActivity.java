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
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import de.aw.awlib.R;
import de.aw.awlib.views.AWLibBottomSheetCalculator;

/**
 * Template fuer Activities. Implementiert das globale Menu sowie die entsprechenden Reaktionen
 * darauf. Ausserdem wird dafuer gesorgt, dass bei Auswahl des MenuButtons des Geraetes der
 * OverFlow-Butten angezeigt wird. Es wird ein Bundle args bereitgestellt, welches immer gesichert
 * bzw. restored wird.
 */
public abstract class AWLibMainActivity extends AppCompatActivity
        implements AWLibInterface, View.OnClickListener {
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
    /**
     * Default FloatingActionButton. Rechts unten, Icon ist 'Add', standardmaessig View.GONE
     */
    private FloatingActionButton mDefaultFAB;
    private Toolbar mToolbar;
    private MainAction mainAction;

    /**
     * Hides a Keyboard
     *
     * @param activity
     *         current Activity
     *
     * @see "stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard"
     */
    public static void hide_keyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token
        // from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

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
     * Methode wird von Fragmemt gerufen, wenn eine Action beendet wird. Als Default wird bei
     * Auswahl des ActionFinishedButton der Subtitle in der Toolbar sowie das Fragment vom Stack
     * gepopt / entfernt
     *
     * @param layoutID
     *         layoutID des Fragments
     * @param itemResID
     *         itemResID, die ausgewaehlt wurde, wenn eine Action durch Usereingriff beendet wurde
     */
    @CallSuper
    public void onActionFinishClicked(int layoutID, int itemResID) {
        getToolbar().setSubtitle(null);
        getToolbar().setNavigationIcon(null);
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
        setContentView(layout);
        mainAction = args.getParcelable(AWLIBACTION);
        mDefaultFAB = (FloatingActionButton) findViewById(R.id.awlib_defaultFAB);
        mToolbar = (Toolbar) findViewById(R.id.awlib_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        if (savedInstanceState != null) {
            args.putAll(savedInstanceState);
            bar.setTitle(args.getString(ACTIONBARTITLE));
        }
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
            AWLibBottomSheetCalculator calc = new AWLibBottomSheetCalculator();
            calc.show(getSupportFragmentManager(), null);
            isConsumed = true;
        } else if (i == R.id.awlib_menu_item_hilfe) {
            intent = new Intent(this, AWLibWebViewActivity.class);
            intent.putExtra(ID, getHelpFile());
            startActivity(intent);
            isConsumed = true;
        } else if (i == android.R.id.home) {
            setResult(RESULT_OK);
        }
        return isConsumed;
    }

    /**
     * Sicherung aller Argumente sowie des aktuellen AppTitels
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        CharSequence title = getSupportActionBar().getTitle();
        if (!TextUtils.isEmpty(title)) {
            args.putCharSequence(ACTIONBARTITLE, title);
        }
        outState.putAll(args);
        super.onSaveInstanceState(outState);
    }
}
