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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.aw.awlib.application.AWApplication;
import de.aw.awlib.application.ApplicationConfig;

import static de.aw.awlib.application.AWApplication.DE_AW_APPLICATIONPATH;
import static de.aw.awlib.application.AWApplication.Log;

/**
 * Start-Activity fuer alle awlib_applications.
 * <p>
 * Prueft beim Start auf SDK-Version. Ist diese > 22, werden die minimal notwendigen Berechitgungen
 * abgefragt.
 */
public final class AWStartActivity extends AppCompatActivity {
    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1;
    private ApplicationConfig applicationConfig;

    /**
     * Prueft  auf SDK-Version. Ist diese > 22, werden die minimal notwendigen Berechtiungen
     * abgefragt.
     * <p>
     * Auf jeden Fall werden die notwendigen Verzeichnisse angelegt (nur beim ersten Start) und die
     * Activity der Application gemaess {@link ApplicationConfig#getStartActivityClass()}
     * gestartet.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationConfig = ((AWApplication) getApplicationContext()).getApplicationConfig();
        if (android.os.Build.VERSION.SDK_INT > 22) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR}, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        } else {
            startApplication();
        }
    }

    /**
     * Prüft die Vergabe der unbedingt beim Start notwendigen Berechtigungen, Sind nich alle
     * vorhanden, wird derzeit einfach beendet.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:
                List<String> notGranted = new ArrayList<>();
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            notGranted.add(permissions[i]);
                        }
                        i++;
                    }
                    if (notGranted.size() == 0) {
                        startApplication();
                    } else {
                        // TODO: 12.12.2016 Unbedingt Dialog erstellen
                        Log("Sorry, nicht alle Berechtigungen");
                        finish();
                    }
                }
        }
    }

    /**
     * Wenn Berechtigungen vergeben sind (SDK-Version > 22) oder dies nicht notwendig ist, werden
     * hier die Verzeichnisse angelegt und die Activity der App gestartet.
     */
    private void startApplication() {
        Boolean isFirstRun =
                getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            File folder = new File(DE_AW_APPLICATIONPATH);
            if (!folder.exists()) {
                folder.mkdir();
            }
            applicationConfig.createFiles();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false)
                    .apply();
        }
        Class<? extends AWMainActivity> startActivity = applicationConfig.getStartActivityClass();
        Intent intent = new Intent(this, startActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}