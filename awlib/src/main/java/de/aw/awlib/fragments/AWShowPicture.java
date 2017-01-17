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

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import de.aw.awlib.R;

/**
 * Created by alex on 19.12.2016.
 */
public class AWShowPicture extends AWFragment {
    private static final int layout = R.layout.awlib_showpicture;

    public static AWShowPicture newInstance(String filename) {
        Bundle args = new Bundle();
        args.putString(FILENAME, filename);
        AWShowPicture fragment = new AWShowPicture();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String mFilename = args.getString(FILENAME);
        File mFile = new File(mFilename);
        Picasso.with(getContext()).load(mFile).into((ImageView) view.findViewById(R.id.imgView));
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }
}
