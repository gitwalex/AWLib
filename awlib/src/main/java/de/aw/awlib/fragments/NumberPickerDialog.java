package de.aw.awlib.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.NumberPicker;

import de.aw.awlib.R;

/**
 * Simpler NumberPickerDialog
 */
public class NumberPickerDialog extends AWFragment implements NumberPicker.OnValueChangeListener {
    public static final int NUMBERPICKER_RESULT = 100;
    private final static int layout = R.layout.awlib_numberpicker;
    private static final String MINVALUE = "MINVALUE";
    private static final String MAXVALUE = "MAXVALUE";
    private static final String DISPLAYEDVALUES = "DISPLAYEDVALUES";
    private NumberPicker mNumberPicker;
    private int mTitle;
    private int mValue;

    /**
     * Erstellt einen NumberPickerDialog
     *
     * @param minValue
     *         Kleinster Wert
     * @param maxValue
     *         groesster Wert
     *
     * @return NumberPickerDialog
     */
    public static NumberPickerDialog newInstance(int minValue, int maxValue) {
        Bundle args = new Bundle();
        args.putInt(MINVALUE, minValue);
        args.putInt(MAXVALUE, maxValue);
        NumberPickerDialog fragment = new NumberPickerDialog();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Erstellt einen NumberPickerDialog
     *
     * @param displayedValues
     *         Angezeigte Werte. Note: Laenge des Arrays muss gleich sein zur Range der
     *         auswaehlbaren Nummern, das ist gleich getMaxValue() - getMinValue() + 1.
     * @param minValue
     *         Kleinster Wert
     * @param maxValue
     *         groesster Wert
     *
     * @return NumberPickerDialog
     */
    public static NumberPickerDialog newInstance(String[] displayedValues, int minValue,
                                                 int maxValue) {
        Bundle args = new Bundle();
        args.putStringArray(DISPLAYEDVALUES, displayedValues);
        NumberPickerDialog fragment = NumberPickerDialog.newInstance(minValue, maxValue);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dlg = super.onCreateDialog(savedInstanceState);
        dlg.setTitle(mTitle);
        return dlg;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!isCanceled) {
            mNumberPicker.clearFocus();
            AWFragment f = (AWFragment) getTargetFragment();
            if (f != null) {
                f.onActivityResult(getTargetRequestCode(), mNumberPicker.getValue(), null);
            }
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        Log("Value" + i1);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNumberPicker = (NumberPicker) view.findViewById(R.id.npNumberpicker);
        mNumberPicker.setMinValue(args.getInt(MINVALUE));
        mNumberPicker.setMaxValue(args.getInt(MAXVALUE));
        String[] displayedValues = args.getStringArray(DISPLAYEDVALUES);
        if (displayedValues != null) {
            mNumberPicker.setDisplayedValues(displayedValues);
        }
        mNumberPicker.setValue(mValue);
        mNumberPicker.setOnValueChangedListener(this);
    }

    @Override
    protected void setInternalArguments(Bundle args) {
        super.setInternalArguments(args);
        args.putInt(LAYOUT, layout);
    }

    public void setTitle(int titleResID) {
        mTitle = titleResID;
    }

    public void setValue(int value) {
        if (mNumberPicker != null) {
            mNumberPicker.setValue(value);
        }
        mValue = value;
    }
}
