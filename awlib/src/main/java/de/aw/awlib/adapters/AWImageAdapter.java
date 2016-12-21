package de.aw.awlib.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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
     * Das Bild wird mittels Picasso geladen
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
        Picasso.with(mContext).load(mImageFiles[position]).noFade().resize(180, 180).centerCrop()
                .into(imageView);
        return imageView;
    }
}
