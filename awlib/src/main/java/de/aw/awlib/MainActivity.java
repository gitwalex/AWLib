package de.aw.awlib;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AWLibMainActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDefaultFAB().setVisibility(View.VISIBLE);
    }
}
