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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.io.File;

import de.aw.awlib.AWLibResultCodes;
import de.aw.awlib.AWLibUtils;
import de.aw.awlib.activities.AWLibInterface;
import de.aw.awlib.application.AWLIbApplication;

/**
 * Klasse fuer Sicheren/Restoren DB
 */
public class EventDBRestore implements AWLibResultCodes, AWLibInterface {
    private final Context mContext;

    public EventDBRestore(Context context) {
        mContext = context;
    }

    public void restore(File file) {
        new DoDatabaseRestore().execute(file);
    }

    private class DoDatabaseRestore extends AsyncTask<File, Void, Integer> {
        @Override
        protected Integer doInBackground(File... params) {
            int result;
            String targetFileName;
            if (AWLIbApplication.getDebugFlag()) {
                targetFileName = AWLIbApplication.getApplicationDatabaseFilename();
            } else {
                targetFileName = mContext.getDatabasePath(AWLIbApplication.getDatenbankname())
                        .getAbsolutePath();
            }
            AWLIbApplication.getDBHelper().close();
            result = AWLibUtils.restoreZipArchivToFile(targetFileName, params[0]);
            AWLIbApplication.getDBHelper();
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == RESULT_OK) {
                AWLIbApplication.onRestoreDB();
                PackageManager pm = mContext.getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(mContext.getPackageName());
                mContext.startActivity(intent);
            }
        }
    }
}
