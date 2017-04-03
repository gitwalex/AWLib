package de.aw.awlib.events;

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

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.concurrent.ExecutionException;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.application.AWApplication;

/**
 * Bearbeitet Events innerhalb MonMa.
 */
public class AWEventService extends IntentService implements AWInterface {
    public AWEventService() {
        super("AWEventService");
    }

    /**
     * Handelt Intents.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        AWEvent event = extras.getParcelable(AWLIBEVENT);
        switch (event) {
            case DoDatabaseSave:
                if (ContextCompat
                        .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    try {
                        new EventDBSave(this).execute();
                        Context context = getApplicationContext();
                        SharedPreferences prefs =
                                PreferenceManager.getDefaultSharedPreferences(context);
                        if (prefs.getBoolean(context.getString(R.string.pkExterneSicherung),
                                false)) {
                        }
                    } catch (ExecutionException e) {
                        //TODO Execption bearbeiten
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        //TODO Execption bearbeiten
                        e.printStackTrace();
                    }
                }
                break;
            case doVaccum:
                ((AWApplication) getApplicationContext()).getDBHelper().optimize();
                break;
        }
    }
}


