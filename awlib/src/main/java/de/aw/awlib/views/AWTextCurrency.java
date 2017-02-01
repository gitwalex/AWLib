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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import de.aw.awlib.R;
import de.aw.awlib.database.AWDBConvert;

/**
 * Zeigt einen Betrag in der jeweiligen Waehrung an. Als Defult wird bei negativen Werten der Text
 * in rot gezeigt. Das kann durch {@link AWTextCurrency#setColorMode(boolean)} geaendert werden.
 */
public class AWTextCurrency extends TextView {
    private static final int minCharacters = 10;
    private boolean colorMode = true;
    private Long value = null;

    public AWTextCurrency(Context context) {
        super(context);
    }

    public AWTextCurrency(Context context, AttributeSet attrs) {
        super(context, attrs);
        setValue(attrs);
    }

    public AWTextCurrency(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setValue(attrs);
    }

    /**
     * @return Liefert den aktuellen Wert zurueck
     */
    public long getValue() {
        return value;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            setValue(0);
        }
        setEms(minCharacters);
        setGravity(Gravity.END);
    }

    /**
     * Setzt den ColorMode.
     *
     * @param colorMode
     *         colorMode. Wenn true (default), wird ein Negativer Wert rot dargestellt. Bei false
     *         werden alle Werte schwarz dargestellt.
     */
    public void setColorMode(boolean colorMode) {
        this.colorMode = colorMode;
    }

    public void setValue(float value) {
        setValue((long) (value * AWDBConvert.mCurrencyDigits));
    }

    private void setValue(AttributeSet attrs) {
        TypedArray a = getContext().getTheme()
                                   .obtainStyledAttributes(attrs, R.styleable.AWTextCurrency, 0, 0);
        try {
            float val = a.getFloat(R.styleable.AWTextCurrency_value, 0f);
            setValue(val);
        } finally {
            a.recycle();
        }
    }

    /**
     * Setzt einen long-Wert als Text. Dieser wird in das entsprechende Currency-Format
     * umformatiert.
     *
     * @param amount
     *         Wert zur Anzeige
     */
    public void setValue(long amount) {
        value = amount;
        setText(AWDBConvert.convertCurrency(value));
        if (colorMode && value < 0) {
            setTextColor(Color.RED);
        } else {
            setTextColor(Color.BLACK);
        }
    }
}
