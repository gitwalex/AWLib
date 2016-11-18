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
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;

import static de.aw.awlib.activities.AWLibInterface.AWLIBEVENT;

/**
 * Bearbeitet Events innerhalb MonMa. Moegliche Events: {@link AWLibEvent}
 */
public class AWLibEventService extends IntentService {
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
                EventDBSave eventSaveDB = new EventDBSave(this);
                String filename = eventSaveDB.getFileName();
                try {
                    new EventTransferDB(getApplicationContext(), EventTransferDB.ConnectionArt.SSL,
                            filename);
                } catch (IOException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                } catch (EventTransferDB.ConnectionFailsException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                }
                break;
        }
    }
}

