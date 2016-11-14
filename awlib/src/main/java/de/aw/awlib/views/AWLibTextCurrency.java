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
package de.aw.awlib.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import de.aw.awlib.database.AbstractDBConvert;

/**
 * Zeigt einen Betrag in der jeweiligen Waehrung an. Als Defult wird bei negativen Werten der Text
 * in rot gezeigt. Das kann durch {@link AWLibTextCurrency#setColorMode(boolean)} geaendert werden.
 */
public class AWLibTextCurrency extends TextView {
    private static final int minCharacters = 10;
    private boolean colorMode = true;
    private Long value = null;

    public AWLibTextCurrency(Context context) {
        super(context);
    }

    public AWLibTextCurrency(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AWLibTextCurrency(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        setGravity(Gravity.RIGHT | Gravity.END);
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

    /**
     * Setzt einen long-Wert als Text. Dieser wird in das entsprechende Currency-Format
     * umformatiert.
     *
     * @param amount
     *         Wert zur Anzeige
     */
    public void setValue(long amount) {
        value = amount;
        setText(AbstractDBConvert.convertCurrency(value));
        if (colorMode && value < 0) {
            setTextColor(Color.RED);
        } else {
            setTextColor(Color.BLACK);
        }
    }
}
