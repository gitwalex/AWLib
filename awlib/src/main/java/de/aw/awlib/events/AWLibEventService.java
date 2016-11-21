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
package de.aw.awlib.events;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.concurrent.ExecutionException;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * Bearbeitet Events innerhalb MonMa.
 */
public class AWLibEventService extends IntentService implements AWLibInterface {
    public AWLibEventService() {
        super("AWLibEventService");
    }

    /**
     * Handelt Intents.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        AWLibEvent event = extras.getParcelable(AWLIBEVENT);
        switch (event) {
            case DoDatabaseSave:
                try {
                    File file = new EventDBSave(this).execute();
                    Context context = getApplicationContext();
                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    if (prefs.getBoolean(context.getString(R.string.pkExterneSicherung), false)) {
                        String mURL =
                                prefs.getString(context.getString(R.string.pkServerURL), null);
                        String mUserID =
                                prefs.getString(context.getString(R.string.pkServerUID), null);
                        String mUserPassword =
                                prefs.getString(context.getString(R.string.pkServerPW), null);
                        int connectionTypeOrdinal =
                                prefs.getInt(context.getString(R.string.pkServerConnectionType), 0);
                        RemoteFileServer.ConnectionType mConnectionType =
                                RemoteFileServer.ConnectionType.values()[connectionTypeOrdinal];
                        String remoteDirectory =
                                prefs.getString(context.getString(R.string.pkServerRemoteDirectory),
                                        null);
                        new RemoteFileServer(mURL, mUserID, mUserPassword, mConnectionType).
                                transferFileToServer(file, remoteDirectory);
                    }
                } catch (ExecutionException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                } catch (RemoteFileServer.ConnectionFailsException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                }
                break;
            case doVaccum:
                AbstractDBHelper.doVacuum();
                break;
        }
    }
}


