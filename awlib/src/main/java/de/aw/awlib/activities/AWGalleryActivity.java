package de.aw.awlib.activities;

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

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.io.File;

import de.aw.awlib.R;
import de.aw.awlib.application.AWApplication;
import de.aw.awlib.fragments.AWShowPicture;

/**
 * Zeigt eine Gallery von Bildern in einem ViewPager.
 */
public class AWGalleryActivity extends AWBasicActivity {
    private static int layout = R.layout.awactivity_gallery;

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, layout);
        ViewPager pager = (ViewPager) findViewById(R.id.GalleryPager);
        String mDirectory = args.getString(FILENAME);
        if (mDirectory == null) {
            AWApplication.Log("Kein Verzeichnis in Intent unter 'DIRECTORY'");
            finish();
        } else {
            File directory = new File(mDirectory);
            if (directory.isDirectory()) {
                File[] pictures = directory.listFiles();
                if (pictures != null) {
                    ScreenSlidePagerAdapter adapter =
                            new ScreenSlidePagerAdapter(getSupportFragmentManager(), pictures);
                    pager.setAdapter(adapter);
                } else {
                    AWApplication.Log("Kein Zugriff");
                }
            } else {
                AWApplication.Log(mDirectory + " ist kein Verzeichnis");
                finish();
            }
        }
    }

    /**
     * Adapter fuer Pictures
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final File[] mPictures;

        /**
         * @param fm
         *         FragmentManager
         * @param pictures
         *         Picture-Files
         */
        ScreenSlidePagerAdapter(FragmentManager fm, File[] pictures) {
            super(fm);
            mPictures = pictures;
        }

        @Override
        public int getCount() {
            return mPictures == null ? 0 : mPictures.length;
        }

        @Override
        public Fragment getItem(int position) {
            return AWShowPicture.newInstance(mPictures[position].getAbsolutePath());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Uri uri = Uri.parse(mPictures[position].getAbsolutePath());
            return uri.getLastPathSegment();
        }
    }
}
