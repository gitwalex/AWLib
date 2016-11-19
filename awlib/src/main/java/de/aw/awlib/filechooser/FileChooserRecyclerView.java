package de.aw.awlib.filechooser;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import de.aw.awlib.R;
import de.aw.awlib.recyclerview.AWLibArrayRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;
import de.aw.awlib.recyclerview.ArrayRecyclerViewAdapter;

/**
 * Created by alex on 19.11.2016.
 */
public class FileChooserRecyclerView<T extends File> extends AWLibArrayRecyclerViewFragment {
    private static final String DIRECTORYNAME = "DIRECTORYNAME";
    private static final int[] viewResIDs =
            new int[]{R.id.awlib_fileName, R.id.awlib_fileData, R.id.folderImage};
    private static final int viewHolderLayout = R.layout.awlib_filechooser_items;
    private String mDirectoy;

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
    public static FileChooserRecyclerView newInstance(String directoryAbsolutPathName) {
        Bundle args = new Bundle();
        File file = new File(directoryAbsolutPathName);
        if (!file.isDirectory()) {
            throw new IllegalStateException("File ist kein Directory");
        }
        args.putString(DIRECTORYNAME, directoryAbsolutPathName);
        FileChooserRecyclerView fragment = new FileChooserRecyclerView();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public ArrayRecyclerViewAdapter getArrayAdapter() {
        mFileArrayAdapter<T> adapter = new mFileArrayAdapter<T>(this);
        File file = new File(mDirectoy);
        adapter.swapValues(file.listFiles());
        return adapter;
    }

    @Override
    protected boolean onBindView(AWLibViewHolder holder, View view, int resID, Object object) {
        TextView tv;
        File file = (File) object;
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
            tv.setText(Formatter.formatFileSize(getContext(), file.length()));
            consumed = true;
        }
        return consumed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDirectoy = args.getString(DIRECTORYNAME);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putIntArray(VIEWRESIDS, viewResIDs);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }

    private class mFileArrayAdapter<T extends File> extends ArrayRecyclerViewAdapter<File> {
        /**
         * Initialisiert Adapter.
         *
         * @param binder
         *         ArrayViewHolderBinder. Wird gerufen,um die einzelnen Views zu initialisieren
         */
        protected mFileArrayAdapter(@NonNull ArrayViewHolderBinder<T> binder) {
            super(binder);
        }
    }
}
