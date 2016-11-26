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
package de.aw.awlib.preferences;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.fragments.AWFragment;

/**
 * Template fuer MonMaFragmente
 * <p/>
 * Folgende Funktionen: - Bereitstellung eines Bundle 'args' fuer alle abgeleiteten Klassen - Setzen
 * HasOptionsMenu(): Alle Fragmente haben OptionsMenu
 */
public class PreferenceDialogFragment extends AWFragment
        implements AWInterface, DialogInterface.OnClickListener {
    private static final String TITLE = "TITLE", HINWEIS = "HINWEIS";
    protected MainAction action;
    private Bundle args;

    /**
     * @param title
     *         Titel des Dialogs
     * @param hinweistext
     *         Hinweis-/Fehlermeldungen. Mehrere Fehlermeldungen werden als Aufzaehlung angezeigt.
     *
     * @return DialogFragment
     */
    public static PreferenceDialogFragment newInstance(String title, String hinweistext) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(HINWEIS, hinweistext);
        PreferenceDialogFragment f = new PreferenceDialogFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.awlib_btnAccept, this);
        builder.setTitle(args.getString(TITLE));
        String hinweise = args.getString(HINWEIS);
        builder.setMessage(hinweise);
        Dialog dialog = builder.create();
        // Wenn das Dialogfenster teilweise von der eingeblendeten Tatstatur
        // ueberlappt wird, resize des Fensters zulassen.
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }
}