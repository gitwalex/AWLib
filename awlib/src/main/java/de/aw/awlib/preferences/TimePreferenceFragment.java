package de.aw.awlib.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v7.preference.DialogPreference;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import de.aw.awlib.R;
import de.aw.awlib.activities.AWInterface;

/**
 * Fragment zur Ermittlung und speicherung einer Zeit in Minuten seit Mitternacht.
 * <p>
 * In der in {@link TimePreferenceFragment#newInstance(TimePreference)} uebergebenen Prefrence wird
 * die eingestellten Minuten als int gespeichert. In der Summary der Preference wird die gewaehlte
 * Uhrzeit im Format 'HH:mm' eingestellt.
 * <p>
 * Ausserdem wird das rufende Fragent durch {@link PreferenceDialogFragment#onActivityResult(int,
 * int, Intent)} mit folgenden Daten benachrichtig: requestCode: wird beim Erstellen des Dialogs
 * eingestellt.
 * <p>
 * resultCode: Konstant {@link AWInterface#DIALOGRESULT}
 * <p>
 * intent: null
 */
public class TimePreferenceFragment extends android.support.v14.preference.PreferenceDialogFragment
        implements AWInterface {
    private TimePicker mTimePicker;

    /**
     * Erstellt einen TimeDialog zur TimePreference
     *
     * @param pref
     *         TimePreference
     *
     * @return Fragment
     */
    public static TimePreferenceFragment newInstance(TimePreference pref) {
        final TimePreferenceFragment fragment = new TimePreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, pref.getKey());
        fragment.setArguments(b);
        return fragment;
    }

    /**
     * Erstellt den Dialog. View muss ein Element {@link TimePicker} mit der id 'pTimePicker'
     * enthalten
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mTimePicker = (TimePicker) view.findViewById(R.id.pTimePicker);
        // Exception when there is no TimePicker
        if (mTimePicker == null) {
            throw new IllegalStateException(
                    "Dialog view must contain" + " a TimePicker with id 'pTimePicker'");
        }
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            int minutesAfterMidnight = ((TimePreference) preference).getTime();
            int hours = minutesAfterMidnight / 60;
            int minutes = minutesAfterMidnight % 60;
            boolean is24hour = DateFormat.is24HourFormat(getActivity());
            mTimePicker.setIs24HourView(is24hour);
            mTimePicker.setCurrentHour(hours);
            mTimePicker.setCurrentMinute(minutes);
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
            int hours = mTimePicker.getCurrentHour();
            int minutes = mTimePicker.getCurrentMinute();
            int minutesAfterMidnight = (hours * 60) + minutes;
            // Get the related Preference and save the value
            TimePreference timePreference = ((TimePreference) getPreference());
            timePreference.setTime(minutesAfterMidnight);
            Intent intent = new Intent();
            intent.putExtra(DIALOGRESULT, minutesAfterMidnight);
            getTargetFragment().onActivityResult(getTargetRequestCode(), 0, intent);
        }
    }
}
