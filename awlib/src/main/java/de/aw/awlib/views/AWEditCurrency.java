package de.aw.awlib.views;

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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import de.aw.awlib.activities.AWInterface;
import de.aw.awlib.database.AWDBConvert;

/**
 * EditText, welcher Currency anzeigt. Hat das Feld den Focus, wird der Text als double angezeigt.
 * Verliert das Feld den Focus, wird der Text mit Waehrungskennzeichen angezeigt. Durch setzen des
 * ColorModes kann vorgegeben werden, ob negative Werte rot (default) oder immer schwarz angezeigt
 * werden.
 */
public class AWEditCurrency extends EditText implements AWInterface {
    private static final String AMOUNT = "AMOUNT", STATE = "STATE", HASFOCUS = "HASFOCUS";
    private long amount = 0L;
    private boolean colorMode = true;
    private int mBroadcastIndex;
    private OnLongValueChangedListener mOnLongValueChangedListener;

    public AWEditCurrency(Context context) {
        super(context);
    }

    public AWEditCurrency(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWEditCurrency(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getBroadcastIndex() {
        return mBroadcastIndex;
    }

    public void setBroadcastIndex(int index) {
        mBroadcastIndex = index;
    }

    /**
     * Liefert den aktuell eingegebenen Text als Long zurueck
     *
     * @return
     */
    public Long getValue() {
        if (hasFocus()) {
            try {
                amount = AWDBConvert
                        .convertCurrency(Double.parseDouble(getText().toString().trim()));
            } catch (NumberFormatException e) {
                // Passiert, wenn Textfeld leer ist.
                amount = 0L;
            }
        }
        return amount;
    }

    /**
     * Setzt einen long-Wert als Text. Dieser wird in das entsprechende Curency-Format
     * umformatiert.
     *
     * @param amount
     */
    public void setValue(Long amount) {
        if (amount == null) {
            amount = 0L;
        }
        this.amount = amount;
        if (hasFocus()) {
            setValueAsDouble();
        } else {
            setText(AWDBConvert.convertCurrency(amount));
        }
    }

    /**
     * Startwerte festlegen.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setSelectAllOnFocus(true);
        setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (hasFocus()) {
            // Wert als Double zeigen
            setValueAsDouble();
        } else {
            // Wert in der jeweiligen Currency zeigen
            setText(AWDBConvert.convertCurrency(amount));
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle extra = (Bundle) state;
        super.onRestoreInstanceState(extra.getParcelable(STATE));
        amount = extra.getLong(AMOUNT);
        setValue(amount);
        if (extra.getBoolean(HASFOCUS) & isEnabled()) {
            setValueAsDouble();
            requestFocus();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable p = super.onSaveInstanceState();
        Bundle extra = new Bundle();
        extra.putParcelable(STATE, p);
        extra.putLong(AMOUNT, amount);
        return extra;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (hasFocus() & lengthAfter != lengthBefore) {
            long newAmount = 0;
            try {
                newAmount = AWDBConvert.convertCurrency(Double.parseDouble(text.toString().trim()));
            } catch (NumberFormatException e) {
            }
            if (amount != newAmount) {
                amount = newAmount;
                if (mOnLongValueChangedListener != null) {
                    mOnLongValueChangedListener.onLongValueChanged(this, newAmount);
                }
                // wenn ColorMode, entsprechend Farbe belegen
                if (colorMode && amount < 0) {
                    setTextColor(Color.RED);
                } else {
                    setTextColor(Color.BLACK);
                }
            }
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    /**
     * Setz den ColorMode. Wenn true (default), wird ein Negativer Wert rot dargestellt. Bei false
     * werden alle Werte schwarz dargestellt.
     *
     * @param colorMode
     */
    public void setColorMode(boolean colorMode) {
        this.colorMode = colorMode;
    }

    public void setOnLongValueChangedListener(OnLongValueChangedListener listener) {
        mOnLongValueChangedListener = listener;
    }

    private void setValueAsDouble() {
        if (hasFocus()) {
            Double d = amount / AWDBConvert.mCurrencyDigits;
            setText(d.toString());
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
