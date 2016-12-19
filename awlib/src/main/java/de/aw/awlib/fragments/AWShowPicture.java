package de.aw.awlib.fragments;

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
