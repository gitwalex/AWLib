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
package de.aw.awlib.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWLibMainActivity;
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.database_private.AWLibDBHelper;
import de.aw.awlib.gv.RemoteFileServer;
import de.aw.awlib.recyclerview.AWLibArrayRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * Dialog zur Abfrage von Zugangsdaten fuer externe Sicherung der DB.
 */
public class AWLibRemoteFileChooser extends AWLibArrayRecyclerViewFragment<FTPFile> {
    protected static final String DIRECTORYNAME = "DIRECTORYNAME";
    private static final int layout = R.layout.awlib_remote_filechooser;
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_fileName, R.id.awlib_fileData, R.id.folderImage};
    private static final int viewHolderLayout = R.layout.awlib_filechooser_items;
    private static final FTPFileFilter mFileFilter = new FTPFileFilter() {
        @Override
        public boolean accept(FTPFile file) {
            String name = file.getName();
            // Nur durch den User beschreibbare Dateien,
            if (!file.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION)) {
                return false;
            }
            if (name.startsWith("..")) {
                return true;
            }
            // keine versteckten
            return !name.startsWith(".");
        }
    };
    private static final int BACKTOPARENT = 1;
    protected String mDirectoy;
    private AWLibFragmentActionBar.OnActionFinishListener mOnActionFinishListener;
    private View mProgressServerConnection;
    private RemoteFileServer mRemoteFileServer;
    private View mServerErrorLayout;
    private TextView mServerErrorTexte;
    private TextView mServerURLTextView;
    private Uri mUri = Uri.parse("/");
    private TextView mUserIDTextView;

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static AWLibRemoteFileChooser newInstance(RemoteFileServer fileServer) {
        Bundle args = new Bundle();
        args.putParcelable(REMOTEFILESERVER, fileServer);
        AWLibRemoteFileChooser fragment = new AWLibRemoteFileChooser();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public AbstractDBHelper getDBHelper() {
        return AWLibDBHelper.getInstance();
    }

    @Override
    public int getItemViewType(int position, FTPFile object) {
        if (position == 0 && object.getName().equals("..")) {
            return BACKTOPARENT;
        }
        return super.getItemViewType(position, object);
    }

    /**
     * Wird ein Directory ausgwaehlt, wird in dieses Directory gewechselt.
     */
    @Override
    public void onArrayRecyclerItemClick(RecyclerView recyclerView, View view, Object object) {
        FTPFile file = (FTPFile) object;
        if (file.isDirectory()) {
            String filename = file.getName();
            if (filename.equals("..")) {
                String path = mUri.getPath();
                String lastPath = mUri.getLastPathSegment();
                path = path.replace("/" + lastPath, "");
                if (TextUtils.isEmpty(path)) {
                    path = "/";
                }
                mUri = Uri.parse(path);
            } else {
                mUri = Uri.withAppendedPath(mUri, filename);
            }
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
        if (file.isDirectory()) {
            return true;
        }
        return super.onArrayRecyclerItemLongClick(recyclerView, view, object);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mOnActionFinishListener = (AWLibFragmentActionBar.OnActionFinishListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "Activity muss OnActionFinishedListener implementieren");
        }
    }

    @Override
    protected boolean onBindView(AWLibViewHolder holder, View view, int resID, FTPFile file) {
        TextView tv;
        boolean consumed = false;
        switch (holder.getItemViewType()) {
            case BACKTOPARENT:
                consumed = true;
                if (resID == R.id.folderImage) {
                    ImageView img = (ImageView) view;
                    img.setImageResource(R.drawable.ic_open_folder);
                    consumed = true;
                } else if (resID == R.id.awlib_fileName) {
                    tv = (TextView) view;
                    tv.setText(file.getName());
                    consumed = true;
                } else if (resID == R.id.awlib_fileData) {
                    tv = (TextView) view;
                    tv.setText(mUri.getPath());
                    consumed = true;
                }
                break;
            default:
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
        }
        return consumed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteFileServer = args.getParcelable(REMOTEFILESERVER);
    }

    /**
     * In den SharedPreferences wird das Ergebnis abgelegt.
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!isCanceled) {
            if (!mRemoteFileServer.isValid()) {
                if (!mRemoteFileServer.isValid()) {
                    AWLibRemoteServerConnectionData f =
                            AWLibRemoteServerConnectionData.newInstance(mRemoteFileServer);
                    f.setOnDismissListener(this);
                    f.setOnCancelListener(this);
                    f.show(getFragmentManager(), null);
                }
            } else {
                testConnection();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mRemoteFileServer.isValid()) {
            AWLibRemoteServerConnectionData f =
                    AWLibRemoteServerConnectionData.newInstance(mRemoteFileServer);
            f.setOnDismissListener(this);
            f.show(getFragmentManager(), null);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mServerErrorLayout = view.findViewById(R.id.awlib_llServerError);
        mServerErrorTexte = (TextView) view.findViewById(R.id.awlib_tvServerError);
        mServerURLTextView = (TextView) view.findViewById(R.id.awlib_tvDBServerName);
        mUserIDTextView = (TextView) view.findViewById(R.id.awlib_tvDBUserName);
        mProgressServerConnection = view.findViewById(R.id.pbDlgServerConnection);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }

    private void testConnection() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return mRemoteFileServer.testConnection();
                } catch (final RemoteFileServer.ConnectionFailsException e) {
                    //TODO Execption bearbeiten
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder s = new StringBuilder();
                            for (String val : e.getStatus()) {
                                s.append(val).append(linefeed);
                            }
                            mServerErrorTexte.setText(s.toString());
                        }
                    });
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mProgressServerConnection.setVisibility(View.GONE);
                if (!result) {
                    mServerErrorLayout.setVisibility(View.VISIBLE);
                } else {
                    new CreateFileList(mUri);
                }
            }

            @Override
            protected void onPreExecute() {
                mServerURLTextView.setText(mRemoteFileServer.getURL());
                mUserIDTextView.setText(mRemoteFileServer.getUserID());
                mProgressServerConnection.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    /**
     * Erstellt eine Liste der Files innerhalb eines Directories. Ist das File ungleich dem in
     * {@link AWLibFileChooser#newInstance(String)} angegebenen Directory, wird am Anfang der Liste
     * der Parent des uebergebenen Files eingefuegt. Damit kann eine Navigation erfolgen.
     * <p>
     * Die erstellte Liste wird direkt in den Adapter einestellt.
     * <p>
     * Ausserdem wird im Subtitle der Toolbar der Name des akuellten Verzeichnisses eingeblendet.
     */
    private class CreateFileList extends AsyncTask<Uri, Void, List<FTPFile>> {
        private final Uri newDirectory;

        /**
         * Erstellt eine Liste der Files innerhalb eines Directories. Ist das File ungleich dem in
         * {@link AWLibFileChooser#newInstance(String)} angegebenen Directory, wird am Anfang der
         * Liste der Parent des uebergebenen Files eingefuegt. Damit kann eine Navigation erfolgen.
         * <p>
         * Die erstellte Liste wird direkt in den Adapter einestellt.
         * <p>
         * Ausserdem wird im Subtitle der Toolbar der Name des akuellten Verzeichnisses
         * eingeblendet.
         *
         * @param directory
         *         directory, zu dem die Liste erstellt werden soll
         */
        public CreateFileList(Uri directory) {
            newDirectory = directory;
            execute(directory);
        }

        @Override
        protected List<FTPFile> doInBackground(Uri... params) {
            Uri directory = params[0];
            try {
                FTPFile[] files;
                files = mRemoteFileServer.listFiles(directory.getEncodedPath(), mFileFilter);
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
            mProgressServerConnection.setVisibility(View.GONE);
            ((AWLibMainActivity) getActivity()).getSupportActionBar()
                    .setSubtitle(newDirectory.getEncodedPath());
            mAdapter.swapValues(value);
        }

        @Override
        protected void onPreExecute() {
            mProgressServerConnection.setVisibility(View.VISIBLE);
        }
    }
}

