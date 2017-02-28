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

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import de.aw.awlib.R;

/**
 * Zeigt ein Image. Der Name des zu ladenden Files wird im Titel angezeigt.Gibt es im Bundle unter
 * 'FRAGMENTTITLE' einen Text, wird dieser als Titel angezeigt. Ansonsten der Letzte Teil des
 * Filenamens
 */
public class AWShowPicture extends AWFragment {
    private static final int layout = R.layout.awlib_zoomableimageview;
    private String mFilename;
    private String mTitel;

    /**
     * Neue Instanz
     *
     * @param args
     *         Bundle mit mindestens einem Filenamen unter 'FILENAME' als String. Dieses Bild wird
     *         angezeigt. Gibt es unter 'FRAGMENTTITLE' einen Text, wied dieser als Titel angezeigt.
     *         Ansonsten der Letzte Teil des Filenamens
     * @return Neues Fragment
     */
    public static AWShowPicture newInstance(@NonNull Bundle args) {
        AWShowPicture fragment = new AWShowPicture();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFilename = args.getString(FILENAME);
        assert mFilename != null;
        mTitel = args.getString(FRAGMENTTITLE);
        if (mTitel == null) {
            Uri uri = Uri.parse(mFilename);
            mTitel = uri.getLastPathSegment();
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
        ImageView imageView = (ImageView) view.findViewById(R.id.imgView);
        Glide.with(getContext()).load(mFilename).asBitmap().into(imageView);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }
}
