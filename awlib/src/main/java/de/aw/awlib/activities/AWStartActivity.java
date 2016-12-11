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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

import de.aw.awlib.application.AWApplication;
import de.aw.awlib.application.ApplicationConfig;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static de.aw.awlib.application.AWApplication.DE_AW_APPLICATIONPATH;

/**
 * Template fuer Activities. Implementiert das globale Menu sowie die entsprechenden Reaktionen
 * darauf. Ausserdem wird dafuer gesorgt, dass bei Auswahl des MenuButtons des Geraetes der
 * OverFlow-Butten angezeigt wird. Es wird ein Bundle args bereitgestellt, welches immer gesichert
 * bzw. restored wird.
 */
public class AWStartActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private void checkPermissions() {
        int permissionCheck =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    /**
     * Allgemeine Aufgaben fuer onCreate: - rufen von onCreate(Bundle, layout) mit Standardlayout. -
     * ContentView ist container in activity_container
     */
    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                            .putBoolean("isFirstRun", false).apply();
                    ApplicationConfig config =
                            ((AWApplication) getApplicationContext()).getApplicationConfig();
                    File folder = new File(DE_AW_APPLICATIONPATH);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    config.createFiles();
                    setResult(RESULT_OK);
                } else {
                    setResult(RESULT_CANCELED);
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        finish();
    }
}