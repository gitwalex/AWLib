package de.aw.awlib.preferences;

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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;

/**
 * Created by alex on 04.03.2017.
 */
public class EmailPreferenceFragment extends PreferenceDialogFragment implements AWInterface {
    private EditText mEmailText;

    /**
     * Erstellt einen NumberDialog zur NumberPreference
     *
     * @param pref
     *         NumberPreference
     * @return Fragment
     */
    public static EmailPreferenceFragment newInstance(EmailPreference pref) {
        final EmailPreferenceFragment fragment = new EmailPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, pref.getKey());
        fragment.setArguments(b);
        return fragment;
    }

    /**
     * Erstellt den Dialog. View muss ein Element {@link NumberPicker} mit der id 'pNumberPicker'
     * enthalten
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEmailText = (EditText) view.findViewById(R.id.emailEditText);
        // Exception when there is no NumberPicker
        if (mEmailText == null) {
            throw new IllegalStateException(
                    "Dialog view must contain a EditText with id 'emailEditText'");
        }
        EmailPreference eMailPreference = (EmailPreference) getPreference();
        mEmailText.setText(eMailPreference.getText());
        mEmailText.selectAll();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // generate value to save
            String text = mEmailText.getText().toString();
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                // Get the related Preference and save the value
                EmailPreference eMailPreference = (EmailPreference) getPreference();
                eMailPreference.setText(text);
                Fragment targetFragment = getTargetFragment();
                if (targetFragment != null) {
                    Intent intent = new Intent();
                    intent.putExtra(DIALOGRESULT, text);
                    targetFragment.onActivityResult(getTargetRequestCode(), 0, intent);
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.pkEmpfaenger);
                builder.setMessage(R.string.noValidEmailAdress);
                builder.setPositiveButton(R.string.awlib_btnAccept,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.create().show();
            }
        }
    }
}