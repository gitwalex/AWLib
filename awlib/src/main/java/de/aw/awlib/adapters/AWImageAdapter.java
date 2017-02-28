package de.aw.awlib.adapters;

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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Adapter fuer Anzeige von Images, die sich auf dem Geraet befinden.
 */
public class AWImageAdapter extends BaseAdapter {
    private final File[] mImageFiles;
    private final Context mContext;

    /**
     * @param c
     *         Context
     * @param imageFiles
     *         Array der anzuzeigenden Files
     */
    public AWImageAdapter(Context c, File[] imageFiles) {
        mContext = c;
        mImageFiles = imageFiles;
    }

    @Override
    public int getCount() {
        return mImageFiles.length;
    }

    @Override
    public File getItem(int position) {
        return mImageFiles[position];
    }

    /**
     * Die Id des Bildes ist der Wert der Position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Erstellt bei Bedarf ein ImageView fuer das zu an die position zu ladende Bild.
     * <p>
     * Das Bild wird mittels Glide geladen
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        Glide.with(mContext).load(mImageFiles[position]).override(300, 400).centerCrop()
             .into(imageView);
        return imageView;
    }
}
