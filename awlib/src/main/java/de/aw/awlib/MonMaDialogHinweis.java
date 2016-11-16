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
package de.aw.awlib;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

/**
 * Allgemeiner Dialog fuer Hinweise und Fehlermeldungen.
 */
public class MonMaDialogHinweis extends AWLibFragment {
    private static final String SHOWOKBUTTON = "SHOWOKBUTTON";
    private static final String TITLE = "TITLE", HINWEIS = "HINWEIS";
    private DialogInterface.OnClickListener mOnDailogClickListener;

    /**
     * @param showOKButton
     *         treu: OK-Button wird gezeigt
     * @param title
     *         Titel des Dialogs
     * @param hinweistext
     *         Hinweis-/Fehlermeldungen. Mehrere Fehlermeldungen werden als Aufzaehlung angezeigt.
     *
     * @return DialogFragment
     */
    public static MonMaDialogHinweis newInstance(boolean showOKButton, String title,
                                                 String... hinweistext) {
        Bundle args = new Bundle();
        args.putBoolean(SHOWOKBUTTON, showOKButton);
        args.putString(TITLE, title);
        args.putStringArray(HINWEIS, hinweistext);
        MonMaDialogHinweis f = new MonMaDialogHinweis();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mOnDailogClickListener != null) {
            mOnDailogClickListener.onClick(dialog, which);
        }
        super.onClick(dialog, which);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (args.getBoolean(SHOWOKBUTTON)) {
            builder.setPositiveButton(R.string.btnAccept, this);
        }
        builder.setTitle(args.getString(TITLE));
        String[] hinweise = args.getStringArray(HINWEIS);
        StringBuilder text = new StringBuilder();
        String delimiter = "";
        if (hinweise.length > 1) {
            delimiter = "- ";
        }
        for (String t : hinweise) {
            text.append(delimiter).append(t).append(linefeed);
        }
        builder.setMessage(text.toString());
        Dialog dialog = builder.create();
        // Wenn das Dialogfenster teilweise von der eingeblendeten Tatstatur
        // ueberlappt wird, resize des Fensters zulassen.
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    /**
     * Setzt einen Listener fuer den Dialog. Wird keiner gesetzt, wird der Dialog nach Click
     * beendet.
     *
     * @param mOnDailogClickListener
     *         OnClickListener
     */
    public void setOnDailogClickListener(DialogInterface.OnClickListener mOnDailogClickListener) {
        this.mOnDailogClickListener = mOnDailogClickListener;
    }
}
