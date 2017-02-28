package de.aw.awlib.fragments;

/*
 * AWLib: Eine Bibliothek  zur schnellen Entwicklung datenbankbasierter Applicationen
 *
 * Copyright [2015] [Alexander Winkler, 2373 Dahme/Germany]
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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileFilter;

import de.aw.awlib.R;
import de.aw.awlib.adapters.AWItemListAdapter;
import de.aw.awlib.recyclerview.AWItemListRecyclerViewFragment;
import de.aw.awlib.recyclerview.AWLibViewHolder;
import de.aw.awlib.utils.AWUtils;

/**
 * Zeigt eine ImageGallery. Der Directoryname muss im Bundle unter 'FILENAME' als String geliefert
 * wewrden. Bilder mit Extension '.jpg' in diesem Directory werden angezeigt. Gibt es unter
 * 'FRAGMENTTITLE' einen Text, wird dieser als Titel angezeigt. Ansonsten der Letzte Teil des
 * Directorynamens.
 * <p>
 * Wird ein Foto erstellt, wird dieses Foto im Directoty abgelegt.
 */
public class AWImageGallery extends AWItemListRecyclerViewFragment<File> {
    private static final int layout = R.layout.awlib_default_recycler_view;
    private static final int viewHolderLayout = R.layout.awlib_imagegalleryrc;
    private String mPicturePath;
    private String mTitel;
    private String mFolderName;

    /**
     * Neue Instanz
     *
     * @param args
     *         Bundle mit mindestens einem Directorynamen unter 'FILENAME' als String. Bilder mit
     *         Extension '.jpg' in diesem Directory werden angezeigt.Gibt es unter 'FRAGMENTTITLE'
     *         einen Text, wied dieser als Titel angezeigt. Ansonsten der Letzte Teil des
     *         Filenamens
     * @return Neues Fragment
     */
    public static AWImageGallery newInstance(@NonNull Bundle args) {
        AWImageGallery fragment = new AWImageGallery();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected AWItemListAdapter<File> createListAdapter() {
        return new AWItemListAdapter<File>(this) {
            @Override
            protected long getID(File item) {
                return 0;
            }
        };
    }

    @Nullable
    private File[] getFiles() {
        File folder = new File(mPicturePath);
        if (folder.exists()) {
            return folder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() && file.length() > 0 &&
                            file.getName().toLowerCase().endsWith(".jpg");
                }
            });
        }
        return null;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(getContext(), maxRecyclerViewColumns(320));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICTURE_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                getAdapter().swap(getFiles());
            }
        }
    }

    @Override
    public void onBindViewHolder(AWLibViewHolder holder, File item, int position) {
        ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.imageView);
        Glide.with(getContext()).load(item).override(320, 320).centerCrop().into(imageView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPicturePath = args.getString(FILENAME);
        mFolderName = Uri.parse(mPicturePath).getLastPathSegment();
        mTitel = args.getString(FRAGMENTTITLE);
        if (mTitel == null) {
            mTitel = mFolderName;
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.awlib_menu_camera_item, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menuitem_camera) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                Intent intent = AWUtils.prepareNewPhoto(getActivity(), mFolderName);
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_PICTURE_RESULT);
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CAMERA);
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_PERMISSION_CAMERA:
                    Intent intent = AWUtils.prepareNewPhoto(getActivity(), mPicturePath);
                    if (intent != null) {
                        startActivityForResult(intent, REQUEST_PICTURE_RESULT);
                    }
                    break;
                case REQUEST_PERMISSION_STORAGE:
                    getAdapter().swap(getFiles());
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(mTitel);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAdapter().swap(getFiles());
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
        args.putInt(VIEWHOLDERLAYOUT, viewHolderLayout);
    }
}
