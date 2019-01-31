/*
 * MonMa: Eine freie Android-Application fuer die Verwaltung privater Finanzen
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

package de.aw.awlib.fragments;

import android.os.Bundle;

import de.aw.awlib.R;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.database.TableColumns;
import de.aw.awlib.recyclerview.AWCursorRecyclerViewFragment;

/**
 * Fragment zur Anzeige der bisher konfigurierten RemoteFileServer
 */
public class AWFragmentRemoteFileServer extends AWCursorRecyclerViewFragment
        implements TableColumns {
    private static final AbstractDBHelper.AWDBDefinition tbd =
            AbstractDBHelper.AWDBDefinition.RemoteServer;
    private static final String[] projection =
            new String[]{column_serverurl, column_userID, column_connectionType,
                    column_maindirectory, _id};
    private static final int viewHolderLayout = R.layout.awlib_remote_fileserver;
    private static final int[] viewResIDs =
            new int[]{R.id.tvRemoteFileServerName, R.id.tvUserName, R.id.tvConnectionType,
                    R.id.tvBackupVerzeichnis};

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putParcelable(DBDEFINITION, tbd);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putStringArray(PROJECTION, projection);
    }
}
