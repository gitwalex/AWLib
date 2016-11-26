package de.aw.awlib.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import de.aw.awlib.R;
import de.aw.awlib.gv.RemoteFileServer;
import de.aw.awlib.utils.AWRemoteFileServerHandler;

import static de.aw.awlib.utils.AWRemoteFileServerHandler.ConnectionType.SSL;

/**
 * Dialog fuer Abfrage von Zugangsdaten zu einem FileServer.
 */
public class AWRemoteServerConnectionData extends AWFragment {
    private static final int layout = R.layout.awlib_dialog_remote_fileserver;
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_etDBServerName, R.id.awlib_etDBUserName};
    private static final int[] fromResIDs =
            new int[]{R.string.column_serverurl, R.string.column_userID};
    private EditText mPasswortEditText;
    private RemoteFileServer mRemoteFileServer;

    public static AWRemoteServerConnectionData newInstance(RemoteFileServer fileServer) {
        Bundle args = new Bundle();
        args.putParcelable(REMOTEFILESERVER, fileServer);
        args.putParcelable(AWLIBACTION, MainAction.SHOW);
        AWRemoteServerConnectionData fragment = new AWRemoteServerConnectionData();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mRemoteFileServer.setUserPassword(mPasswortEditText.getText().toString());
        if (mRemoteFileServer.isValid()) {
            super.onClick(dialog, which);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteFileServer = args.getParcelable(REMOTEFILESERVER);
        awlib_gv = mRemoteFileServer;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dlg = super.onCreateDialog(savedInstanceState);
        dlg.setTitle(R.string.titleFileServerKonfigurieren);
        return dlg;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPasswortEditText = (EditText) view.findViewById(R.id.awlib_etDBUserPW);
        mPasswortEditText.setText(mRemoteFileServer.getUserPassword());
        CheckBox mConnectionTypeCheckBox = (CheckBox) view.findViewById(R.id.cbConnectionType);
        mConnectionTypeCheckBox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (buttonView.isChecked()) {
                            mRemoteFileServer.put(R.string.column_connectionType, SSL.name());
                        } else {
                            mRemoteFileServer.put(R.string.column_connectionType,
                                    AWRemoteFileServerHandler.ConnectionType.NONSSL.name());
                        }
                    }
                });
        mConnectionTypeCheckBox.setChecked(mRemoteFileServer.getConnectionType() == SSL);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putIntArray(FROMRESIDS, fromResIDs);
    }
}
