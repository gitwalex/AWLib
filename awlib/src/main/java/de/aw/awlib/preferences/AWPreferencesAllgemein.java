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

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.DateFormat;

import de.aw.awlib.BuildConfig;
import de.aw.awlib.R;
import de.aw.awlib.activities.AWActivityActions;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.activities.AWWebViewActivity;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.database.AWDBConvert;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.events.AWEvent;
import de.aw.awlib.events.AWEventService;
import de.aw.awlib.events.EventDBSave;
import de.aw.awlib.fragments.AWPreferenceFragment;

import static de.aw.awlib.application.AWApplication.DE_AW_APPLICATIONPATH;
import static de.aw.awlib.events.AWEvent.DoDatabaseSave;
import static de.aw.awlib.events.AWEvent.copyAndDebugDatabase;
import static de.aw.awlib.events.AWEvent.doVaccum;

/**
 * Erstellt und bearbeitet die allgemeinen Preferences.
 *
 * @author alex
 */
public class AWPreferencesAllgemein extends AWPreferenceFragment
        implements Preference.OnPreferenceClickListener, AWInterface {
    private static final int[] mPrefs =
            new int[]{R.string.pkDBVacuum, R.string.pkDBSave, R.string.pkDBRestore,
                    R.string.pkSavePeriodic, R.string.pkCopyright, R.string.pkAbout,
                    R.string.pkCompileInfo, R.string.pkVersionInfo, R.string.pkExterneSicherung,
                    R.string.pkServerURL, R.string.pkServerUID};
    private AWApplication mApplication;
    private AWEvent pendingEvent;
    private Preference regelmSicherung;

    private void buildAndShowDialog(int titleRes, int messageRes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(titleRes);
        builder.setMessage(messageRes);
        builder.setPositiveButton(R.string.awlib_btnAccept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        Dialog dlg = builder.create();
        dlg.show();
    }

    private void copyAndDebugDatabase() throws IOException {
        mApplication.createFiles();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        AWApplication app = (AWApplication) getContext().getApplicationContext();
        String databasePath = app.getApplicationDatabasePath();
        File src = app.getDatabasePath(app.theDatenbankname());
        File dest = new File(databasePath + File.separator + app.theDatenbankname());
        copyFile(src, dest);
        Uri uri = Uri.parse("sqlite:" + dest.getAbsolutePath());
        intent.setData(uri);
        startActivity(intent);
    }

    private void copyFile(File src, File dest) throws IOException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dest).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    /**
     * Fuehrt die ausgewaehlte Aktion gemaess {@link AWEvent}durch.
     *
     * @param event
     *         Aktion geamaess Action
     */
    private void doAction(final AWEvent event) {
        switch (event) {
            case DoDatabaseSave:
                int permission = ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    startDBSave(event);
                } else {
                    pendingEvent = event;
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_PERMISSION_STORAGE);
                }
                break;
            case doVaccum:
                buildAndShowDialog(R.string.dbTitleDatenbank, R.string.dlgDatenbankAufraeumen);
                Intent intent = new Intent(getContext(), AWEventService.class);
                intent.putExtra(AWLIBEVENT, (Parcelable) event);
                getContext().startService(intent);
                break;
        }
    }

    @CallSuper
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        mApplication = ((AWApplication) getActivity().getApplicationContext());
        addPreferencesFromResource(R.xml.awlib_preferences_allgemein);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        for (int pkKey : mPrefs) {
            String key = getString(pkKey);
            Preference preference = findPreference(key);
            if (pkKey == R.string.pkCompileInfo) {
                java.util.Date date = new Date(BuildConfig.BuildTime);
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
                StringBuilder buildInfo =
                        new StringBuilder("Compilezeit: ").append(df.format(date));
                preference.setSummary(buildInfo);
            } else if (pkKey == R.string.pkVersionInfo) {
                StringBuilder versionInfo = new StringBuilder("Datenbankversion : ")
                        .append(mApplication.theDatenbankVersion()).append(", Version: ")
                        .append(BuildConfig.VERSION_NAME);
                preference.setSummary(versionInfo);
            } else if (pkKey == R.string.pkServerUID || pkKey == R.string.pkServerURL) {
                String value = prefs.getString(key, null);
                preference.setSummary(value);
            } else if (pkKey == R.string.pkSavePeriodic) {
                setRegelmSicherungSummary(preference, prefs);
            }
            preference.setOnPreferenceClickListener(this);
        }
    }

    @CallSuper
    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (getString(R.string.pkDBVacuum).equals(key)) {
            doAction(doVaccum);
            return true;
        } else if (getString(R.string.pkDBSave).equals(key)) {
            // Datenbank sichern
            doAction(DoDatabaseSave);
            return true;
        } else if (getString(R.string.pkDBRestore).equals(key)) {
            Intent intent = new Intent(getActivity(), AWActivityActions.class);
            intent.putExtra(AWLIBEVENT, (Parcelable) AWEvent.showBackupFiles);
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkCopyright).equals(key)) {
            Intent intent = new Intent(getActivity(), AWWebViewActivity.class);
            intent.putExtra(ID, mApplication.getCopyrightHTML());
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkAbout).equals(key)) {
            Intent intent = new Intent(getActivity(), AWWebViewActivity.class);
            intent.putExtra(ID, mApplication.getAboutHTML());
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkExterneSicherung).equals(key)) {
            Intent intent = new Intent(getActivity(), AWActivityActions.class);
            intent.putExtra(AWLIBEVENT, (Parcelable) AWEvent.showRemoteFileServer);
            getActivity().startActivity(intent);
            return true;
        } else if (getString(R.string.pkVersionInfo).equals(key)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view =
                    LayoutInflater.from(getContext()).inflate(R.layout.awlib_pref_dbversion, null);
            final EditText etVersion = (EditText) view.findViewById(R.id.etVersion);
            builder.setView(view);
            int oldVersion = AbstractDBHelper.getInstance().getReadableDatabase().getVersion();
            builder.setTitle("Aktuelle Version" + oldVersion);
            builder.setPositiveButton(R.string.awlib_btnAccept,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int newVersion = Integer.parseInt(etVersion.getText().toString());
                            AbstractDBHelper.getInstance().getWritableDatabase()
                                    .setVersion(newVersion);
                        }
                    });
            builder.create().show();
            return true;
        } else if (getString(R.string.pkSavePeriodic).equals(key)) {
            EventDBSave.checkDBSaveAlarm(getContext(), preference.getSharedPreferences());
            return true;
        } else if (getString(R.string.pkCompileInfo).equals(key)) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    copyAndDebugDatabase();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                pendingEvent = copyAndDebugDatabase;
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_STORAGE);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        switch (pendingEvent) {
                            case DoDatabaseSave:
                                startDBSave(pendingEvent);
                                break;
                            case copyAndDebugDatabase:
                                try {
                                    copyAndDebugDatabase();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                    i++;
                }
        }
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
            String date = AWDBConvert.convertDate(value);
            pref.setSummary(getString(R.string.smryDBSavePeriodicOn) + date);
        } else {
            pref.setSummary(prefs.getString(getString(R.string.nextDoDBSave),
                    getString(R.string.smryDBSavePeriodic)));
        }
    }

    private void startDBSave(AWEvent event) {
        buildAndShowDialog(R.string.dbTitleDatenbank, R.string.dlgDatenbankSichern);
        File folder = new File(DE_AW_APPLICATIONPATH);
        if (!folder.exists()) {
            folder.mkdir();
        }
        mApplication.createFiles();
        Intent intent = new Intent(getContext(), AWEventService.class);
        intent.putExtra(AWLIBEVENT, (Parcelable) event);
        getContext().startService(intent);
    }
}
