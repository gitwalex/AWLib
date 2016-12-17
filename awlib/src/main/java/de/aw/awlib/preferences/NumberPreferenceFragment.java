package de.aw.awlib.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v7.preference.DialogPreference;
import android.view.View;
import android.widget.NumberPicker;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;

/**
 * Fragment zur Ermittlung und speicherung einer Zeit in Minuten seit Mitternacht.
 * <p>
 * In der in {@link NumberPreferenceFragment#newInstance(NumberPreference)} uebergebenen Preference
 * wird der eingestellte Wert als int gespeichert.
 * <p>
 * Ausserdem wird das rufende Fragent durch {@link PreferenceDialogFragment#onActivityResult(int,
 * int, Intent)} mit folgenden Daten benachrichtig:
 * <p>
 * requestCode: wird beim Erstellen des Dialogs eingestellt.
 * <p>
 * resultCode: Konstant {@link AWInterface#DIALOGRESULT}
 * <p>
 * intent: null
 */
public class NumberPreferenceFragment
        extends android.support.v14.preference.PreferenceDialogFragment implements AWInterface {
    private NumberPicker mNumberPicker;

    /**
     * Erstellt einen NumberDialog zur NumberPreference
     *
     * @param pref
     *         NumberPreference
     *
     * @return Fragment
     */
    public static NumberPreferenceFragment newInstance(NumberPreference pref) {
        final NumberPreferenceFragment fragment = new NumberPreferenceFragment();
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
        mNumberPicker = (NumberPicker) view.findViewById(R.id.pNumberPicker);
        // Exception when there is no NumberPicker
        if (mNumberPicker == null) {
            throw new IllegalStateException(
                    "Dialog view must contain" + " a NumberPicker with id 'pNumberPicker'");
        }
        DialogPreference preference = getPreference();
        if (preference instanceof NumberPreference) {
            int number = ((NumberPreference) preference).getNumber();
            mNumberPicker.setMinValue(1);
            mNumberPicker.setMaxValue(180);
            mNumberPicker.setValue(number);
        }
    }

    /**
     * Wenn der Dailog durch OK geschlossen wurde, wird das Ergebnis persisted, die Summary
     * aktualisiert und das TargetFragment benachrichtigt..
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // generate value to save
            int number = mNumberPicker.getValue();
            // Get the related Preference and save the value
            NumberPreference timePreference = ((NumberPreference) getPreference());
            timePreference.setNumber(number);
            Intent intent = new Intent();
            intent.putExtra(DIALOGRESULT, number);
            getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        }
    }
}
