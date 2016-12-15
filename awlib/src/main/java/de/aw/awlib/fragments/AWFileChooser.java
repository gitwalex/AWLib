package de.aw.awlib.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.aw.awlib.R;
import de.aw.awlib.recyclerview.AWArrayRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;

/**
 * FileChooser fuer Dateien. Ermittelt vor Anzeige die Berechtigung, wenn erforderlich.
 */
public class AWFileChooser extends AWArrayRecyclerViewFragment<File> {
    protected static final String DIRECTORYNAME = "DIRECTORYNAME";
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_fileName, R.id.awlib_fileData, R.id.folderImage};
    private static final int viewHolderLayout = R.layout.awlib_filechooser_items;
    private static final int HASPARENTFOLDER = 1;
    protected boolean hasParent;
    protected String mDirectoy;
    private File mFile;
    private FilenameFilter mFilenameFilter;

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @param directoryAbsolutPathName
     *         Absoluter Pafd des Directory
     *
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static AWFileChooser newInstance(@NonNull String directoryAbsolutPathName) {
        Bundle args = new Bundle();
        File file = new File(directoryAbsolutPathName);
        if (!file.isDirectory()) {
            throw new IllegalStateException("File ist kein Directory");
        }
        args.putString(DIRECTORYNAME, directoryAbsolutPathName);
        AWFileChooser fragment = new AWFileChooser();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Erstellt eine neue Instanz eines FileChooser, zeigt die Daten des uebergebenen
     * Verzeichnisnamen an
     *
     * @param directoryAbsolutPathName
     *         Absoluter Pafd des Directory
     * @param filterExtension
     *         Dateiextension, die gewaehlt werden soll.
     *
     * @return Fragment
     *
     * @throws IllegalStateException
     *         wenn das Verzeichnis kein Directory ist
     */
    public static AWFileChooser newInstance(@NonNull String directoryAbsolutPathName,
                                            @NonNull String filterExtension) {
        Bundle args = new Bundle();
        args.putString(FILENAMEFILTER, filterExtension);
        AWFileChooser fragment = newInstance(directoryAbsolutPathName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Erstellt eine Liste der Files innerhalb eines Directories. Ist das File ungleich dem in
     * {@link AWFileChooser#newInstance(String)} angegebenen Directory, wird am Anfang der Liste der
     * Parent des uebergebenen Files eingefuegt. Damit kann eine Navigation erfolgen.
     * <p>
     * Die erstellte Liste wird direkt in den Adapter einestellt.
     * <p>
     * Ausserdem wird im Subtitle der Toolbar der Name des akuellten Verzeichnisses eingeblendet.
     *
     * @param file
     *         File, zu dem die Liste erstellt werden soll
     */
    private void createFileList(File file) {
        File[] files = file.listFiles(mFilenameFilter);
        List<File> mFiles = Arrays.asList(files);
        Collections.sort(mFiles, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
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
        ArrayList<File> value = new ArrayList<>(mFiles);
        hasParent = !mDirectoy.toLowerCase().equals(file.getAbsolutePath().toLowerCase());
        if (hasParent) {
            value.add(0, file.getParentFile());
        }
        mFile = file;
        setTitle(file.getAbsolutePath());
        mAdapter.swapValues(value);
    }

    /**
     * Prueft, ob an Position 0 eine View eingefuegt werden muss, damit in das uebergeordnete
     * Verzeichnis gewechselt werden kann. Ansonsten wird der Default zuruckgeliefert.
     */
    @Override
    public int getItemViewType(int position, File object) {
        if (position == 0 && hasParent) {
            return HASPARENTFOLDER;
        }
        return super.getItemViewType(position, object);
    }

    /**
     * Wird ein Directory ausgewaehlt, wird in dieses Directory gewechselt.
     */
    @Override
    public void onArrayRecyclerItemClick(RecyclerView recyclerView, View view, Object object) {
        File file = (File) object;
        if (file.isDirectory()) {
            createFileList(file);
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
        final File file = (File) object;
        if (!file.isDirectory()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.awlib_btnAccept,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            File parent = file.getParentFile();
                            file.delete();
                            createFileList(parent);
                        }
                    });
            builder.setTitle(R.string.awlib_deleteFile);
            Dialog dlg = builder.create();
            dlg.show();
            return true;
        }
        return super.onArrayRecyclerItemLongClick(recyclerView, view, object);
    }

    /**
     * Sollte gerufen werden, bevor das Fragment beendet wird.
     *
     * @return true, wenn innerhalb der Verzeichnishierachie eine Stufe nach oben gegangen werden
     * konnte. Dann wird auch gleich eine neue Fileliste angezeigt.
     * <p>
     * Ansonsten false.
     */
    public boolean onBackPressed() {
        if (hasParent) {
            createFileList(mFile.getParentFile());
            return true;
        }
        return false;
    }

    protected boolean onBindView(AWLibViewHolder holder, View view, int resID, File file) {
        TextView tv;
        boolean consumed = false;
        switch (holder.getItemViewType()) {
            case HASPARENTFOLDER:
                if (resID == R.id.folderImage) {
                    ImageView img = (ImageView) view;
                    img.setImageResource(R.drawable.ic_open_folder);
                    consumed = true;
                } else if (resID == R.id.awlib_fileName) {
                    tv = (TextView) view;
                    tv.setText("..");
                    consumed = true;
                } else if (resID == R.id.awlib_fileData) {
                    tv = (TextView) view;
                    tv.setText(file.getAbsolutePath());
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
                    tv.setText(Formatter.formatFileSize(getContext(), file.length()));
                    consumed = true;
                }
        }
        return consumed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDirectoy = args.getString(DIRECTORYNAME);
        final String filenameFilter = args.getString(FILENAMEFILTER);
        mFilenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filenameFilter == null || filename.toLowerCase()
                        .endsWith("." + filenameFilter);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createFileList(new File(mDirectoy));
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Sobald die View erstellt wurde erste Liste zur Verfuegung stellen.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createFileList(new File(mDirectoy));
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_STORAGE);
        }
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }
}

