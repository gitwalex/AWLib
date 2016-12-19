package de.aw.awlib.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by alex on 19.12.2016.
 */
public class ImageAdapter extends BaseAdapter {
    private final File[] mImageFiles;
    private final Context mContext;

    public ImageAdapter(Context c, File[] imageFiles) {
        mContext = c;
        mImageFiles = imageFiles;
    }

    public int getCount() {
        return mImageFiles.length;
    }

    public File getItem(int position) {
        return mImageFiles[position];
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        Picasso.with(mContext).load(mImageFiles[position]).noFade().resize(180, 180).centerCrop()
                .into(imageView);
        return imageView;
    }
}
