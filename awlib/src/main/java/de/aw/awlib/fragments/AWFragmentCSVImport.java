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

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.io.File;
import java.util.concurrent.ExecutionException;

import de.aw.awlib.AWResultCodes;
import de.aw.awlib.R;
import de.aw.awlib.csvimportexport.AWCSVImporter;

/**
 * Created by alex on 10.11.2015.
 */
public class AWFragmentCSVImport extends AWFragment implements AWResultCodes {
    public static final String PATH = "PATH";

    public static AWFragmentCSVImport newInstance(String path) {
        AWFragmentCSVImport f = new AWFragmentCSVImport();
        Bundle args = new Bundle();
        args.putString(PATH, path);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String filename = args.getString(PATH);
        try {
            readCSVFile(filename);
        } catch (ExecutionException | InterruptedException e) {
            //TODO Execption bearbeiten
            e.printStackTrace();
        }
    }

    private int readCSVFile(final String filename) throws ExecutionException, InterruptedException {
        return new AsyncTask<String, Integer, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                return new AWCSVImporter(getActivity()).execute(params[0]);
            }

            /**
             * Zeigt nach Abschluss des Exports eine Snackbar zum
             * Oeffnen des exportfiles
             *
             * @param result
             *         HTTPDownloadResult des Exports
             */
            @Override
            protected void onPostExecute(Integer result) {
                switch (result) {
                    case RESULT_OK:
                        Snackbar snackbar = Snackbar.make(getView(), R.string.exportErfolgreich,
                                Snackbar.LENGTH_LONG).setAction(R.string.awlib_openFile,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        File file = new File(filename);
                                        final Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setDataAndType(Uri.fromFile(file), "text/csv");
                                        getActivity().startActivity(intent);
                                    }
                                });
                        snackbar.show();
                }
                super.onPostExecute(result);
            }
        }.execute(filename).get();
    }
}
