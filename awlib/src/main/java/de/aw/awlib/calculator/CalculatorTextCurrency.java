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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import de.aw.awlib.R;
import de.aw.awlib.database.AWLibDBConvert;
import de.aw.awlib.views.AWLibTextCurrency;

/**
 * Zeigt einen Betrag in der jeweiligen Waehrung an. Als Defult wird bei negativen Werten der Text
 * in rot gezeigt. Das kann durch {@link CalculatorTextCurrency#setColorMode(boolean)} geaendert
 * werden.
 */
public class CalculatorTextCurrency extends AWLibTextCurrency
        implements AWLibCalculatorView.ResultListener, View.OnClickListener {
    private PopupWindow calculatorPopUp;
    private Double initialValue;
    private int mIndex;
    private OnClickListener mOnClickListener;
    private OnLongValueChangedListener mOnValueChangeListener;

    public CalculatorTextCurrency(Context context) {
        super(context);
    }

    public CalculatorTextCurrency(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CalculatorTextCurrency(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getIndex() {
        return mIndex;
    }

    public void hideCalculator() {
        if (calculatorPopUp.isShowing()) {
            calculatorPopUp.dismiss();
        }
        setTypeface(Typeface.DEFAULT);
    }

    @Override
    public void onClick(View v) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
        if (calculatorPopUp.isShowing()) {
            hideCalculator();
        } else {
            showCalculator();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        calculatorPopUp.dismiss();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //        setFocusable(true);
        setClickable(true);
        super.setOnClickListener(this);
        setFocusableInTouchMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(R.style.TextViewMain_Medium);
        } else {
            //noinspection deprecation
            setTextAppearance(getContext(), R.style.TextViewMain_Medium);
        }
        AWLibCalculatorView mCalculator = new AWLibCalculatorView(getContext());
        if (initialValue != null) {
            mCalculator.setInitialValue(initialValue);
        }
        mCalculator.setResultListener(this);
        calculatorPopUp = new PopupWindow(mCalculator, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        calculatorPopUp.setClippingEnabled(false);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            showCalculator();
        } else {
            hideCalculator();
        }
    }

    @Override
    public void onResultChanged(Double result) {
        setValue((long) (result * 100));
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    public void setOnLongValueChangedListener(OnLongValueChangedListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * Setzt einen long-Wert als Text. Dieser wird in das entsprechende Currency-Format
     * umformatiert.
     *
     * @param amount
     *         Wert zur Anzeige
     */
    @Override
    public void setValue(long amount) {
        if (!isInEditMode()) {
            super.setValue(amount);
            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onLongValueChanged(this, amount);
            }
            initialValue = amount / AWLibDBConvert.mCurrencyDigits;
        }
    }

    public void showCalculator() {
        if (hasFocus() && !calculatorPopUp.isShowing()) {
            calculatorPopUp.showAsDropDown(this);
            setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    /**
     * Interface fuer Listener auf Wertaenderungen
     */
    public interface OnLongValueChangedListener {
        /**
         * Wird gerufen, wenn sich der Wert geaendert hat
         *
         * @param view
         *         view
         * @param newAmount
         *         neuer Wert
         */
        void onLongValueChanged(View view, long newAmount);
    }
}
