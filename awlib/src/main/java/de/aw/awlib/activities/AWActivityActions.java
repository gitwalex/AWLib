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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.io.File;

import de.aw.awlib.R;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.events.AWEvent;
import de.aw.awlib.events.EventDBRestore;
import de.aw.awlib.fragments.AWFileChooser;
import de.aw.awlib.fragments.AWFragmentActionBar;
import de.aw.awlib.fragments.AWRemoteFileChooser;
import de.aw.awlib.gv.RemoteFileServer;
import de.aw.awlib.recyclerview.AWOnArrayRecyclerViewListener;

/**
 * Activity fuer verschiedene Aktionen
 */
public class AWActivityActions extends AWMainActivity
        implements AWOnArrayRecyclerViewListener, AWFragmentActionBar.OnActionFinishListener {
    private AWEvent event;

    @Override
    public void onActionFinishClicked(int layoutID, int itemResID) {
        super.onActionFinishClicked(layoutID, itemResID);
        finish();
    }

    @Override
    public void onArrayRecyclerItemClick(RecyclerView parent, View view, Object object) {
        switch (event) {
            case showBackupFiles:
                final File file = (File) object;
                if (!file.isDirectory()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton(R.string.awlib_btnAccept,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    EventDBRestore dbRestore =
                                            new EventDBRestore(AWActivityActions.this);
                                    dbRestore.restore(file);
                                }
                            });
                    builder.setTitle(R.string.dbTitleDatenbank);
                    builder.setMessage(R.string.dlgDatenbankRestore);
                    Dialog dlg = builder.create();
                    dlg.show();
                    break;
                }
        }
    }

    @Override
    public boolean onArrayRecyclerItemLongClick(RecyclerView recyclerView, View view,
                                                Object object) {
        return false;
    }

    /**
     * Wenn BackButton gewaehlt wird, pruefen, ob ein {@link AWFileChooser} gezeigt wird. Ist dies
     * der Fall, wird die Methode {@link AWFileChooser#onBackPressed()} gerufen.
     * <p>
     * Liefert das Fragment true zuruck, wird keine weitere Aktion durchgefuehrt. Ansonsten wird
     * super.onBackpressed gerufen..
     */
    @Override
    public void onBackPressed() {
        Fragment mFragment =
                getSupportFragmentManager().findFragmentByTag(AWEvent.showBackupFiles.name());
        if (mFragment != null) {
            if (((AWFileChooser) mFragment).onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        event = args.getParcelable(AWLIBEVENT);
        if (event == null) {
            finish();
        } else {
            Integer titleResID = null;
            if (savedInstanceState == null) {
                Fragment f;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                switch (event) {
                    case showBackupFiles:
                        // Datenbank wiederherstellen
                        String backupFolderName = AWApplication.getApplicationBackupPath();
                        f = AWFileChooser.newInstance(backupFolderName);
                        titleResID = R.string.fileChooserTitleDoRestore;
                        break;
                    case configRemoteFileServer:
                        RemoteFileServer mRemoteFileServer = new RemoteFileServer();
                        args.putParcelable(REMOTEFILESERVER, mRemoteFileServer);
                        f = AWRemoteFileChooser.newInstance(mRemoteFileServer);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Kein Fragment fuer " + getMainAction().name() + " vorgesehen");
                }
                if (f != null) {
                    ft.add(container, f, event.name());
                }
                ft.commit();
            }
            if (titleResID != null) {
                getSupportActionBar().setTitle(titleResID);
            }
        }
    }
}