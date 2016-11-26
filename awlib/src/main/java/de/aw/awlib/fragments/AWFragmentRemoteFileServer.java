package de.aw.awlib.fragments;

import android.os.Bundle;

import de.aw.awlib.R;
import de.aw.awlib.database_private.AWDBDefinition;
import de.aw.awlib.recyclerview.AWCursorRecyclerViewFragment;

/**
 * Fragment zur Anzeige der bisher konfigurierten RemoteFileServer
 */
public class AWFragmentRemoteFileServer extends AWCursorRecyclerViewFragment {
    private static final AWDBDefinition tbd = AWDBDefinition.RemoteServer;
    private static final int[] fromResIDs =
            new int[]{R.string.column_serverurl, R.string.column_userID,
                    R.string.column_connectionType, R.string.column_maindirectory};
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
        args.putIntArray(FROMRESIDS, fromResIDs);
    }
}
