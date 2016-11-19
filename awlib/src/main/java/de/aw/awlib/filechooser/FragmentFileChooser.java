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
package de.aw.awlib.filechooser;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.fragments.AWLibDialogHinweis;
import de.aw.awlib.fragments.AWLibFragment;

/**
 * FileChooser: Auswahl von Dateien auf dem ExternalStrorage
 */
public class FragmentFileChooser extends AWLibFragment
        implements AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener,
        AdapterView.OnItemClickListener {
    private static final int layout = R.layout.awlib_default_list_view;
    private static String FILEEXTENSION = "FILEEXTENSION", ONLYFOLDERFILES = "ONLYFOLDERFILES",
            STARTDIRECTORY = "STARTDIRECTORY";
    private FileChooserAdapter adapter;
    private File currentDir;
    private String externalStorageDir;
    private FileChooserListener fileChooserListener;
    private String mExtension;
    private ListView mListView;
    private boolean onlyFiles;

    /**
     * @param extension
     *         Zeigt nur files mit dieser Extension
     * @param onlyFolderFiles
     *         true: es werden nur Files gezegt, kein Directory
     *
     * @return Fragment
     */
    public static FragmentFileChooser newInstance(String extension, boolean onlyFolderFiles) {
        FragmentFileChooser f = new FragmentFileChooser();
        Bundle args = new Bundle();
        args.putBoolean(ONLYFOLDERFILES, onlyFolderFiles);
        args.putString(FILEEXTENSION, extension);
        f.setArguments(args);
        return f;
    }

    /**
     * Auswahl einer Datei aus einem bestimmtem Verzeichnis - und nur diesem. Kein Wechsel ins
     * uebergordnete Verzeichnis, keine Unterverzeichisse
     *
     * @param startDirectory
     *         Auswahl einer Datei aus einem bestimmtem Verzeichnis - und nur diesem. Kein Wechsel
     *         ins uebergordnete Verzeichnis, keine Unterverzeichisse
     *
     * @return Fragment
     */
    public static FragmentFileChooser newInstance(String startDirectory) {
        Bundle args = new Bundle();
        args.putString(STARTDIRECTORY, startDirectory);
        args.putBoolean(ONLYFOLDERFILES, true);
        FragmentFileChooser fragment = new FragmentFileChooser();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Fuellt Adapter mit Filenamen
     *
     * @param f
     *         File
     */
    protected List<FileChooserOptions> createContent(File f) {
        File[] dirs = f.listFiles();
        List<FileChooserOptions> dir = new ArrayList<>();
        List<FileChooserOptions> fls = new ArrayList<>();
        try {
            for (File ff : dirs) {
                String filename = ff.getName().toLowerCase();
                if (!filename.startsWith(".")) {
                    if (!onlyFiles && ff.isDirectory()) {
                        dir.add(new FileChooserOptions(ff.getName(), "Folder", ff.getAbsolutePath(),
                                false));
                    } else {
                        if (mExtension == null) {
                            fls.add(new FileChooserOptions(ff.getName(),
                                    "File Size: " + ff.length(), ff.getAbsolutePath(), null));
                        } else {
                            if (filename.endsWith("." + mExtension)) {
                                fls.add(new FileChooserOptions(ff.getName(),
                                        "File Size: " + ff.length(), ff.getAbsolutePath(), null));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        if (!onlyFiles && !externalStorageDir.equals(f.getName())) {
            dir.add(0, new FileChooserOptions("..", "Parent Directory", f.getParent(), true));
        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        return dir;
    }

    protected String getActionBarSubTitle() {
        return getActivity().getString(R.string.fileChooserSubtitel);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setSubtitle(getActionBarSubTitle());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fileChooserListener = (FileChooserListener) context;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    private void onFileClick(FileChooserOptions o) {
        Toast.makeText(getActivity(), "File Clicked: " + currentDir + "/" + o.getName(),
                Toast.LENGTH_SHORT).show();
        String selectedFilename = currentDir + "/" + o.getName();
        fileChooserListener.onFilenameSelected(selectedFilename);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileChooserOptions o = adapter.getItem(position);
        if (o.getData().equalsIgnoreCase("folder") || o.getData()
                .equalsIgnoreCase("parent directory")) {
            currentDir = new File(o.getPath());
            List content = createContent(currentDir);
            adapter = new FileChooserAdapter(getActivity(), R.layout.awlib_filechooser_items,
                    content);
            mListView.setAdapter(adapter);
        } else {
            onFileClick(o);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileChooserOptions o = adapter.getItem(position);
        if (!o.getData().equalsIgnoreCase("folder") && !o.getData()
                .equalsIgnoreCase("parent directory")) {
            String title = getString(R.string.awlib_deleteFile);
            String message = o.getName();
            AWLibDialogHinweis hinweis = AWLibDialogHinweis.newInstance(true, title, message);
            hinweis.setOnDailogClickListener(this);
        }
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.awlib_defaultListView);
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
        mExtension = args.getString(FILEEXTENSION);
        onlyFiles = args.getBoolean(ONLYFOLDERFILES, false);
        String startDirectory = args.getString(STARTDIRECTORY);
        if (startDirectory == null) {
            currentDir = Environment.getExternalStorageDirectory();
        } else {
            currentDir = new File(startDirectory);
        }
        externalStorageDir = currentDir.getName();
        List content = createContent(currentDir);
        adapter = new FileChooserAdapter(getActivity(), R.layout.awlib_filechooser_items, content);
        mListView.setAdapter(adapter);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }

    public interface FileChooserListener {
        void onFilenameSelected(String filename);
    }
}
