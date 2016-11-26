/*
 * MonMa: Eine freie Android-App fuer Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 23730 Neustadt/Germany]
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
package de.aw.awlib.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;

import de.aw.awlib.R;

/**
 * zeigt eine interne html-Seite. Im Intent wird unter ID der Name des html-files erwartet und im
 * Verzeichnis assets/html gesucht.
 */
public class AWWebViewActivity extends FragmentActivity implements AWInterface {
    private static final String path = "file:///android_asset/html/";
    private WebView webView;

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.awlib_webview);
        webView = (WebView) findViewById(R.id.awlib_webView);
        String htmlSeite = getIntent().getExtras().getString(ID);
        webView.loadUrl(path + htmlSeite);
    }
}
