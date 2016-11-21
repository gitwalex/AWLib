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
package de.aw.awlib.preferences;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.fragments.AWLibFileChooser;
import de.aw.awlib.fragments.AWLibFragment;
import de.aw.awlib.gv.RemoteFileServer;

/**
 * Dialog zur Abfrage von Zugangsdaten fuer externe Sicherung der DB.
 */
public class DialogFTP extends AWLibFileChooser {
    private final int layout = R.layout.awlib_dialog_db_ftp;
    private Button mOKBtn;
    private String mPasswort;
    private EditText mPasswortEditText;
    private View mProgressBar;
    private String mServer;
    private EditText mServerEditText;
    private View mServerMessageLayout;
    private TextView mServerMessageTextView;
    private String mServerName;
    private Button mTestBtn;
    private String mUserName;
    private EditText mUserNameEditText;
    private String mUsername;

    public static AWLibFragment newInstance() {
        return new DialogFTP();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServer = prefs.getString(getString(R.string.pkServerURL), null);
        mUsername = prefs.getString(getString(R.string.pkServerUID), null);
    }

    /**
     * Erstellt den Dialog mit CustomView und OK/NEUTRAL-Button.
     * <p>
     * Sobald der Dialog gezeigt wird, werden die OnClickListener der Buttons ersetzt. Dies ist
     * notwendig, weil die Buttons ausgeblendet werden muessen, ein Zugriff auf die Buttons ist aber
     * erst nach show() moeglich.
     * <p>
     * Bei Wahlt NEUTRAL wird mit den eingegebenen Daten eine Testverbindung zum Server aufgebaut.
     * Nur wenn diese erfolgreich war, wir der OK-Button freigeschaltet - erst dann kann gespeichert
     * werden.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View mView = LayoutInflater.from(getActivity()).inflate(layout, null);
        mServerEditText = (EditText) mView.findViewById(R.id.awlib_etDBServerName);
        mServerEditText.setText(mServer);
        mUserNameEditText = (EditText) mView.findViewById(R.id.awlib_etDBUserName);
        mUserNameEditText.setText(mUsername);
        mPasswortEditText = (EditText) mView.findViewById(R.id.awlib_etDBUserPW);
        mServerMessageLayout = mView.findViewById(R.id.awlib_llServerError);
        mServerMessageTextView = (TextView) mView.findViewById(R.id.awlib_tvServerError);
        mProgressBar = mView.findViewById(R.id.awlib_pbDlg_db_ftp);
        builder.setPositiveButton(R.string.awlib_btnAccept, this);
        builder.setNeutralButton(R.string.dbServerTest, this);
        builder.setTitle(R.string.dbServerZugangsdaten);
        builder.setView(mView);
        AlertDialog dlg = builder.create();
        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dlg) {
                mTestBtn = ((AlertDialog) dlg).getButton(DialogInterface.BUTTON_NEUTRAL);
                mTestBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mServerName = mServerEditText.getText().toString();
                        mUserName = mUserNameEditText.getText().toString();
                        mPasswort = mPasswortEditText.getText().toString();
                        new AsyncTask<Void, Void, Boolean>() {
                            @Override
                            protected Boolean doInBackground(Void... params) {
                                try {
                                    new RemoteFileServer(mServerName, mUserName, mPasswort,
                                            RemoteFileServer.ConnectionType.SSL);
                                    return true;
                                } catch (final RemoteFileServer.ConnectionFailsException e) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mServerMessageLayout.setVisibility(View.VISIBLE);
                                            mServerMessageTextView.setText(null);
                                            for (String stat : e.getStatus()) {
                                                mServerMessageTextView.append(stat);
                                            }
                                        }
                                    });
                                    e.printStackTrace();
                                    return false;
                                }
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                mProgressBar.setVisibility(View.INVISIBLE);
                                mOKBtn.setEnabled(aBoolean);
                                mTestBtn.setEnabled(true);
                                if (aBoolean) {
                                    mServerMessageLayout.setVisibility(View.VISIBLE);
                                    mServerMessageTextView
                                            .setText(R.string.dbServerMessageErfolgreich);
                                }
                            }

                            @Override
                            protected void onPreExecute() {
                                mProgressBar.setVisibility(View.VISIBLE);
                                mServerMessageLayout.setVisibility(View.GONE);
                                mTestBtn.setEnabled(false);
                            }
                        }.execute();
                    }
                });
                mOKBtn = ((AlertDialog) dlg).getButton(DialogInterface.BUTTON_POSITIVE);
                mOKBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
                mOKBtn.setEnabled(false);
            }
        });
        return dlg;
    }

    /**
     * In den SharedPreferences wird das Ergebnis abgelegt.
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.pkExterneSicherung), !isCanceled).apply();
        if (!isCanceled) {
            editor.putString(getString(R.string.pkServerURL), mServerName).apply();
            editor.putString(getString(R.string.pkServerUID), mUserName).apply();
            editor.putString(getString(R.string.pkServerPW), mPasswort).apply();
        }
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }
}

