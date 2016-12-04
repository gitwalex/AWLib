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
                try {
                    File file = new EventDBSave().execute();
                    Context context = getApplicationContext();
                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    if (prefs.getBoolean(context.getString(R.string.pkExterneSicherung), false)) {
                    }
                } catch (ExecutionException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                }
                break;
            case doVaccum:
                ((AWApplication) getApplicationContext()).getApplicationConfig()
                        .createAndGetDBHelper().optimize();
                break;
        }
    }
}


