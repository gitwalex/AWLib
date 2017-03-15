/**
 *
 */
package de.aw.awlib.activities;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
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
import de.aw.awlib.fragments.AWFragment;
import de.aw.awlib.fragments.AWFragmentActionBar;
import de.aw.awlib.fragments.AWFragmentRemoteFileServer;
import de.aw.awlib.fragments.AWRemoteFileChooser;
import de.aw.awlib.fragments.AWRemoteServerConnectionData;
import de.aw.awlib.fragments.AWShowPicture;
import de.aw.awlib.gv.AWApplicationGeschaeftsObjekt;
import de.aw.awlib.gv.AWRemoteFileServer;
import de.aw.awlib.recyclerview.AWBaseRecyclerViewListener;
import de.aw.awlib.recyclerview.AWItemListRecyclerViewListener;

/**
 * Activity fuer verschiedene Aktionen
 */
public class AWActivityActions extends AWMainActivity
        implements AWItemListRecyclerViewListener, AWFragmentActionBar.OnActionFinishListener,
        AWFragment.OnAWFragmentDismissListener, AWFragment.OnAWFragmentCancelListener,
        AWBaseRecyclerViewListener {
    private AWEvent event;
    private AWRemoteFileServer mRemoteServer;

    @Override
    public void onActionFinishClicked(int layoutID) {
        finish();
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
    public void onCancel(@LayoutRes int layoutID, DialogInterface dialog) {
        if (layoutID == R.layout.awlib_dialog_remote_fileserver) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onClick(View v) {
        switch (event) {
            case showRemoteFileServer:
                mRemoteServer = new AWRemoteFileServer(this);
                AWFragment f = AWRemoteServerConnectionData.newInstance(mRemoteServer);
                f.setOnDismissListener(this);
                f.setOnCancelListener(this);
                f.show(getSupportFragmentManager(), event.name());
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        event = args.getParcelable(AWLIBEVENT);
        if (event == null) {
            finish();
        } else {
            Integer subTitleResID = null;
            if (savedInstanceState == null) {
                Fragment f = null;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                switch (event) {
                    case showBackupFiles:
                        // Datenbank wiederherstellen
                        String backupFolderName = ((AWApplication) getApplicationContext())
                                .getApplicationBackupPath();
                        f = AWFileChooser.newInstance(backupFolderName);
                        subTitleResID = R.string.fileChooserTitleDoRestore;
                        break;
                    case configRemoteFileServer:
                        AWRemoteFileServer mRemoteFileServer = new AWRemoteFileServer(this);
                        f = AWRemoteFileChooser.newInstance(mRemoteFileServer);
                        setSubTitle(mRemoteFileServer.getURL());
                        break;
                    case showRemoteFileServer:
                        f = new AWFragmentRemoteFileServer();
                        getDefaultFAB().setVisibility(View.VISIBLE);
                        getDefaultFAB().setOnClickListener(this);
                        break;
                    case ShowPicture:
                        String filename = args.getString(FILENAME);
                        if (filename != null) {
                            f = AWShowPicture.newInstance(filename);
                        }
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
            if (subTitleResID != null) {
                setSubTitle(subTitleResID);
            }
        }
    }

    @Override
    public void onDismiss(@LayoutRes int layoutID, DialogInterface dialog) {
        AWFragment f = null;
        if (layoutID == R.layout.awlib_dialog_remote_fileserver) {
            f = AWRemoteFileChooser.newInstance(mRemoteServer);
        }
        if (f != null) {
            getSupportFragmentManager().beginTransaction().replace(container, f, event.name())
                                       .addToBackStack(null).commit();
        }
    }

    @Override
    public void onItemListRecyclerItemClick(RecyclerView parent, View view, Object object) {
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
    public boolean onItemListRecyclerItemLongClick(RecyclerView recyclerView, View view,
                                                   Object object) {
        return false;
    }

    @Override
    public void onRecyclerItemClick(RecyclerView parent, View view, int position, long id,
                                    int viewHolderLayoutID) {
        AWFragment f = null;
        if (viewHolderLayoutID == R.layout.awlib_remote_fileserver) {
            try {
                mRemoteServer = new AWRemoteFileServer(this, id);
                f = AWRemoteFileChooser.newInstance(mRemoteServer);
            } catch (AWApplicationGeschaeftsObjekt.LineNotFoundException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
            }
        }
        if (f != null) {
            getSupportFragmentManager().beginTransaction().replace(container, f, event.name())
                                       .addToBackStack(null).commit();
        }
    }

    @Override
    public boolean onRecyclerItemLongClick(RecyclerView recyclerView, View view, int position,
                                           long id, int viewHolderLayoutID) {
        return false;
    }
}