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

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

import de.aw.awlib.AWLIbApplication;
import de.aw.awlib.AWLibResultCodes;
import de.aw.awlib.AWLibUtils;
import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.database.AbstractDBHelper;

/**
 * Klasse fuer Sicheren/Restoren DB
 */
public class EventDBRestore extends AsyncTask<String, Void, Integer>
        implements AWLibResultCodes, AWLibInterface {
    private final Context mContext;
    private boolean fromServiceCalled = false;

    public EventDBRestore(Service service) {
        this(service.getApplicationContext());
        fromServiceCalled = true;
    }

    public EventDBRestore(Context context) {
        mContext = context;
    }

    @Override
    protected Integer doInBackground(String... params) {
        int result;
        String targetFileName;
        if (AWLIbApplication.getDebug()) {
            targetFileName = AWLIbApplication.getDatenbankFilename();
        } else {
            targetFileName =
                    mContext.getDatabasePath(AWLIbApplication.getDatenbankname()).getAbsolutePath();
        }
        AbstractDBHelper.getInstance().close();
        result = AWLibUtils.restoreZipArchivToFile(targetFileName, params[0]);
        AbstractDBHelper.getInstance();
        if (result == RESULT_OK) {
            AWLIbApplication.onRestoreDB();
        }
        return result;
    }

    public void restore(String filename) {
        try {
            execute(filename).get();
        } catch (InterruptedException e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        } catch (ExecutionException e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        }
    }
}
