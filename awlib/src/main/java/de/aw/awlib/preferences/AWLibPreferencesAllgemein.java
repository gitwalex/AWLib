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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
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
import de.aw.awlib.database.AWLibDBConvert;
import de.aw.awlib.events.AWLibEvent;
import de.aw.awlib.events.AWLibEventService;
import de.aw.awlib.events.EventDBSave;
import de.aw.awlib.fragments.AWLibPreferenceFragment;

import static de.aw.awlib.events.AWLibEvent.DoDatabaseSave;

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
    private Preference regelmSicherung;

    /**
     * Fuehrt die ausgewaehlte Aktion gemaess {@link AWLibEvent}durch.
     *
     * @param event
     *         Aktion geamaess Action
     * @param title
     *         Titel des Hinweisdialogs
     * @param message
     *         Text im Dialog
     */
    private void doAction(final AWLibEvent event, final String title, final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.awlib_btnAccept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        Dialog dlg = builder.create();
        dlg.show();
        Intent intent = new Intent(getContext(), AWLibEventService.class);
        intent.putExtra(AWLIBEVENT, (Parcelable) event);
        getContext().startService(intent);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.awlib_preferences_allgemein);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
            } else if (pkKey == R.string.pkSavePeriodic) {
                setRegelmSicherungSummary(preference, prefs);
            }
            preference.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        String title;
        String hinweis;
        if (getString(R.string.pkDBVacuum).equals(key)) {
            title = getString(R.string.dbTitleDatenbank);
            hinweis = getString(R.string.dlgDatenbankAufraeumen);
            doAction(AWLibEvent.doVaccum, title, hinweis);
            return true;
        } else if (getString(R.string.pkDBSave).equals(key)) {
            // Datenbank sichern
            title = getString(R.string.dbTitleDatenbank);
            hinweis = getString(R.string.dlgDatenbankSichern);
            doAction(DoDatabaseSave, title, hinweis);
            return true;
        } else if (getString(R.string.pkDBRestore).equals(key)) {
            Intent intent = new Intent(getActivity(), AWLibActivityActions.class);
            intent.putExtra(AWLIBEVENT, (Parcelable) AWLibEvent.showBackupFiles);
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
            Intent intent = new Intent(getActivity(), AWLibActivityActions.class);
            intent.putExtra(AWLIBEVENT, (Parcelable) AWLibEvent.configRemoteFileServer);
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkSavePeriodic).equals(key)) {
            EventDBSave.checkDBSaveAlarm(getContext(), preference.getSharedPreferences());
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.nextDoDBSave))) {
            Preference pref = findPreference(getString(R.string.pkSavePeriodic));
            setRegelmSicherungSummary(pref, sharedPreferences);
        }
    }

    private void setRegelmSicherungSummary(Preference pref, SharedPreferences prefs) {
        if (prefs.getBoolean(getString(R.string.pkSavePeriodic), false)) {
            long value = prefs.getLong(getString(R.string.nextDoDBSave), 0);
            String date = AWLibDBConvert.convertDate(value);
            pref.setSummary(getString(R.string.smryDBSavePeriodicOn) + date);
        } else {
            pref.setSummary(prefs.getString(getString(R.string.nextDoDBSave),
                    getString(R.string.smryDBSavePeriodic)));
        }
    }
}
