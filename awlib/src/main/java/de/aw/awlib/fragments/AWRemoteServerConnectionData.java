package de.aw.awlib.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import de.aw.awlib.R;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.gv.AWRemoteFileServer;
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
    private AWRemoteFileServer mRemoteFileServer;

    public static AWRemoteServerConnectionData newInstance(AWRemoteFileServer fileServer) {
        Bundle args = new Bundle();
        args.putParcelable(REMOTEFILESERVER, fileServer);
        AWRemoteServerConnectionData fragment = new AWRemoteServerConnectionData();
        fragment.setArguments(args);
        return fragment;
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
    protected boolean onOKButtonClicked() {
        mRemoteFileServer.setUserPassword(mPasswortEditText.getText().toString());
        if (mRemoteFileServer.isValid()) {
            AbstractDBHelper db = AbstractDBHelper.getInstance();
            if (mRemoteFileServer.isInserted()) {
                mRemoteFileServer.update(getActivity(), db);
            } else {
                mRemoteFileServer.insert(getActivity(), db);
            }
            View view = getView();
            if (view != null) {
                Snackbar.make(view, getString(R.string.awlib_datensatzSaved), Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        return true;
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
