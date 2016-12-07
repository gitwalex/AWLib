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
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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
import de.aw.awlib.database.AbstractDBHelper;
import de.aw.awlib.gv.AWRemoteFileServer;
import de.aw.awlib.recyclerview.AWArrayRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;
import de.aw.awlib.utils.AWRemoteFileServerHandler;
import de.aw.awlib.utils.AWRemoteFileServerHandler.ExecutionListener;

import static android.net.Uri.withAppendedPath;

/**
 * Dialog zur Abfrage von Zugangsdaten fuer externe Sicherung der DB.
 */
public class AWRemoteFileChooser extends AWArrayRecyclerViewFragment<FTPFile>
        implements ExecutionListener, AWFragment.OnAWFragmentDismissListener,
        AWFragment.OnAWFragmentCancelListener {
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
    private final ArrayList<String> mDirectoyList = new ArrayList<>();
    private AWFragmentActionBar.OnActionFinishListener mOnActionFinishListener;
    private View mProgressServerConnection;
    private AWRemoteFileServer mRemoteFileServer;
    private AWRemoteFileServerHandler mRemoteFileServerHandler;
    private View mServerErrorLayout;
    private TextView mServerErrorTexte;
    private Uri mUri = Uri.parse("/");

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static AWRemoteFileChooser newInstance(AWRemoteFileServer fileServer) {
        Bundle args = new Bundle();
        args.putParcelable(REMOTEFILESERVER, fileServer);
        AWRemoteFileChooser fragment = new AWRemoteFileChooser();
        fragment.setArguments(args);
        return fragment;
    }

    private AWRemoteFileServerHandler getExecuter() {
        if (mRemoteFileServerHandler == null) {
            mRemoteFileServerHandler = new AWRemoteFileServerHandler(mRemoteFileServer, this);
        }
        return mRemoteFileServerHandler;
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
                if (mDirectoyList.size() != 0) {
                    mDirectoyList.remove(mDirectoyList.size() - 1);
                }
                mUri = Uri.parse("/");
                for (int i = 0; i < mDirectoyList.size(); i++) {
                    String dir = mDirectoyList.get(i);
                    mUri = withAppendedPath(mUri, dir);
                }
            } else {
                mDirectoyList.add(filename);
                mUri = withAppendedPath(mUri, filename);
            }
            getExecuter().listFilesInDirectory(mUri.getEncodedPath(), mFileFilter);
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
            mUri = withAppendedPath(mUri, file.getName());
            mRemoteFileServer.setMainDirectory(mUri.getEncodedPath());
            if (mRemoteFileServer.isInserted()) {
                mRemoteFileServer.update(getActivity(), AbstractDBHelper.getInstance());
            } else {
                mRemoteFileServer.insert(getActivity(), AbstractDBHelper.getInstance());
            }
            mOnActionFinishListener.onActionFinishClicked(layout);
            return true;
        }
        return super.onArrayRecyclerItemLongClick(recyclerView, view, object);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            mOnActionFinishListener = (AWFragmentActionBar.OnActionFinishListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    "Activity muss OnActionFinishedListener implementieren");
        }
    }

    public boolean onBackpressed() {
        return mDirectoyList.size() == 0;
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
                    if (mDirectoyList.size() == 0) {
                        tv.setText(".");
                    } else {
                        tv.setText(file.getName());
                    }
                    consumed = true;
                } else if (resID == R.id.awlib_fileData) {
                    view.setVisibility(View.GONE);
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
                    view.setVisibility(View.VISIBLE);
                    tv = (TextView) view;
                    tv.setText(Formatter.formatFileSize(getContext(), file.getSize()));
                    consumed = true;
                }
        }
        return consumed;
    }

    @Override
    public void onCancel(int layoutID, DialogInterface dialog) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteFileServer = args.getParcelable(REMOTEFILESERVER);
    }

    @Override
    public void onDismiss(int layoutID, DialogInterface dialog) {
        if (!isCanceled) {
            if (!mRemoteFileServer.isValid()) {
                if (!mRemoteFileServer.isValid()) {
                    AWRemoteServerConnectionData f =
                            AWRemoteServerConnectionData.newInstance(mRemoteFileServer);
                    f.setOnDismissListener(this);
                    f.setOnCancelListener(this);
                    f.show(getFragmentManager(), null);
                }
            } else {
                getExecuter().listFilesInDirectory(mUri.getEncodedPath(), mFileFilter);
            }
        }
    }

    public void onEndFileServerTask(AWRemoteFileServerHandler.ConnectionFailsException result) {
        mProgressServerConnection.setVisibility(View.INVISIBLE);
        if (result == null) {
            setFileList(mRemoteFileServerHandler.getFiles());
            setTitle(mUri.getEncodedPath());
        } else {
            mServerErrorLayout.setVisibility(View.VISIBLE);
            mServerErrorTexte.setText(result.getStatusMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getExecuter().listFilesInDirectory(mUri.getEncodedPath(), mFileFilter);
    }

    public void onStartFileServerTask() {
        mProgressServerConnection.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mServerErrorLayout = view.findViewById(R.id.awlib_llServerError);
        mServerErrorTexte = (TextView) view.findViewById(R.id.awlib_tvServerError);
        mProgressServerConnection = view.findViewById(R.id.pbDlgServerConnection);
    }

    protected void setFileList(FTPFile[] files) {
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
        mAdapter.swapValues(mFiles);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }
}

