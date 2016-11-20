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
package de.aw.awlib.calculator;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import de.aw.awlib.R;

/**
 * Created by alex on 11.03.2016.
 * <p/>
 * http://innovativenetsolutions.com/2013/01/calculator-app/
 */
public class AWLibCalculatorView extends LinearLayout implements View.OnClickListener {
    private static final int layout = R.layout.awlib_calculator;
    private static final String DIGITS = "0123456789.";
    private static final int[] buttonIds =
            new int[]{R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
                    R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
                    R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide,
                    R.id.buttonToggleSign, R.id.buttonDecimalPoint, R.id.buttonEquals,
                    R.id.buttonClear, R.id.buttonClearMemory, R.id.buttonAddToMemory,
                    R.id.buttonSubtractFromMemory, R.id.buttonRecallMemory, R.id.buttonSquareRoot,
                    R.id.buttonSquared, R.id.buttonInvert, R.id.buttonSine, R.id.buttonCosine,
                    R.id.buttonTangent};
    DecimalFormat df = new DecimalFormat("@###########");
    private Double initialValue;
    private CalculatorBrain mCalculatorBrain;
    private TextView mCalculatorDisplay;
    private ResultListener mCalculatorResultListener;
    private Boolean userIsInTheMiddleOfTypingANumber = false;

    public AWLibCalculatorView(Context context) {
        super(context);
        onInflate(context);
    }

    public AWLibCalculatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInflate(context);
    }

    public AWLibCalculatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInflate(context);
    }

    @Override
    public void onClick(View v) {
        String buttonPressed = ((Button) v).getText().toString();
        if (DIGITS.contains(buttonPressed)) {
            // digit was pressed
            if (userIsInTheMiddleOfTypingANumber) {
                //noinspection StatementWithEmptyBody
                if (buttonPressed.equals(".") && mCalculatorDisplay.getText().toString()
                        .contains(".")) {
                    // ERROR PREVENTION
                    // Eliminate entering multiple decimals
                } else {
                    mCalculatorDisplay.append(buttonPressed);
                }
            } else {
                if (buttonPressed.equals(".")) {
                    // ERROR PREVENTION
                    // This will avoid error if only the decimal is hit before an operator, by placing a leading zero
                    // before the decimal
                    mCalculatorDisplay.setText(0 + buttonPressed);
                } else {
                    mCalculatorDisplay.setText(buttonPressed);
                }
                userIsInTheMiddleOfTypingANumber = true;
            }
            if (mCalculatorResultListener != null) {
                double value = Double.parseDouble(mCalculatorDisplay.getText().toString());
                mCalculatorResultListener.onResultChanged(value);
            }
        } else {
            // operation was pressed
            if (userIsInTheMiddleOfTypingANumber) {
                mCalculatorBrain
                        .setOperand(Double.parseDouble(mCalculatorDisplay.getText().toString()));
                userIsInTheMiddleOfTypingANumber = false;
            }
            mCalculatorBrain.performOperation(buttonPressed);
            onResultChanged(mCalculatorBrain.getResult());
        }
    }

    private void onInflate(Context context) {
        inflate(context, layout, this);
        mCalculatorDisplay = (TextView) findViewById(R.id.calculatorResult);
        mCalculatorBrain = new CalculatorBrain(initialValue);
        df.setMinimumFractionDigits(0);
        df.setMinimumIntegerDigits(1);
        df.setMaximumIntegerDigits(8);
        for (int resID : buttonIds) {
            View v = findViewById(resID);
            if (v != null) {
                v.setOnClickListener(this);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle args = (Bundle) state;
        super.onRestoreInstanceState(args.getParcelable("SAVESTATE"));
        mCalculatorBrain.setOperand(args.getDouble("OPERAND"));
        mCalculatorBrain.setMemory(args.getDouble("MEMORY"));
        onResultChanged(mCalculatorBrain.getResult());
    }

    private void onResultChanged(double result) {
        mCalculatorDisplay.setText(df.format(mCalculatorBrain.getResult()));
        if (mCalculatorResultListener != null) {
            mCalculatorResultListener.onResultChanged(result);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable outState = super.onSaveInstanceState();
        Bundle args = new Bundle();
        args.putDouble("MEMORY", mCalculatorBrain.getMemory());
        args.putDouble("OPERAND", mCalculatorBrain.getResult());
        args.putParcelable("SAVESTATE", outState);
        return args;
    }

    public void setInitialValue(double initialValue) {
        this.initialValue = initialValue;
    }

    public void setResultListener(ResultListener listener) {
        mCalculatorResultListener = listener;
        findViewById(R.id.calculatorDisplay).setVisibility(GONE);
    }

    public interface ResultListener {
        void onResultChanged(Double result);
    }
}
