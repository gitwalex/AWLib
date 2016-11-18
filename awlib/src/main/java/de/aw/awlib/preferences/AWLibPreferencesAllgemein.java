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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

import java.sql.Date;
import java.text.DateFormat;

import de.aw.awlib.BuildConfig;
import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibActivityActions;
import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.activities.AWLibWebViewActivity;
import de.aw.awlib.application.AWLIbApplication;
import de.aw.awlib.database.AbstractDBConvert;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.events.AWLibEvent;
import de.aw.awlib.events.EventDBSave;
import de.aw.awlib.fragments.AWLibDialogHinweis;
import de.aw.awlib.fragments.AWLibFragment;
import de.aw.awlib.fragments.AWLibPreferenceFragment;

import static de.aw.awlib.activities.AWLibInterface.MainAction.doSave;

/**
 * Erstellt und bearbeitet die allgemeinen Preferences.
 *
 * @author alex
 */
public class AWLibPreferencesAllgemein extends AWLibPreferenceFragment
        implements Preference.OnPreferenceClickListener, AWLibInterface {
    private static final int[] mPrefs =
            new int[]{R.string.pkDBVacuum, R.string.pkDBSave, R.string.pkDBRestore,
                    R.string.pkSavePeriodic, R.string.pkCopyright, R.string.pkAbout,
                    R.string.pkBuildInfo, R.string.pkExterneSicherung, R.string.pkServerURL,
                    R.string.pkServerUID};

    /**
     * Fuehrt die ausgewaehlte Aktion gemaess {@link MainAction}durch.
     *
     * @param dbAction
     *         Aktion geamaess Action
     * @param title
     *         Titel des Hinweisdialogs
     * @param message
     *         Text im Dialog
     */
    private void doAction(final MainAction dbAction, final String title, final String message) {
        AWLibDialogHinweis dialog = AWLibDialogHinweis.newInstance(true, title, message);
        String dialogTag = dialog.getTag();
        dialog.show(getFragmentManager(), dialogTag);
        switch (dbAction) {
            case doSave:
                EventDBSave eventSaveDB = new EventDBSave(getActivity());
                Date saveDate = eventSaveDB.save();
                if (saveDate != null) {
                    setDBSaveSummary(AbstractDBConvert.convertDate(saveDate));
                }
                FragmentManager fm = getFragmentManager();
                if (fm != null) {
                    dialog = (AWLibDialogHinweis) getFragmentManager().findFragmentByTag(dialogTag);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Snackbar.make(getView(), dbAction.name(), Snackbar.LENGTH_SHORT).show();
                break;
            case doVaccum:
                new AsyncTask<Void, Void, Void>() {
                    public String dialogTag;

                    @Override
                    protected Void doInBackground(Void... params) {
                        AbstractDBHelper.doVacuum();
                        return null;
                    }

                    /**
                     * Dismiss Dialog, ggfs. Nacharbeiten fuer Aktion.
                     */
                    @Override
                    protected void onPostExecute(Void aVoid) {
                        // Generell Dailog entfernen
                        FragmentManager fm = getFragmentManager();
                        if (fm != null) {
                            AWLibDialogHinweis dialog = (AWLibDialogHinweis) getFragmentManager()
                                    .findFragmentByTag(dialogTag);
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                        Snackbar.make(getView(), dbAction.name(), Snackbar.LENGTH_SHORT).show();
                    }
                }.execute();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String lastSave = prefs.getString(AWLibEvent.DoDatabaseSave.name(), null);
        setDBSaveSummary(lastSave);
        for (int pkKey : mPrefs) {
            String key = getString(pkKey);
            Preference preference = findPreference(key);
            if (pkKey == R.string.pkBuildInfo) {
                java.util.Date date = new Date(BuildConfig.BuildTime);
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
                StringBuilder buildInfo = new StringBuilder("Compilezeit: ").append(df.format(date))
                        .append(", Datenbankversion : ")
                        .append(AWLIbApplication.getDatenbankVersion()).append(", Version: ")
                        .append(BuildConfig.VERSION_NAME);
                preference.setSummary(buildInfo);
            } else if (pkKey == R.string.pkServerUID || pkKey == R.string.pkServerURL) {
                String value = prefs.getString(key, null);
                preference.setSummary(value);
                preference.setOnPreferenceClickListener(this);
            } else {
                preference.setOnPreferenceClickListener(this);
            }
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.awlib_preferences_allgemein);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        String title;
        String hinweis;
        if (getString(R.string.pkDBVacuum).equals(key)) {
            title = getString(R.string.dbTitleDatenbank);
            hinweis = getString(R.string.dlgDatenbankAufraeumen);
            doAction(MainAction.doVaccum, title, hinweis);
            return true;
        } else if (getString(R.string.pkDBSave).equals(key)) {
            // Datenbank sichern
            title = getString(R.string.dbTitleDatenbank);
            hinweis = getString(R.string.dlgDatenbankSichern);
            doAction(doSave, title, hinweis);
            return true;
        } else if (getString(R.string.pkDBRestore).equals(key)) {
            Intent intent = new Intent(getActivity(), AWLibActivityActions.class);
            intent.putExtra(AWLIBACTION, (Parcelable) MainAction.doRestore);
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkCopyright).equals(key)) {
            Intent intent = new Intent(getActivity(), AWLibWebViewActivity.class);
            intent.putExtra(ID, AWLIbApplication.getCopyrightHTML());
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkAbout).equals(key)) {
            Intent intent = new Intent(getActivity(), AWLibWebViewActivity.class);
            intent.putExtra(ID, AWLIbApplication.getAboutHTML());
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkExterneSicherung).equals(key)) {
            AWLibFragment f = DialogFTP.newInstance();
            f.show(getFragmentManager(), null);
            return true;
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    /**
     * Liest aus den Preferences mit Key  {@link MainAction#doSave#name()} das letzte
     * Sicherungsdatum und stellt dieses in die Summary ein.
     */
    protected void setDBSaveSummary(String saveDate) {
        int key = R.string.pkDBSave;
        final Preference doDBSave = findPreference(getString(key));
        final StringBuilder sb = new StringBuilder(getString(R.string.lastSave)).append(": ");
        if (saveDate == null) {
            sb.append(getString(R.string.na));
        } else {
            sb.append(saveDate);
        }
        final String erg = sb.toString();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doDBSave.setSummary(erg);
            }
        });
    }
}
