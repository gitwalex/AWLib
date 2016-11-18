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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import de.aw.awlib.R;
import de.aw.awlib.application.AWLIbApplication;
import de.aw.awlib.events.EventDBRestore;
import de.aw.awlib.filechooser.FragmentFileChooser;
import de.aw.awlib.fragments.AWLibDialogHinweis;

/**
 * Activity fuer verschiedene Aktionen
 */
public class AWLibActivityActions extends AWLibMainActivity
        implements DialogInterface.OnClickListener, FragmentFileChooser.FileChooserListener {
    @Override
    public void onClick(DialogInterface dialog, int which) {
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getMainAction() == null) {
            finish();
        } else {
            Integer titleResID = null;
            if (savedInstanceState == null) {
                Fragment f;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                switch (getMainAction()) {
                    case doRestore:
                        // Datenbank wiederherstellen
                        String backupFolderName = AWLIbApplication.getApplicationBackupPath();
                        f = FragmentFileChooser.newInstance(backupFolderName);
                        titleResID = R.string.fileChooserTitle;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Kein Fragment fuer " + getMainAction().name() + " vorgesehen");
                }
                if (f != null) {
                    ft.add(container, f, null);
                }
                ft.commit();
            }
            if (titleResID != null) {
                getSupportActionBar().setTitle(titleResID);
            }
        }
    }

    /**
     * Wird von {@link FragmentFileChooser} gerufen, wenn ein Filename selektiert wurde. Zeigt
     * Dialog und fuehrt Aktion durch.
     *
     * @param filename
     *         fileName
     */
    @Override
    public void onFilenameSelected(String filename) {
        switch (getMainAction()) {
            case doRestore:
                String restoreTtitle = getString(R.string.dbTitleDatenbank);
                String restoreHinweistext = getString(R.string.dlgDatenbankRestore);
                AWLibDialogHinweis hinweis =
                        AWLibDialogHinweis.newInstance(false, restoreTtitle, restoreHinweistext);
                hinweis.show(getSupportFragmentManager(), null);
                EventDBRestore dbRestore = new EventDBRestore(AWLibActivityActions.this);
                dbRestore.restore(filename);
                hinweis.dismiss();
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(getPackageName());
                startActivity(intent);
                break;
        }
    }
}
