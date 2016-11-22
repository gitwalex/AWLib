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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibMainActivity;
import de.aw.awlib.fragments.AWLibFileChooser;
import de.aw.awlib.gv.RemoteFileServer;
import de.aw.awlib.recyclerview.AWLibArrayRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Dialog zur Abfrage von Zugangsdaten fuer externe Sicherung der DB.
 */
public class DialogFTP extends AWLibArrayRecyclerViewFragment<FTPFile> {
    protected static final String DIRECTORYNAME = "DIRECTORYNAME";
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_fileName, R.id.awlib_fileData, R.id.folderImage};
    private static final int viewHolderLayout = R.layout.awlib_filechooser_items;
    private final int layout = R.layout.awlib_dialog_db_ftp;
    protected boolean hasParent;
    protected String mDirectoy;
    private FTPFile[] files;
    private FTPFile mFile;
    private FilenameFilter mFilenameFilter;
    private Button mOKBtn;
    private String mPasswort;
    private EditText mPasswortEditText;
    private View mProgressBar;
    private RemoteFileServer mRemoteFileServer;
    private String mServer;
    private EditText mServerEditText;
    private View mServerMessageLayout;
    private TextView mServerMessageTextView;
    private String mServerName;
    private Button mTestBtn;
    private Uri mUri;
    private String mUserName;
    private EditText mUserNameEditText;
    private String mUsername;

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static DialogFTP newInstance() {
        Bundle args = new Bundle();
        args.putString(DIRECTORYNAME, "/");
        DialogFTP fragment = new DialogFTP();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Erstellt eine Liste der Files innerhalb eines Directories. Ist das File ungleich dem in
     * {@link AWLibFileChooser#newInstance(String)} angegebenen Directory, wird am Anfang der Liste
     * der Parent des uebergebenen Files eingefuegt. Damit kann eine Navigation erfolgen.
     * <p>
     * Die erstellte Liste wird direkt in den Adapter einestellt.
     * <p>
     * Ausserdem wird im Subtitle der Toolbar der Name des akuellten Verzeichnisses eingeblendet.
     *
     * @param directory
     *         directory, zu dem die Liste erstellt werden soll
     */
    private void createFileList(String directory) throws RemoteFileServer.ConnectionFailsException {
    }

    /**
     * Wird ein Directory ausgwaehlt, wird in dieses Directory gewechselt.
     */
    @Override
    public void onArrayRecyclerItemClick(RecyclerView recyclerView, View view, Object object) {
        FTPFile file = (FTPFile) object;
        if (file.isDirectory()) {
            String filename = file.getName();
            mUri = Uri.withAppendedPath(mUri, filename);
            new CreateFileList(mUri);
        } else {
            super.onArrayRecyclerItemClick(recyclerView, view, object);
        }
    }

    /**
     * Wird ein Dateieintrag lang ausgewaehlt, wird ein Loeschen-Dialog angeboten.
     */
    @Override
    public boolean onArrayRecyclerItemLongClick(RecyclerView recyclerView, View view,
                                                Object object) {
        final FTPFile file = (FTPFile) object;
        if (!file.isDirectory()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.awlib_btnAccept,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.setTitle(R.string.awlib_deleteFile);
            Dialog dlg = builder.create();
            dlg.show();
            return true;
        }
        return super.onArrayRecyclerItemLongClick(recyclerView, view, object);
    }

    @Override
    protected boolean onBindView(AWLibViewHolder holder, View view, int resID, FTPFile file) {
        TextView tv;
        boolean consumed = false;
        if (resID == R.id.folderImage) {
            ImageView img = (ImageView) view;
            if (file.isDirectory()) {
                img.setImageResource(R.drawable.ic_closed_folder);
            } else {
                img.setImageResource(R.drawable.ic_file_generic);
            }
            consumed = true;
        } else if (resID == R.id.awlib_fileName) {
            tv = (TextView) view;
            tv.setText(file.getName());
            consumed = true;
        } else if (resID == R.id.awlib_fileData) {
            tv = (TextView) view;
            tv.setText(Formatter.formatFileSize(getContext(), file.getSize()));
            consumed = true;
        }
        return consumed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDirectoy = args.getString(DIRECTORYNAME);
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
    public void onViewCreated(View mView, Bundle savedInstanceState) {
        super.onViewCreated(mView, savedInstanceState);
        mServerEditText = (EditText) mView.findViewById(R.id.awlib_etDBServerName);
        mServerEditText.setText(mServer);
        mUserNameEditText = (EditText) mView.findViewById(R.id.awlib_etDBUserName);
        mUserNameEditText.setText(mUsername);
        mPasswortEditText = (EditText) mView.findViewById(R.id.awlib_etDBUserPW);
        mServerMessageLayout = mView.findViewById(R.id.awlib_llServerError);
        mServerMessageTextView = (TextView) mView.findViewById(R.id.awlib_tvServerError);
        mProgressBar = mView.findViewById(R.id.awlib_pbDlg_db_ftp);
        mTestBtn = (Button) mView.findViewById(R.id.btnTestConnection);
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
                            mRemoteFileServer =
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
                    protected void onPostExecute(Boolean result) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mOKBtn.setEnabled(result);
                        mTestBtn.setEnabled(true);
                        mTestBtn.requestFocus();
                        if (result) {
                            mUri = Uri.parse(mDirectoy);
                            new CreateFileList(mUri);
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
        mOKBtn = (Button) mView.findViewById(R.id.btnAccept);
        mOKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mOKBtn.setEnabled(false);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }

    private class CreateFileList extends AsyncTask<Uri, Void, List<FTPFile>> {
        private final Uri newDirectory;

        public CreateFileList(Uri directory) {
            newDirectory = directory;
            execute(directory);
        }

        @Override
        protected List<FTPFile> doInBackground(Uri... params) {
            Uri directory = params[0];
            FTPFile[] files;
            try {
                files = mRemoteFileServer.listFiles(directory.getEncodedPath());
                List<FTPFile> mFiles = Arrays.asList(files);
                Collections.sort(mFiles, new Comparator<FTPFile>() {
                    @Override
                    public int compare(FTPFile lhs, FTPFile rhs) {
                        if (lhs.isDirectory() && !rhs.isDirectory()) {
                            // Directory before File
                            return -1;
                        } else if (!lhs.isDirectory() && rhs.isDirectory()) {
                            // File after directory
                            return 1;
                        } else {
                            // Otherwise in Alphabetic order...
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    }
                });
                return new ArrayList<>(mFiles);
            } catch (RemoteFileServer.ConnectionFailsException e) {
                //TODO Execption bearbeiten
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<FTPFile> value) {
            ((AWLibMainActivity) getActivity()).getSupportActionBar()
                    .setSubtitle(newDirectory.getEncodedPath());
            mAdapter.swapValues(value);
        }
    }
}

