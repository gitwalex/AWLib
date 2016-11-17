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
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;

import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;

import de.aw.awlib.BuildConfig;
import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.activities.AWLibWebViewActivity;
import de.aw.awlib.database.AbstractDBConvert;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.events.AWLibEvent;
import de.aw.awlib.events.EventDBRestore;
import de.aw.awlib.events.EventTransferDB;
import de.aw.awlib.fragments.AWLibFragment;

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
                    R.string.dbSavePeriodic, R.string.MonMaCopyright, R.string.MonMaAbout,
                    R.string.BuildInfo, R.string.dbExterneSicherung, R.string.dbServerURL,
                    R.string.dbServerUID};

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
        new AsyncTask<MainAction, Void, MainAction>() {
            public String dialogTag;

            @Override
            protected MainAction doInBackground(MainAction... params) {
                MainAction action = params[0];
                switch (action) {
                    case doVaccum:
                        AbstractDBHelper.doVacuum();
                        break;
                    case doSave:
                        EventDBRestore eventSaveDB = new EventDBRestore(getActivity());
                        Date saveDate = eventSaveDB.save();
                        if (saveDate != null) {
                            setDBSaveSummary(AbstractDBConvert.convertDate(saveDate));
                        }
                        String filename = eventSaveDB.getFileName();
                        try {
                            new EventTransferDB(getContext(), EventTransferDB.ConnectionArt.SSL,
                                    filename);
                        } catch (IOException | EventTransferDB.ConnectionFailsException e) {
                            //TODO Execption bearbeiten
                            e.printStackTrace();
                        }
                        break;
                }
                return action;
            }

            /**
             * Dismiss Dialog, ggfs. Nacharbeiten fuer Aktion.
             * @param action Action
             */
            @Override
            protected void onPostExecute(MainAction action) {
                // Generell Dailog entfernen
                FragmentManager fm = getFragmentManager();
                if (fm != null) {
                    PreferenceDialogFragment dialog =
                            (PreferenceDialogFragment) getFragmentManager()
                                    .findFragmentByTag(dialogTag);
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
                Snackbar.make(getView(), action.name(), Snackbar.LENGTH_SHORT).show();
            }

            /**
             * Dialog erstellen
             */
            @Override
            protected void onPreExecute() {
                PreferenceDialogFragment dialog =
                        PreferenceDialogFragment.newInstance(title, message);
                dialogTag = dialog.getTag();
                dialog.show(getFragmentManager(), dialogTag);
            }
        }.execute(dbAction);
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
            if (pkKey == R.string.BuildInfo) {
                java.util.Date date = new Date(BuildConfig.BuildTime);
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
                StringBuilder buildInfo = new StringBuilder("Compilezeit: ").append(df.format(date))
                        .append(", Datenbankversion : ")
                        .append(AbstractDBHelper.getDatabaseVersion()).append(", Version: ")
                        .append(BuildConfig.VERSION_NAME);
                preference.setSummary(buildInfo);
            } else if (pkKey == R.string.dbServerUID || pkKey == R.string.dbServerURL) {
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
        addPreferencesFromResource(R.xml.preferences_allgemein);
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
            throw new UnsupportedOperationException("Muss noch implementiert werden!");
        } else if (getString(R.string.MonMaCopyright).equals(key)) {
            Intent intent = new Intent(getActivity(), AWLibWebViewActivity.class);
            intent.putExtra(ID, "monma_copyright.html");
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.MonMaAbout).equals(key)) {
            Intent intent = new Intent(getActivity(), AWLibWebViewActivity.class);
            intent.putExtra(ID, "monma_about.html");
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.dbExterneSicherung).equals(key)) {
            AWLibFragment f = DialogFTP.newInstance();
            f.show(getFragmentManager(), null);
            return true;
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.prefLogoutTime).equals(key)) {
            String s = sharedPreferences.getString(key, "10");
            if (TextUtils.isEmpty(s)) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key, "1").apply();
            }
        }
        super.onSharedPreferenceChanged(sharedPreferences, key);
    }

    /**
     * Liest aus den Preferences mit Key  {@link MainAction#doSave#name()} das letzte
     * Sicherungsdatum und stellt dieses in die Summary ein.
     */
    private void setDBSaveSummary(String saveDate) {
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
